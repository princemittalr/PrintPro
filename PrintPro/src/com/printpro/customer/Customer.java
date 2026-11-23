package com.printpro.customer;

/**
 * Abstract class (Unit IV). We never create a plain "Customer" — only a
 * Student or a Faculty. Each subclass MUST override getDiscountRate().
 */
public abstract class Customer extends Person {

    protected char type;   // 'S' = Student, 'F' = Faculty

    public Customer(String name, char type) {
        super(name);       // calls Person(String) constructor
        this.type = type;
    }

    /** Overridden in Student (0.10) and Faculty (0.15). */
    public abstract double getDiscountRate();

    /** Readable type name, overridden by each subclass. */
    public abstract String getTypeName();

    /**
     * final method (Unit IV): subclasses can NOT override this.
     * The discount formula must be the same for everyone; only the rate differs.
     */
    public final double calculateDiscount(double subtotal) {
        return subtotal * getDiscountRate();
    }

    public char getType() {
        return type;
    }

    @Override
    public String toString() {
        return name + " (" + getTypeName() + ", " + (int) (getDiscountRate() * 100) + "% discount)";
    }
}
