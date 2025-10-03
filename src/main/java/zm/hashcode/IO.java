package zm.hashcode;

/**
 * Simple utility class for input/output operations.
 */
public final class IO {
    private IO() {
        // Utility class - prevent instantiation
    }

    /**
     * Prints a line to standard output.
     *
     * @param message
     *            the message to print
     */
    public static void println(String message) {
        System.out.println(message);
    }

    /**
     * Prints to standard output without a newline.
     *
     * @param message
     *            the message to print
     */
    public static void print(String message) {
        System.out.print(message);
    }
}
