package com.printpro.util;

import java.util.NoSuchElementException;
import java.util.Scanner;

/**
 * Safe console input (requirement 7): never crashes on letters, blanks,
 * negative numbers or a closed input stream.
 */
public class InputReader {

    private final Scanner scanner;

    public InputReader(Scanner scanner) {
        this.scanner = scanner;
    }

    /** Thrown (unchecked) when the input stream ends, so the app can shut down cleanly. */
    public static class InputClosedException extends RuntimeException {
        private static final long serialVersionUID = 1L;
        public InputClosedException() {
            super("Input closed");
        }
    }

    public String readLine(String prompt) {
        System.out.print(prompt);
        try {
            String text = scanner.nextLine().trim();
            if (!Ui.isInteractive()) {
                System.out.println(text);   // echo answers from a test file so the log is readable
            }
            return text;
        } catch (NoSuchElementException | IllegalStateException e) {   // multi-catch
            throw new InputClosedException();
        }
    }

    /** Reads a non-empty line. */
    public String readNonEmpty(String prompt) {
        while (true) {
            String text = readLine(prompt);
            if (!text.isEmpty()) {
                return text;
            }
            Ui.warn("This cannot be empty. Please try again.");
        }
    }

    // ---------- Overloaded methods (Unit III) ----------

    /** Any whole number. */
    public int readInt(String prompt) {
        while (true) {
            String text = readLine(prompt);
            try {
                return Integer.parseInt(text);
            } catch (NumberFormatException e) {
                Ui.warn("'" + text + "' is not a whole number. Please enter digits only.");
            }
        }
    }

    /** A whole number between min and max (inclusive). */
    public int readInt(String prompt, int min, int max) {
        while (true) {
            int value = readInt(prompt);
            if (value >= min && value <= max) {
                return value;
            }
            Ui.warn("Please enter a number from " + min + " to " + max + ".");
        }
    }

    /** Reads one of the allowed characters (case-insensitive), returned upper-case. */
    public char readChoice(String prompt, char... allowed) {
        while (true) {
            String text = readLine(prompt).toUpperCase();
            if (text.length() >= 1) {
                char c = text.charAt(0);
                // also accept the full words "STUDENT" / "FACULTY"
                for (char a : allowed) {
                    if (c == a && (text.length() == 1 || Character.isLetter(text.charAt(1)))) {
                        return c;
                    }
                }
            }
            Ui.warn("Invalid choice. Allowed: " + String.valueOf(allowed).replace("", " ").trim());
        }
    }
}
