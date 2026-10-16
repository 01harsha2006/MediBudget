package MediBudget;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Data Access Object for User authentication and profile management.
 */
public class UserDAO {

    /**
     * Checks if a given username is already registered.
     */
    public boolean isUsernameTaken(String username) throws SQLException {
        String sql = "SELECT id FROM users WHERE username = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    /**
     * Registers a new user with cryptographic salt and hash.
     */
    public User registerUser(String fullName, String department, String username, String plainPassword) throws SQLException {
        if (isUsernameTaken(username)) {
            throw new IllegalArgumentException("Username '" + username + "' is already in use. Please choose another.");
        }

        String salt = PasswordUtil.generateSalt();
        String hash = PasswordUtil.hashPassword(plainPassword, salt);

        String sql = "INSERT INTO users (full_name, department, username, password_hash, password_salt) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, fullName);
            ps.setString(2, department);
            ps.setString(3, username);
            ps.setString(4, hash);
            ps.setString(5, salt);
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    int id = rs.getInt(1);
                    return new User(id, fullName, department, username, hash, salt);
                }
            }
        }
        return null;
    }

    /**
     * Authenticates a user with username and plain password.
     * Returns User object upon success, or null if credentials are invalid.
     */
    public User loginUser(String username, String plainPassword) throws SQLException {
        String sql = "SELECT id, full_name, department, username, password_hash, password_salt FROM users WHERE username = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int id = rs.getInt("id");
                    String fullName = rs.getString("full_name");
                    String department = rs.getString("department");
                    String uname = rs.getString("username");
                    String hash = rs.getString("password_hash");
                    String salt = rs.getString("password_salt");

                    if (PasswordUtil.verifyPassword(plainPassword, salt, hash)) {
                        return new User(id, fullName, department, uname, hash, salt);
                    }
                }
            }
        }
        return null;
    }
}
