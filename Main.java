package MediBudget;

import javax.swing.*;
import java.sql.SQLException;
import java.util.Scanner;

/**
 * MediBudget Main Application Entry Point.
 * Launches the polished Java Swing Desktop GUI application by default,
 * ensures database schema readiness, and supports an optional CLI mode.
 */
public class Main {

    public static void main(String[] args) {

        // Check if CLI mode was explicitly requested via command line flag
        if (args.length > 0 && args[0].equalsIgnoreCase("--cli")) {
            runCliMode();
            return;
        }

        // Default: Launch modern Java Swing Desktop Application
        SwingUtilities.invokeLater(() -> {
            try {
                // Apply System Look and Feel for native OS aesthetics
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {}

            // Auto-initialize MySQL schema if connection is already configured
            try {
                DBConnection.initDatabase();
                System.out.println("[MediBudget] Database tables verified successfully.");
            } catch (SQLException e) {
                System.out.println("[MediBudget] Note: Database not yet initialized or credentials required. Use DB Settings dialog in GUI.");
            }

            // Launch Login Screen
            LoginFrame loginFrame = new LoginFrame();
            loginFrame.setVisible(true);
        });
    }

    /**
     * Optional Console / CLI fallback demonstration mode.
     */
    private static void runCliMode() {
        Scanner sc = new Scanner(System.in);
        System.out.println("\n========================================");
        System.out.println("              MEDIBUDGET");
        System.out.println("  Medical Expense Planner for Households");
        System.out.println("========================================");
        System.out.println("1. Launch Desktop GUI (Recommended)");
        System.out.println("2. Test Database Connectivity");
        System.out.println("3. Exit");
        System.out.print("\nEnter choice: ");

        int choice = sc.hasNextInt() ? sc.nextInt() : 3;
        sc.nextLine();

        if (choice == 1) {
            main(new String[]{});
        } else if (choice == 2) {
            try {
                DBConnection.initDatabase();
                System.out.println("✓ MySQL Database connected and tables verified!");
            } catch (Exception ex) {
                System.out.println("✗ Connection error: " + ex.getMessage());
            }
        } else {
            System.out.println("Exiting MediBudget.");
        }
    }
}