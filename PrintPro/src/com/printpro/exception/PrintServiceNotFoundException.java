package com.printpro.exception;

/**
 * Checked exception (Unit V): thrown when the operator enters a service id
 * that is not in the service list. It extends Exception (not RuntimeException),
 * so every caller must either catch it or declare it with "throws".
 */
public class PrintServiceNotFoundException extends Exception {

    private static final long serialVersionUID = 1L;

    private final int serviceId;

    public PrintServiceNotFoundException(int serviceId) {
        super("Service id " + serviceId + " does not exist. Please check the service list (menu option 1).");
        this.serviceId = serviceId;
    }

    public int getServiceId() {
        return serviceId;
    }
}
