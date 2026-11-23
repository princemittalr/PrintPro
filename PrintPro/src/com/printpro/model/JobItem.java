package com.printpro.model;

/**
 * One line on a job: a service and how many units.
 */
public class JobItem {

    private final PrintService service;
    private final int quantity;

    public JobItem(PrintService service, int quantity) {
        this.service = service;
        this.quantity = quantity;
    }

    public PrintService getService() { return service; }
    public int getQuantity()         { return quantity; }

    public double getLineTotal() {
        return service.getPrice() * quantity;
    }

    @Override
    public String toString() {
        return String.format("  %-24s %4d x %8.2f = %10.2f",
                service.getName(), quantity, service.getPrice(), getLineTotal());
    }
}
