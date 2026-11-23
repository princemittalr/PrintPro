package com.printpro.util;

import com.printpro.model.JobItem;

/**
 * Our own stack (Unit III), built on a plain array — no ArrayList allowed.
 * LIFO: the last service added is the first one removed when the operator enters -1.
 */
public class ItemStack {

    private JobItem[] items;
    private int top;          // number of items currently on the stack

    public ItemStack() {
        this(10);             // constructor chaining with this(...)
    }

    public ItemStack(int initialSize) {
        this.items = new JobItem[initialSize];
        this.top = 0;
    }

    public void push(JobItem item) {
        if (top == items.length) {
            grow();
        }
        items[top++] = item;
    }

    /** Removes and returns the last item, or null when empty. */
    public JobItem pop() {
        if (isEmpty()) {
            return null;
        }
        JobItem item = items[--top];
        items[top] = null;     // let the old reference go
        return item;
    }

    public JobItem peek() {
        return isEmpty() ? null : items[top - 1];
    }

    public boolean isEmpty() {
        return top == 0;
    }

    public int size() {
        return top;
    }

    /** Returns a copy of the items in the order they were added (bottom -> top). */
    public JobItem[] toArray() {
        JobItem[] copy = new JobItem[top];
        for (int i = 0; i < top; i++) {
            copy[i] = items[i];
        }
        return copy;
    }

    /** Doubles the array when full: manual resizing, the way ArrayList does internally. */
    private void grow() {
        JobItem[] bigger = new JobItem[items.length * 2];
        for (int i = 0; i < items.length; i++) {
            bigger[i] = items[i];
        }
        items = bigger;
    }
}
