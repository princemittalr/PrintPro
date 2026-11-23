package com.printpro.customer;

public class Student extends Customer {

    private static final double STUDENT_DISCOUNT = 0.10;

    public Student(String name) {
        super(name, 'S');
    }

    @Override
    public double getDiscountRate() {
        return STUDENT_DISCOUNT;
    }

    @Override
    public String getTypeName() {
        return "Student";
    }
}
