package MediBudget;

/**
 * Automated Test Suite for MediBudget.
 * Validates cryptographic password hashing, model constructors,
 * and database connection utilities.
 */
public class TestMediBudget {

    public static void main(String[] args) {
        System.out.println("==================================================");
        System.out.println("    Running MediBudget Automated Test Suite       ");
        System.out.println("==================================================");

        int passed = 0;
        int failed = 0;

        // Test 1: Password Salt & Hash Generation
        try {
            String salt = PasswordUtil.generateSalt();
            assertNotNull(salt, "Salt should not be null");
            assertTrue(salt.length() == 32, "Salt length should be 32 hex chars (16 bytes)");

            String hash1 = PasswordUtil.hashPassword("secret123", salt);
            assertNotNull(hash1, "Hash should not be null");
            assertTrue(hash1.length() == 64, "SHA-256 hash should be 64 hex characters");

            // Test verification
            assertTrue(PasswordUtil.verifyPassword("secret123", salt, hash1), "Valid password verification failed");
            assertTrue(!PasswordUtil.verifyPassword("wrongpass", salt, hash1), "Invalid password should return false");

            System.out.println("✓ Test 1: Cryptographic Password Hashing & Salt Verification PASSED");
            passed++;
        } catch (Exception e) {
            System.err.println("✗ Test 1 FAILED: " + e.getMessage());
            failed++;
        }

        // Test 2: Budget Model Computations
        try {
            Budget b = new Budget(1, 1, 10, 2026, 20000.0, 5000.0, 80);
            assertTrue(b.getRemainingAmount() == 15000.0, "Remaining budget calculation wrong");
            assertTrue(Math.abs(b.getPercentageUsed() - 25.0) < 0.001, "Percentage calculation wrong");
            assertTrue(!b.isAlertTriggered(), "Alert should not be triggered at 25%");

            b.addExpense(12000.0); // Total spent = 17000.0 (85%)
            assertTrue(b.isAlertTriggered(), "Alert SHOULD be triggered at 85% when threshold is 80%");

            System.out.println("✓ Test 2: Household Budget Math & Alert Threshold Calculations PASSED");
            passed++;
        } catch (Exception e) {
            System.err.println("✗ Test 2 FAILED: " + e.getMessage());
            failed++;
        }

        // Test 3: DB Connection Config Loading
        try {
            String host = DBConnection.getHost();
            String port = DBConnection.getPort();
            String db = DBConnection.getDbName();
            assertNotNull(host, "Host should not be null");
            assertNotNull(port, "Port should not be null");
            assertNotNull(db, "Database name should not be null");
            assertTrue(DBConnection.getJdbcUrl().contains(host), "JDBC URL should contain host");

            System.out.println("✓ Test 3: Database Connection Configuration & URL Resolution PASSED");
            passed++;
        } catch (Exception e) {
            System.err.println("✗ Test 3 FAILED: " + e.getMessage());
            failed++;
        }

        // Test 4: UI Formatting Utilities
        try {
            String formatted = UITheme.formatCurrency(12500.50);
            assertTrue(formatted.contains("12,500.50"), "Currency formatting mismatch: " + formatted);
            System.out.println("✓ Test 4: UI Currency Formatting (₹12,500.50) PASSED");
            passed++;
        } catch (Exception e) {
            System.err.println("✗ Test 4 FAILED: " + e.getMessage());
            failed++;
        }

        System.out.println("==================================================");
        System.out.println("Results: " + passed + " passed, " + failed + " failed.");
        System.out.println("==================================================");

        if (failed > 0) {
            System.exit(1);
        }
    }

    private static void assertTrue(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }

    private static void assertNotNull(Object obj, String message) {
        if (obj == null) {
            throw new AssertionError(message);
        }
    }
}
