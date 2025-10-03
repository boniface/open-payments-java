package zm.hashcode;

public final class Main {
    private Main() {
        // Utility class - prevent instantiation
    }

    public static void main(String[] args) {
        var name = "HashCode";
        IO.println("Hello and welcome! " + name);

        for (int i = 1; i <= 5; i++) {
            IO.println("i = " + i);
        }
    }
}
