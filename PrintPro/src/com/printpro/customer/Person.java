package com.printpro.customer;

/**
 * Base class of the hierarchy: Person -> abstract Customer -> Student / Faculty.
 */
public class Person {

    protected String name;

    public Person(String name) {
        this.name = name;            // "this" separates the field from the parameter
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return name;
    }
}
