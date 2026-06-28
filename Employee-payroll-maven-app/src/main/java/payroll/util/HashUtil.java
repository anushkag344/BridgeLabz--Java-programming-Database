package payroll.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class HashUtil {

    private HashUtil() {
        // Prevent instantiation
    }

    /**
     * Returns SHA-256 hash of the given password.
     *
     * @param password Plain text password
     * @return SHA-256 hash in hexadecimal format
     */
    public static String hashPassword(String password) {

        try {

            MessageDigest md = MessageDigest.getInstance("SHA-256");

            byte[] hashBytes = md.digest(password.getBytes(StandardCharsets.UTF_8));

            StringBuilder sb = new StringBuilder();

            for (byte b : hashBytes) {
                sb.append(String.format("%02x", b));
            }

            return sb.toString();

        } catch (NoSuchAlgorithmException e) {

            throw new RuntimeException("SHA-256 Algorithm not found.", e);

        }
    }

    /**
     * Compares a plain password with a stored hash.
     *
     * @param plainPassword User entered password
     * @param storedHash Password stored in database
     * @return true if password matches
     */
    public static boolean verifyPassword(String plainPassword, String storedHash) {

        return hashPassword(plainPassword).equals(storedHash);

    }
}