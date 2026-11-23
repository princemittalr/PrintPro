package com.printpro.billing;

/**
 * Interface (Unit IV): anything that can be billed.
 * An interface only says WHAT must be done; PrintJob decides HOW.
 */
public interface Billable {

    double getSubtotal();

    double getDiscountAmount();

    double getGstAmount();

    double getFinalTotal();

    String generateBill();
}
