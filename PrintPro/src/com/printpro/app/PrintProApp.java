package com.printpro.app;

import com.printpro.customer.Customer;
import com.printpro.customer.Faculty;
import com.printpro.customer.Student;
import com.printpro.exception.CapacityExceededException;
import com.printpro.exception.PrintServiceNotFoundException;
import com.printpro.model.JobItem;
import com.printpro.model.PrintJob;
import com.printpro.model.PrintService;
import com.printpro.util.InputReader;
import com.printpro.util.ItemStack;
import com.printpro.util.Ui;

import java.util.Scanner;

/**
 * PrintPro — menu-driven console billing for the campus print shop.
 * Entry point: com.printpro.app.PrintProApp   (optional flag: --no-color)
 */
public class PrintProApp {

    private final PrintShop shop;
    private final InputReader in;

    public PrintProApp(InputReader in) {
        this.shop = new PrintShop();
        this.in = in;
    }

    public static void main(String[] args) {
        for (String arg : args) {
            if (arg.equalsIgnoreCase("--no-color")) {
                Ui.disableColor();
            }
        }
        // try-with-resources: the Scanner is closed automatically when the program ends
        try (Scanner scanner = new Scanner(System.in)) {
            new PrintProApp(new InputReader(scanner)).run();
        }
    }

    // ------------------------------------------------------------------
    // Main menu: do-while + switch (Unit II)
    // ------------------------------------------------------------------

    public void run() {
        int choice = 0;
        try {
            do {
                Ui.clearScreen();
                printBanner();
                printMenu();
                choice = in.readInt(Ui.bold("  Enter choice (1-5): "));
                switch (choice) {
                    case 1:
                        viewServices();
                        break;
                    case 2:
                        newJob();
                        break;
                    case 3:
                        viewBill();
                        break;
                    case 4:
                        jobReport();
                        break;
                    case 5:
                        Ui.info("Closing the counter for today...");
                        break;
                    default:
                        Ui.warn("Invalid option. Please choose 1 to 5.");
                }
                if (choice != 5) {
                    Ui.pause(in);
                }
            } while (choice != 5);
        } catch (InputReader.InputClosedException e) {
            System.out.println();
            Ui.warn("Input ended - closing the counter safely.");
        } finally {
            // runs whether we exit normally or the input stream closes
            String report = shop.buildReport();
            System.out.println();
            System.out.print(Ui.cyan(report));
            shop.saveReport(report);
            Ui.ok("Goodbye from PrintPro!");
        }
    }

    private void printBanner() {
        System.out.println();
        System.out.println(Ui.cyan("  ____       _       _   ____"));
        System.out.println(Ui.cyan(" |  _ \\ _ __(_)_ __ | |_|  _ \\ _ __ ___"));
        System.out.println(Ui.cyan(" | |_) | '__| | '_ \\| __| |_) | '__/ _ \\"));
        System.out.println(Ui.cyan(" |  __/| |  | | | | | |_|  __/| | | (_) |"));
        System.out.println(Ui.cyan(" |_|   |_|  |_|_| |_|\\__|_|   |_|  \\___/ ")
                + Ui.grey("  Campus Print Shop - Billing Counter"));
    }

    private void printMenu() {
        int nextToken = PrintJob.getJobCount() + 1;
        int nextTray = (nextToken & 1) == 1 ? 1 : 2;
        System.out.println();
        System.out.println(Ui.grey(String.format("  Jobs today: %d  |  Revenue: Rs. %.2f  |  Next token: %d -> Tray %d  |  Queue: %d min",
                shop.getJobCount(), shop.getTotalRevenue(), nextToken, nextTray, shop.queueMinutes())));
        System.out.println("  +---------------------------------------+");
        System.out.println("  |  " + Ui.bold("1") + ". View service list (ids & prices)  |");
        System.out.println("  |  " + Ui.bold("2") + ". New job  (take an order + bill)   |");
        System.out.println("  |  " + Ui.bold("3") + ". View bill (find old bill by id)   |");
        System.out.println("  |  " + Ui.bold("4") + ". Job report (today's summary)      |");
        System.out.println("  |  " + Ui.bold("5") + ". Exit (close the day)              |");
        System.out.println("  +---------------------------------------+");
    }

