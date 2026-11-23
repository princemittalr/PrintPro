package com.printpro.app;

import com.printpro.customer.Customer;
import com.printpro.exception.CapacityExceededException;
import com.printpro.exception.PrintServiceNotFoundException;
import com.printpro.model.JobItem;
import com.printpro.model.PrintJob;
import com.printpro.model.PrintService;
import com.printpro.util.Ui;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalTime;

/**
 * The shop: holds services in PrintService[] and jobs in PrintJob[] (arrays only — no ArrayList/HashMap).
 */
public class PrintShop {

    private static final String BILL_FOLDER = "bills";

    private final PrintService[] services;
    private PrintJob[] jobs;
    private int jobCount;
    private LocalTime queueFreeAt;   // when the shop finishes the jobs already accepted

    public PrintShop() {
        this.services = new PrintService[] {
            //               id   name                    category     price capacity minutes/unit
            new PrintService(101, "B/W Print (page)",     "Print",     2,   500,   0.2),
            new PrintService(102, "Colour Print (page)",  "Print",     10,  200,   0.5),
            new PrintService(103, "Spiral Binding",       "Binding",   40,  30,    5),
            new PrintService(104, "Hard Binding",         "Binding",   150, 10,    15),
            new PrintService(105, "Lamination (A4)",      "Finishing", 25,  50,    2),
            new PrintService(106, "Poster Print (A2)",    "Print",     120, 15,    4)
        };
        this.jobs = new PrintJob[10];
        this.jobCount = 0;
        this.queueFreeAt = LocalTime.now();
    }

    // ------------------------------------------------------------------
    // Services
    // ------------------------------------------------------------------

    /** throws: declares the checked exception so the caller must handle it. */
    public PrintService findService(int id) throws PrintServiceNotFoundException {
        for (PrintService s : services) {
            if (s.getId() == id) {
                return s;
            }
        }
        throw new PrintServiceNotFoundException(id);   // throw: actually raises it
    }

    /** Checks that totalQty (everything this job wants of the service) fits today's remaining capacity. */
    public void checkCapacity(PrintService s, int totalQty) throws CapacityExceededException {
        if (totalQty > s.getRemaining()) {
            throw new CapacityExceededException(s.getName(), totalQty, s.getRemaining());
        }
    }

    public void printServiceList() {
        String line = "+------+------------------------+------------+-----------+---------------+--------+";
        System.out.println(line);
        System.out.println(Ui.bold("| ID   | Service                | Category   | Price(Rs) | Left / Daily  | Status |"));
        System.out.println(line);
        for (PrintService s : services) {
            System.out.println(s.getStatus().equals("LOW") ? Ui.yellow(s.toString()) : s.toString());
        }
        System.out.println(line);
        System.out.println(Ui.grey("  Status is LOW when fewer than " + PrintService.LOW_LIMIT + " units are left today."));
    }

    // ------------------------------------------------------------------
    // Jobs
    // ------------------------------------------------------------------

    /**
     * Accepts a job ALL-OR-NOTHING.
     * Pass 1 validates every service; only if all pass, pass 2 deducts capacity.
     * So a rejected job never changes any daily capacity.
     */
    public PrintJob acceptJob(Customer customer, JobItem[] items) throws CapacityExceededException {
        // Pass 1: total quantity per service (parallel int[] to services[])
        int[] wanted = new int[services.length];
        for (JobItem item : items) {
            wanted[indexOf(item.getService())] += item.getQuantity();
        }
        for (int i = 0; i < services.length; i++) {
            if (wanted[i] > 0) {
                checkCapacity(services[i], wanted[i]);   // may throw -> nothing deducted
            }
        }
        // Pass 2: everything is valid, now deduct
        for (int i = 0; i < services.length; i++) {
            if (wanted[i] > 0) {
                services[i].consume(wanted[i]);
            }
        }
        // Ready-by time: jobs are done one after another, so this job starts when the queue is free
        int minutes = workMinutes(items);
        LocalTime now = LocalTime.now();
        LocalTime start = queueFreeAt.isAfter(now) ? queueFreeAt : now;
        queueFreeAt = start.plusMinutes(minutes);
        PrintJob job = new PrintJob(customer, items, queueFreeAt, minutes);
        addJob(job);
        return job;
    }

    /** Total work time of a job in whole minutes (at least 1). */
    public int workMinutes(JobItem[] items) {
        double total = 0;
        for (JobItem item : items) {
            total += item.getQuantity() * item.getService().getMinutesPerUnit();
        }
        return Math.max(1, (int) Math.ceil(total));
    }

    /** Minutes of accepted work still waiting in the queue right now. */
    public int queueMinutes() {
        LocalTime now = LocalTime.now();
        return queueFreeAt.isAfter(now) ? (int) java.time.Duration.between(now, queueFreeAt).toMinutes() : 0;
    }

    private int indexOf(PrintService s) {
        for (int i = 0; i < services.length; i++) {
            if (services[i] == s) {
                return i;
            }
        }
        return -1;
    }

