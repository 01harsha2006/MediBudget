package MediBudget;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

/**
 * Reusable MySQL Database Connection and Schema Management.
 * Manages configuration loading, connection retrieval, connection verification,
 * and automated database and table creation.
 */
public class DBConnection {

    private static final String CONFIG_FILE = "db.properties";
    private static Properties config = new Properties();
    private static boolean driverLoaded = false;

    static {
        loadDriver();
        loadConfiguration();
    }

    private static void loadDriver() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            driverLoaded = true;
        } catch (ClassNotFoundException e) {
            System.err.println("[MediBudget DB] MySQL JDBC Driver not found in classpath: " + e.getMessage());
        }
    }

    /**
     * Loads database configuration from db.properties if available,
     * otherwise falls back to system properties/environment variables or sensible defaults.
     */
    public static synchronized void loadConfiguration() {
        config = new Properties();
        File propFile = new File(CONFIG_FILE);

        if (propFile.exists() && propFile.isFile()) {
            try (InputStream in = new FileInputStream(propFile)) {
                config.load(in);
            } catch (IOException e) {
                System.err.println("[MediBudget DB] Failed to read db.properties: " + e.getMessage());
            }
        }

        // Apply defaults if properties missing
        if (!config.containsKey("db.host")) {
            config.setProperty("db.host", System.getenv().getOrDefault("MEDIBUDGET_DB_HOST", "localhost"));
        }
        if (!config.containsKey("db.port")) {
            config.setProperty("db.port", System.getenv().getOrDefault("MEDIBUDGET_DB_PORT", "3306"));
        }
        if (!config.containsKey("db.name")) {
            config.setProperty("db.name", System.getenv().getOrDefault("MEDIBUDGET_DB_NAME", "medibudget_db"));
        }
        if (!config.containsKey("db.user")) {
            config.setProperty("db.user", System.getenv().getOrDefault("MEDIBUDGET_DB_USER", "root"));
        }
        if (!config.containsKey("db.password")) {
            config.setProperty("db.password", System.getenv().getOrDefault("MEDIBUDGET_DB_PASSWORD", ""));
        }
    }

    public static String getHost() {
        return config.getProperty("db.host", "localhost");
    }

    public static String getPort() {
        return config.getProperty("db.port", "3306");
    }

    public static String getDbName() {
        return config.getProperty("db.name", "medibudget_db");
    }

    public static String getUser() {
        return config.getProperty("db.user", "root");
    }

    public static String getPassword() {
        return config.getProperty("db.password", "");
    }

    public static String getJdbcUrl() {
        String customUrl = config.getProperty("db.url");
        if (customUrl != null && !customUrl.trim().isEmpty()) {
            return customUrl.trim();
        }
        return "jdbc:mysql://" + getHost() + ":" + getPort() + "/" + getDbName()
                + "?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    }

    /**
     * Saves the provided database configuration to db.properties.
     */
    public static synchronized void saveConfiguration(String host, String port, String dbName, String user, String password) throws IOException {
        config.setProperty("db.host", host);
        config.setProperty("db.port", port);
        config.setProperty("db.name", dbName);
        config.setProperty("db.user", user);
        config.setProperty("db.password", password);
        config.setProperty("db.url", "jdbc:mysql://" + host + ":" + port + "/" + dbName
                + "?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC");

        try (FileOutputStream out = new FileOutputStream(CONFIG_FILE)) {
            config.store(out, "MediBudget Database Configuration - Auto-generated");
        }
    }

    /**
     * Attempts to obtain a new Connection using current configuration.
     */
    public static Connection getConnection() throws SQLException {
        if (!driverLoaded) {
            loadDriver();
        }
        return DriverManager.getConnection(getJdbcUrl(), getUser(), getPassword());
    }

    /**
     * Tests a database connection with specific credentials without altering stored config.
     */
    public static boolean testConnection(String host, String port, String dbName, String user, String password) throws SQLException {
        if (!driverLoaded) {
            loadDriver();
        }
        String testUrl = "jdbc:mysql://" + host + ":" + port + "/" + dbName
                + "?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
        try (Connection conn = DriverManager.getConnection(testUrl, user, password)) {
            return conn != null && !conn.isClosed();
        }
    }

    /**
     * Initializes the database schema, creating tables and foreign keys if not already created.
     */
    public static void initDatabase() throws SQLException {
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {

            // 1. Users Table
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS users ("
                    + "id INT AUTO_INCREMENT PRIMARY KEY, "
                    + "username VARCHAR(50) NOT NULL UNIQUE, "
                    + "password_hash VARCHAR(256) NOT NULL, "
                    + "password_salt VARCHAR(64) NOT NULL, "
                    + "full_name VARCHAR(100) NOT NULL, "
                    + "department VARCHAR(100) DEFAULT '', "
                    + "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP"
                    + ") ENGINE=InnoDB");

            // 2. Family Members Table
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS family_members ("
                    + "id INT AUTO_INCREMENT PRIMARY KEY, "
                    + "user_id INT NOT NULL, "
                    + "name VARCHAR(100) NOT NULL, "
                    + "age INT NOT NULL, "
                    + "relationship VARCHAR(50) NOT NULL, "
                    + "gender VARCHAR(20) DEFAULT 'Not Specified', "
                    + "blood_group VARCHAR(10) DEFAULT 'Unknown', "
                    + "notes VARCHAR(255) DEFAULT '', "
                    + "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, "
                    + "CONSTRAINT fk_family_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE"
                    + ") ENGINE=InnoDB");

            // 3. Budgets Table
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS budgets ("
                    + "id INT AUTO_INCREMENT PRIMARY KEY, "
                    + "user_id INT NOT NULL, "
                    + "budget_month INT NOT NULL, "
                    + "budget_year INT NOT NULL, "
                    + "monthly_budget DECIMAL(12, 2) NOT NULL DEFAULT 0.00, "
                    + "alert_threshold_percent INT NOT NULL DEFAULT 80, "
                    + "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, "
                    + "CONSTRAINT uq_user_budget_period UNIQUE (user_id, budget_month, budget_year), "
                    + "CONSTRAINT fk_budget_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE"
                    + ") ENGINE=InnoDB");

            // 4. Medical Expenses Table
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS medical_expenses ("
                    + "id INT AUTO_INCREMENT PRIMARY KEY, "
                    + "user_id INT NOT NULL, "
                    + "family_member_id INT NULL, "
                    + "patient_name VARCHAR(100) NOT NULL, "
                    + "category VARCHAR(60) NOT NULL, "
                    + "description VARCHAR(255) NOT NULL, "
                    + "amount DECIMAL(12, 2) NOT NULL, "
                    + "expense_date DATE NOT NULL, "
                    + "payment_mode VARCHAR(40) DEFAULT 'Cash', "
                    + "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, "
                    + "CONSTRAINT fk_expense_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE, "
                    + "CONSTRAINT fk_expense_family FOREIGN KEY (family_member_id) REFERENCES family_members(id) ON DELETE SET NULL"
                    + ") ENGINE=InnoDB");

            // 5. Treatment Plans Table
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS treatments ("
                    + "id INT AUTO_INCREMENT PRIMARY KEY, "
                    + "user_id INT NOT NULL, "
                    + "family_member_id INT NULL, "
                    + "patient_name VARCHAR(100) NOT NULL, "
                    + "treatment_name VARCHAR(120) NOT NULL, "
                    + "hospital VARCHAR(150) NOT NULL, "
                    + "doctor_name VARCHAR(100) DEFAULT '', "
                    + "estimated_cost DECIMAL(12, 2) NOT NULL, "
                    + "status VARCHAR(50) NOT NULL DEFAULT 'Planned', "
                    + "start_date DATE NULL, "
                    + "notes VARCHAR(255) DEFAULT '', "
                    + "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, "
                    + "CONSTRAINT fk_treatment_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE, "
                    + "CONSTRAINT fk_treatment_family FOREIGN KEY (family_member_id) REFERENCES family_members(id) ON DELETE SET NULL"
                    + ") ENGINE=InnoDB");

            // 6. Insurance Policies Table
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS insurance_policies ("
                    + "id INT AUTO_INCREMENT PRIMARY KEY, "
                    + "user_id INT NOT NULL, "
                    + "provider VARCHAR(100) NOT NULL, "
                    + "policy_number VARCHAR(80) NOT NULL, "
                    + "policy_type VARCHAR(60) DEFAULT 'Health Floater', "
                    + "coverage_amount DECIMAL(12, 2) NOT NULL, "
                    + "premium_amount DECIMAL(12, 2) DEFAULT 0.00, "
                    + "expiry_date DATE NULL, "
                    + "notes VARCHAR(255) DEFAULT '', "
                    + "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, "
                    + "CONSTRAINT fk_policy_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE"
                    + ") ENGINE=InnoDB");

            // 7. Insurance Claims Table
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS insurance_claims ("
                    + "id INT AUTO_INCREMENT PRIMARY KEY, "
                    + "user_id INT NOT NULL, "
                    + "policy_id INT NULL, "
                    + "family_member_id INT NULL, "
                    + "patient_name VARCHAR(100) NOT NULL, "
                    + "claim_amount DECIMAL(12, 2) NOT NULL, "
                    + "settled_amount DECIMAL(12, 2) DEFAULT 0.00, "
                    + "status VARCHAR(50) NOT NULL DEFAULT 'Submitted', "
                    + "claim_date DATE NOT NULL, "
                    + "remarks VARCHAR(255) DEFAULT '', "
                    + "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, "
                    + "CONSTRAINT fk_claim_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE, "
                    + "CONSTRAINT fk_claim_policy FOREIGN KEY (policy_id) REFERENCES insurance_policies(id) ON DELETE SET NULL, "
                    + "CONSTRAINT fk_claim_family FOREIGN KEY (family_member_id) REFERENCES family_members(id) ON DELETE SET NULL"
                    + ") ENGINE=InnoDB");

            // 8. Reminders Table
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS reminders ("
                    + "id INT AUTO_INCREMENT PRIMARY KEY, "
                    + "user_id INT NOT NULL, "
                    + "family_member_id INT NULL, "
                    + "patient_name VARCHAR(100) NOT NULL, "
                    + "reminder_type VARCHAR(60) NOT NULL, "
                    + "description VARCHAR(255) NOT NULL, "
                    + "reminder_date DATE NOT NULL, "
                    + "status VARCHAR(30) NOT NULL DEFAULT 'Pending', "
                    + "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, "
                    + "CONSTRAINT fk_reminder_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE, "
                    + "CONSTRAINT fk_reminder_family FOREIGN KEY (family_member_id) REFERENCES family_members(id) ON DELETE SET NULL"
                    + ") ENGINE=InnoDB");
        }
    }
}