    // ------------------------------------------------------------------
    // 1. Service list
    // ------------------------------------------------------------------

    private void viewServices() {
        Ui.title("SERVICE LIST");
        shop.printServiceList();
    }

    // ------------------------------------------------------------------
    // 2. New job: nested try, multiple catch, finally (Unit V)
    // ------------------------------------------------------------------

    private void newJob() {
        Ui.title("NEW JOB");

        // Step 1: customer
        System.out.println(Ui.bold("  Step 1 of 3 - Customer"));
        String name = in.readNonEmpty("  Customer name: ");
        char type = in.readChoice("  Type  [S] Student (10% off)  [F] Faculty (15% off): ", 'S', 'F');
        Customer customer = (type == 'S') ? new Student(name) : new Faculty(name);
        Ui.ok("Customer: " + customer);

        // Step 2: services
        System.out.println();
        System.out.println(Ui.bold("  Step 2 of 3 - Add services") + Ui.grey("   (type the ID from this list)"));
        shop.printServiceList();
        System.out.println(Ui.yellow("  Enter a service ID, then the quantity.   0 = finish job   -1 = undo last"));

        ItemStack cart = new ItemStack();
        boolean finished = false;

        try {                                                       // outer try
            do {
                int id = in.readInt(Ui.bold("\n  Service ID (0 = done, -1 = undo): "));
                if (id == 0) {
                    finished = true;
                } else if (id == -1) {
                    undoLast(cart);
                } else {
                    PrintService service = null;
                    try {                                           // nested try
                        service = shop.findService(id);
                        int qty = in.readInt("  How many \"" + service.getName() + "\"? ");
                        if (qty <= 0) {
                            Ui.warn("Quantity must be 1 or more. Line not added.");
                        } else {
                            int alreadyInCart = quantityInCart(cart, service);
                            shop.checkCapacity(service, alreadyInCart + qty);
                            cart.push(new JobItem(service, qty));
                            Ui.ok(String.format("Added %d x %s = Rs. %.2f", qty, service.getName(), service.getPrice() * qty));
                        }
                    } catch (PrintServiceNotFoundException e) {     // multiple catch blocks
                        Ui.error("NOT FOUND: " + e.getMessage());
                    } catch (CapacityExceededException e) {
                        Ui.error("REJECTED: " + e.getMessage());
                        Ui.info("No capacity was changed.");
                        offerWhatIsLeft(cart, service);             // smart capacity offer
                    }
                }
                if (!finished) {
                    printCartStatus(cart);
                }
            } while (!finished);

            if (cart.isEmpty()) {
                Ui.warn("Job cancelled - no services were added.");
                return;
            }

            // Step 3: confirm
            System.out.println();
            System.out.println(Ui.bold("  Step 3 of 3 - Confirm"));
            showCart(cart, customer);
            warnLowStock(cart);
            Ui.info("Estimated ready time: about " + shop.workMinutes(cart.toArray()) + " min of work"
                    + (shop.queueMinutes() > 0 ? ", after " + shop.queueMinutes() + " min of jobs already in the queue." : ", shop is free now."));
            char confirm = in.readChoice("  Confirm and print the bill? (Y/N): ", 'Y', 'N');
            if (confirm == 'N') {
                Ui.warn("Job discarded. No capacity was changed.");
                return;
            }

            PrintJob job = shop.acceptJob(customer, cart.toArray());   // all-or-nothing
            System.out.println();
            System.out.println(job.generateBill());
            Ui.ok(String.format("%s billed: Rs. %.2f  |  Token %d  ->  collect at TRAY %d  |  Ready by %s",
                    job.getJobId(), job.getFinalTotal(), job.getToken(), job.getTray(), job.getReadyByText()));
            shop.saveBill(job);
        } catch (CapacityExceededException e) {
            Ui.error("JOB REJECTED: " + e.getMessage());
            Ui.info("The whole job was cancelled. No daily capacity was changed.");
        } finally {
            Ui.info("(job entry closed)");
        }
    }

