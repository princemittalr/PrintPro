package com.printpro.customer;

public class Faculty extends Customer {

    private static final double FACULTY_DISCOUNT = 0.15;

    public Faculty(String name) {
        super(name, 'F');
    }

    @Override
    public double getDiscountRate() {
        return FACULTY_DISCOUNT;
    }

    @Override
    public String getTypeName() {
        return "Faculty";
    }
}
