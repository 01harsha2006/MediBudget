package MediBudget;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

/**
 * Utility for cryptographically secure password hashing and verification.
 * Uses SHA-256 with cryptographically random salt (128-bit).
 */
public class PasswordUtil {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    /**
     * Generates a 16-byte (128-bit) cryptographically random salt as a hex string.
     */
    public static String generateSalt() {
        byte[] salt = new byte[16];
        SECURE_RANDOM.nextBytes(salt);
        return bytesToHex(salt);
    }

    /**
     * Computes the SHA-256 hash of (salt + password).
     */
    public static String hashPassword(String password, String saltHex) {
        if (password == null || saltHex == null) {
            throw new IllegalArgumentException("Password and salt cannot be null.");
        }
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] saltBytes = hexToBytes(saltHex);
            digest.update(saltBytes);
            byte[] hashBytes = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(hashBytes);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available in runtime", e);
        }
    }

    /**
     * Verifies whether the provided plain password matches the stored salt and hash.
     */
    public static boolean verifyPassword(String plainPassword, String storedSalt, String storedHash) {
        if (plainPassword == null || storedSalt == null || storedHash == null) {
            return false;
        }
        String computedHash = hashPassword(plainPassword, storedSalt);
        // Constant-time comparison to prevent timing attacks
        return MessageDigest.isEqual(computedHash.getBytes(StandardCharsets.UTF_8),
                                    storedHash.getBytes(StandardCharsets.UTF_8));
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    private static byte[] hexToBytes(String hex) {
        int len = hex.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4)
                                 + Character.digit(hex.charAt(i + 1), 16));
        }
        return data;
    }
}
