package com.printpro.model;

import com.printpro.billing.Billable;
import com.printpro.customer.Customer;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * A finished, accepted job. Implements Billable (Unit IV).
 * Billing: subtotal -> minus discount -> plus 18% GST on the discounted amount.
 */
public class PrintJob implements Billable {

    public static final double GST = 0.18;      // Unit I: final double GST = 0.18

    private static int jobCounter = 0;          // Unit III: static, shared by all jobs
    private static int tokenCounter = 0;

    private final String jobId;                 // JOB1, JOB2, ...
    private final int token;
    private final Customer customer;
    private final JobItem[] items;
    private final LocalTime readyBy;           // when the prints will be ready
    private final int workMinutes;             // shop time this job needs

    public PrintJob(Customer customer, JobItem[] items, LocalTime readyBy, int workMinutes) {
        jobCounter++;
        tokenCounter++;
        this.jobId = "JOB" + jobCounter;
        this.token = tokenCounter;
        this.customer = customer;
        this.items = items;
        this.readyBy = readyBy;
        this.workMinutes = workMinutes;
    }

    // ---------- Recursion (Unit III) ----------

    /** Recursive subtotal: line total of item[index] + subtotal of the rest. */
    private double calculateSubtotal(int index) {
        if (index >= items.length) {
            return 0.0;                                   // base case
        }
        return items[index].getLineTotal() + calculateSubtotal(index + 1);
    }

    // ---------- Billable ----------

    @Override
    public double getSubtotal() {
        return calculateSubtotal(0);
    }

    @Override
    public double getDiscountAmount() {
        return customer.calculateDiscount(getSubtotal());  // final method in Customer
    }

    public double getAmountAfterDiscount() {
        return getSubtotal() - getDiscountAmount();
    }

    @Override
    public double getGstAmount() {
        return getAmountAfterDiscount() * GST;
    }

    @Override
    public double getFinalTotal() {
        return round2(getAmountAfterDiscount() + getGstAmount());
    }

    // ---------- Tray rule (Unit II) ----------

    /** Bitwise AND: (token & 1) is 1 for odd numbers, 0 for even. */
    public int getTray() {
        return (token & 1) == 1 ? 1 : 2;
    }

    @Override
    public String generateBill() {
        String line = "+" + "-".repeat(52) + "+";
        StringBuilder sb = new StringBuilder();
        sb.append(line).append('\n');
        sb.append(String.format("|%s|%n", center("PRINTPRO - CAMPUS PRINT SHOP", 52)));
        sb.append(String.format("|%s|%n", center("TAX INVOICE", 52)));
        sb.append(line).append('\n');
        sb.append(String.format("| Job ID   : %-40s|%n", jobId));
        sb.append(String.format("| Token    : %-40s|%n", token + "   ->  Collect at TRAY " + getTray()));
        sb.append(String.format("| Ready by : %-40s|%n", getReadyByText() + "  (about " + workMinutes + " min of work)"));
        sb.append(String.format("| Customer : %-40s|%n", customer.getName()));
        sb.append(String.format("| Type     : %-40s|%n", customer.getTypeName()));
        sb.append(line).append('\n');
        sb.append(String.format("| %-22s %5s %9s %12s |%n", "Service", "Qty", "Rate", "Amount"));
        sb.append(line).append('\n');
        for (JobItem item : items) {
            sb.append(String.format("| %-22s %5d %9.2f %12.2f |%n",
                    item.getService().getName(), item.getQuantity(),
                    item.getService().getPrice(), item.getLineTotal()));
        }
        sb.append(line).append('\n');
        sb.append(String.format("| %-36s Rs.%10.2f |%n", "Subtotal", getSubtotal()));
        sb.append(String.format("| %-36s-Rs.%10.2f |%n",
                "Discount (" + customer.getTypeName() + " " + (int) Math.round(customer.getDiscountRate() * 100) + "%)",
                getDiscountAmount()));
        sb.append(String.format("| %-36s Rs.%10.2f |%n", "Amount after discount", getAmountAfterDiscount()));
        sb.append(String.format("| %-36s+Rs.%10.2f |%n", "GST (" + (int) Math.round(GST * 100) + "%)", getGstAmount()));
        sb.append(line).append('\n');
        sb.append(String.format("| %-36s Rs.%10.2f |%n", "FINAL TOTAL", getFinalTotal()));
        sb.append(line).append('\n');
        sb.append(String.format("|%s|%n", center("Thank you! Please collect from Tray " + getTray(), 52)));
        sb.append(line);
        return sb.toString();
    }

    // ---------- helpers ----------

    private static double round2(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    private static String center(String text, int width) {
        int left = (width - text.length()) / 2;
        int right = width - text.length() - left;
        return " ".repeat(Math.max(0, left)) + text + " ".repeat(Math.max(0, right));
    }

    public String getJobId()       { return jobId; }
    public int getToken()          { return token; }
    public Customer getCustomer()  { return customer; }
    public JobItem[] getItems()    { return items; }
    public static int getJobCount() { return jobCounter; }
    public LocalTime getReadyBy()   { return readyBy; }
    public int getWorkMinutes()     { return workMinutes; }

    public String getReadyByText() {
        return readyBy.format(DateTimeFormatter.ofPattern("hh:mm a"));
    }

    @Override
    public String toString() {
        return String.format("%-6s | Token %-3d | Tray %d | %-20s | %-7s | Rs.%10.2f",
                jobId, token, getTray(), customer.getName(), customer.getTypeName(), getFinalTotal());
    }
}
