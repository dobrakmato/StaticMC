package eu.matejkormuth.staticmc;

public class Main {
    public static void main(final String[] args) {
        // Start server.
        System.out.println("Starting StaticMC...");
        new StaticMC(25535).start();
    }
}
