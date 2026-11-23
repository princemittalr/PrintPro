package com.printpro.util;

/**
 * Small helper for nicer console output: colours, pauses and screen clearing.
 * Colours use ANSI codes and are only switched on when the terminal supports them
 * (Windows Terminal, VS Code, IntelliJ, macOS/Linux). Run with --no-color to turn them off.
 */
public final class Ui {

    private static final String RESET  = "\u001B[0m";
    private static final String BOLD   = "\u001B[1m";
    private static final String RED    = "\u001B[31m";
    private static final String GREEN  = "\u001B[32m";
    private static final String YELLOW = "\u001B[33m";
    private static final String CYAN   = "\u001B[36m";
    private static final String GREY   = "\u001B[90m";

    /** true when a person is typing (not when input comes from a test file). */
    private static final boolean INTERACTIVE = System.console() != null;

    private static boolean colorOn = detectColor();

    private Ui() { }   // utility class: no objects

    private static boolean detectColor() {
        if (!INTERACTIVE || System.getenv("NO_COLOR") != null) {
            return false;
        }
        boolean windows = System.getProperty("os.name", "").toLowerCase().contains("win");
        if (!windows) {
            return true;
        }
        // Windows Terminal, VS Code terminal and IntelliJ all support ANSI colours
        return System.getenv("WT_SESSION") != null
                || "vscode".equals(System.getenv("TERM_PROGRAM"))
                || System.getenv("IDEA_INITIAL_DIRECTORY") != null
                || System.getenv("TERM") != null;
    }

    public static void disableColor()    { colorOn = false; }
    public static boolean isInteractive() { return INTERACTIVE; }

    private static String paint(String code, String text) {
        return colorOn ? code + text + RESET : text;
    }

    public static String red(String t)    { return paint(RED, t); }
    public static String green(String t)  { return paint(GREEN, t); }
    public static String yellow(String t) { return paint(YELLOW, t); }
    public static String cyan(String t)   { return paint(CYAN, t); }
    public static String grey(String t)   { return paint(GREY, t); }
    public static String bold(String t)   { return paint(BOLD, t); }

    public static void ok(String msg)    { System.out.println(green("  [OK] " + msg)); }
    public static void error(String msg) { System.out.println(red("  [X]  " + msg)); }
    public static void warn(String msg)  { System.out.println(yellow("  [!]  " + msg)); }
    public static void info(String msg)  { System.out.println(grey("       " + msg)); }

    public static void title(String text) {
        System.out.println();
        System.out.println(cyan(bold("  == " + text + " " + "=".repeat(Math.max(3, 44 - text.length())))));
    }

    /** Clears the screen (only when ANSI is available, so nothing breaks elsewhere). */
    public static void clearScreen() {
        if (colorOn) {
            System.out.print("\u001B[H\u001B[2J");
            System.out.flush();
        }
    }

    /** Waits for Enter so the operator can read the result before the menu returns. */
    public static void pause(InputReader in) {
        if (INTERACTIVE) {
            in.readLine(grey("\n  Press Enter to return to the menu..."));
        }
    }
}