    /**
     * Smart capacity offer: instead of only saying "no", offer what the shop can still do today.
     */
    private void offerWhatIsLeft(ItemStack cart, PrintService service) {
        int canStillAdd = service.getRemaining() - quantityInCart(cart, service);
        if (canStillAdd <= 0) {
            Ui.info("Nothing left of " + service.getName() + " today. Try another service.");
            return;
        }
        char take = in.readChoice("  Only " + canStillAdd + " more can be done today. Add " + canStillAdd + " instead? (Y/N): ", 'Y', 'N');
        if (take == 'Y') {
            cart.push(new JobItem(service, canStillAdd));
            Ui.ok(String.format("Added %d x %s = Rs. %.2f", canStillAdd, service.getName(), service.getPrice() * canStillAdd));
        } else {
            Ui.info("Okay, nothing added. Enter a smaller quantity or another service.");
        }
    }

    /** Warns before billing if this job will push any service below the LOW limit. */
    private void warnLowStock(ItemStack cart) {
        JobItem[] items = cart.toArray();
        for (int i = 0; i < items.length; i++) {
            PrintService s = items[i].getService();
            boolean firstTime = true;                    // report each service once
            for (int j = 0; j < i; j++) {
                if (items[j].getService() == s) {
                    firstTime = false;
                }
            }
            int qty = quantityInCart(cart, s);
            if (firstTime && s.wouldTurnLow(qty)) {
                Ui.warn("After this job " + s.getName() + " will be LOW: only " + (s.getRemaining() - qty) + " left today.");
            }
        }
    }

    private void printCartStatus(ItemStack cart) {
        double sum = 0;
        for (JobItem item : cart.toArray()) {
            sum += item.getLineTotal();
        }
        System.out.println(Ui.grey(String.format("       Cart: %d item%s  |  Subtotal so far: Rs. %.2f",
                cart.size(), cart.size() == 1 ? "" : "s", sum)));
    }

    private void undoLast(ItemStack cart) {
        JobItem removed = cart.pop();
        if (removed == null) {
            Ui.warn("Nothing to undo - the job is empty.");
        } else {
            Ui.ok("Undo: removed " + removed.getQuantity() + " x " + removed.getService().getName());
        }
    }

    private int quantityInCart(ItemStack cart, PrintService service) {
        int total = 0;
        for (JobItem item : cart.toArray()) {
            if (item.getService() == service) {
                total += item.getQuantity();
            }
        }
        return total;
    }

    private void showCart(ItemStack cart, Customer customer) {
        double sum = 0;
        for (JobItem item : cart.toArray()) {
            System.out.println("  " + item);
            sum += item.getLineTotal();
        }
        double discount = customer.calculateDiscount(sum);
        double total = (sum - discount) * (1 + PrintJob.GST);
        System.out.println(Ui.grey("  " + "-".repeat(52)));
        System.out.printf("  %-38s Rs. %9.2f%n", "Subtotal", sum);
        System.out.println(Ui.bold(String.format("  %-38s Rs. %9.2f", "Estimated total (after discount + GST)", total)));
    }

    // ------------------------------------------------------------------
    // 3. Find an old bill
    // ------------------------------------------------------------------

    private void viewBill() {
        Ui.title("VIEW BILL");
        if (shop.getJobCount() == 0) {
            Ui.warn("No jobs billed yet today. Use option 2 to create one.");
            return;
        }
        Ui.info("Bills today: JOB1 to JOB" + shop.getJobCount());
        String id = in.readNonEmpty("  Enter job id (e.g. JOB1 or just 1): ");
        PrintJob job = shop.findJob(id);
        if (job == null) {
            Ui.error("No bill found for '" + id + "'. Valid ids: JOB1 to JOB" + shop.getJobCount() + ".");
        } else {
            System.out.println();
            System.out.println(job.generateBill());
        }
    }

    // ------------------------------------------------------------------
    // 4. Report
    // ------------------------------------------------------------------

    private void jobReport() {
        Ui.title("JOB REPORT");
        System.out.print(shop.buildReport());
    }
}
