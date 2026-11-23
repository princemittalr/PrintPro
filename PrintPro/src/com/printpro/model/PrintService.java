package com.printpro.model;

/**
 * One row of the service list: id, name, category, price, daily capacity.
 */
public class PrintService {

    public static final int LOW_LIMIT = 10;   // below this, status shows LOW

    private final int id;
    private final String name;
    private final String category;
    private final double price;
    private final int dailyCapacity;   // capacity at the start of the day
    private int remaining;             // capacity still free today
    private int soldToday;             // units sold today (for "most popular")
    private final double minutesPerUnit; // how long one unit takes (for the ready-by time)

    public PrintService(int id, String name, String category, double price, int dailyCapacity, double minutesPerUnit) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.price = price;
        this.dailyCapacity = dailyCapacity;
        this.remaining = dailyCapacity;
        this.soldToday = 0;
        this.minutesPerUnit = minutesPerUnit;
    }

    /** Ternary operator ?: (Unit II): LOW when below 10, else OK. */
    public String getStatus() {
        return remaining < LOW_LIMIT ? "LOW" : "OK";
    }

    /** Only called after the whole job has been validated. */
    public void consume(int qty) {
        this.remaining -= qty;
        this.soldToday += qty;
    }

    public int getId()            { return id; }
    public String getName()       { return name; }
    public String getCategory()   { return category; }
    public double getPrice()      { return price; }
    public int getDailyCapacity() { return dailyCapacity; }
    public int getRemaining()     { return remaining; }
    public int getSoldToday()     { return soldToday; }
    public double getMinutesPerUnit() { return minutesPerUnit; }

    /** Percentage of today's capacity already used (int cast = type casting). */
    public int getUsedPercent() {
        return (int) Math.round(soldToday * 100.0 / dailyCapacity);
    }

    /** true when taking extraQty more would drop this service from OK to LOW. */
    public boolean wouldTurnLow(int extraQty) {
        return remaining >= LOW_LIMIT && remaining - extraQty < LOW_LIMIT;
    }

    @Override
    public String toString() {
        return String.format("| %-4d | %-22s | %-10s | %9.2f | %5d / %-5d | %-6s |",
                id, name, category, price, remaining, dailyCapacity, getStatus());
    }
}
