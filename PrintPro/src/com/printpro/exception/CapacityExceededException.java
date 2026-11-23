package com.printpro.exception;

/**
 * Checked exception (Unit V): thrown when a job asks for more units of a
 * service than the shop can still finish today.
 */
public class CapacityExceededException extends Exception {

    private static final long serialVersionUID = 1L;

    private final String serviceName;
    private final int requested;
    private final int available;

    public CapacityExceededException(String serviceName, int requested, int available) {
        super("Capacity exceeded for " + serviceName + ": requested " + requested
                + ", only " + available + " left today.");
        this.serviceName = serviceName;
        this.requested = requested;
        this.available = available;
    }

    public String getServiceName() { return serviceName; }
    public int getRequested()      { return requested; }
    public int getAvailable()      { return available; }
}