    private void addJob(PrintJob job) {
        if (jobCount == jobs.length) {                 // grow the array manually
            PrintJob[] bigger = new PrintJob[jobs.length * 2];
            for (int i = 0; i < jobs.length; i++) {
                bigger[i] = jobs[i];
            }
            jobs = bigger;
        }
        jobs[jobCount++] = job;
    }

    /** Accepts "JOB3", "job3" or just "3". Returns null when not found. */
    public PrintJob findJob(String input) {
        String key = input.trim().toUpperCase();
        if (!key.startsWith("JOB")) {
            key = "JOB" + key;
        }
        for (int i = 0; i < jobCount; i++) {
            if (jobs[i].getJobId().equals(key)) {
                return jobs[i];
            }
        }
        return null;
    }

    public int getJobCount() {
        return jobCount;
    }

    public double getTotalRevenue() {
        double total = 0;
        for (int i = 0; i < jobCount; i++) {
            total += jobs[i].getFinalTotal();
        }
        return total;
    }

    // ------------------------------------------------------------------
    // Saving bills: try-with-resources + finally (Unit V)
    // ------------------------------------------------------------------

    public void saveBill(PrintJob job) {
        File folder = new File(BILL_FOLDER);
        try {
            if (!folder.exists() && !folder.mkdirs()) {
                throw new IOException("Could not create folder '" + BILL_FOLDER + "'");
            }
            File file = new File(folder, job.getJobId() + ".txt");
            // try-with-resources: the writer is closed automatically, even if an error happens
            try (PrintWriter out = new PrintWriter(new FileWriter(file))) {
                out.println(job.generateBill());
            }
            Ui.info("Bill saved to " + file.getPath());
        } catch (IOException e) {
            System.out.println("  ! Bill could not be saved to disk (" + e.getMessage() + "). It is still kept in memory.");
        }
    }

    // ------------------------------------------------------------------
    // End-of-day report
    // ------------------------------------------------------------------

    public String buildReport() {
        double revenue = 0;
        int students = 0, faculty = 0, tray1 = 0, tray2 = 0;
        for (int i = 0; i < jobCount; i++) {
            revenue += jobs[i].getFinalTotal();
            if (jobs[i].getCustomer().getType() == 'S') students++; else faculty++;
            if (jobs[i].getTray() == 1) tray1++; else tray2++;
        }

        PrintService popular = null;
        for (PrintService s : services) {
            if (s.getSoldToday() > 0 && (popular == null || s.getSoldToday() > popular.getSoldToday())) {
                popular = s;
            }
        }

        String line = "+" + "-".repeat(62) + "+";
        StringBuilder sb = new StringBuilder();
        sb.append(line).append('\n');
        sb.append(String.format("| %-60s |%n", "PRINTPRO - END OF DAY JOB REPORT"));
        sb.append(line).append('\n');
        sb.append(String.format("| %-30s %29d |%n", "Total jobs", jobCount));
        sb.append(String.format("| %-30s %29s |%n", "Total revenue", String.format("Rs. %.2f", revenue)));
        sb.append(String.format("| %-30s %29s |%n", "Most popular service",
                popular == null ? "None yet" : popular.getName() + " (" + popular.getSoldToday() + ")"));
        sb.append(String.format("| %-30s %29s |%n", "Student / Faculty jobs", students + " / " + faculty));
        sb.append(String.format("| %-30s %29s |%n", "Tray 1 / Tray 2 jobs", tray1 + " / " + tray2));
        if (jobCount > 0) {
            sb.append(String.format("| %-30s %29s |%n", "Average bill", String.format("Rs. %.2f", revenue / jobCount)));
        }
        sb.append(line).append('\n');
        sb.append(String.format("| %-24s %8s %8s %8s %8s |%n", "Service", "Sold", "Left", "Used", "Status"));
        sb.append(line).append('\n');
        for (PrintService s : services) {
            sb.append(String.format("| %-24s %8d %8d %7d%% %8s |%n", s.getName(), s.getSoldToday(), s.getRemaining(), s.getUsedPercent(), s.getStatus()));
        }
        sb.append(line).append('\n');
        if (jobCount > 0) {
            sb.append("  Jobs today:\n");
            for (int i = 0; i < jobCount; i++) {
                sb.append("  ").append(jobs[i].toString()).append('\n');
            }
        }
        return sb.toString();
    }

    public void saveReport(String report) {
        File folder = new File(BILL_FOLDER);
        try {
            if (!folder.exists() && !folder.mkdirs()) {
                throw new IOException("Could not create folder '" + BILL_FOLDER + "'");
            }
            try (PrintWriter out = new PrintWriter(new FileWriter(new File(folder, "DAY_REPORT.txt")))) {
                out.print(report);
            }
            Ui.info("Report saved to " + BILL_FOLDER + File.separator + "DAY_REPORT.txt");
        } catch (IOException e) {
            System.out.println("  ! Report could not be saved (" + e.getMessage() + ").");
        }
    }
}
