package greet;

import greet.model.Greeting;
import greet.model.User;
import greet.util.DBUtil;
import greet.util.HashUtil;

import java.sql.*;
import java.util.Scanner;

public class GreetingJDBCApp {

    private static final Scanner scanner = new Scanner(System.in);
    private static User currentUser = null;
    public static void main(String[] args) {
        System.out.println("=== Greeting JDBC Application Console ===");
        while (true) {
            if (currentUser == null) {
                showAnonymousMenu();
            } else {
                showUserMenu();
            }
        }
    }

    private static void showAnonymousMenu() {

        System.out.println("\n1. Login");
        System.out.println("2. Register");
        System.out.println("3. Exit");

        System.out.print("Select Option: ");
        String choice = scanner.nextLine();
        switch (choice) {
            case "1":
                login();
                break;

                    case "2":
                register();
                break;

            case "3":
                System.out.println("Goodbye!");
                System.exit(0);

            default:
                System.out.println("Invalid Option.");
        }
    }

    private static void showUserMenu() {
        System.out.println("\nLogged in as : " + currentUser.getUsername() + " (" + currentUser.getRole() + ")"
        );

        System.out.println("1. View Greetings");
        System.out.println("2. View Audit Logs");
        System.out.println("3. Get Greeting Count");

        if (isAdmin()) {
            System.out.println("4. Create Greeting");
            System.out.println("5. Update Greeting");
            System.out.println("6. Delete Greeting");
        }

        System.out.println("7. Logout");

        System.out.print("Select Option: ");

        String choice = scanner.nextLine();

        switch (choice) {

            case "1":
                viewGreetings();
                break;

            case "2":
                viewAuditLogs();
                break;

            case "3":
                viewUserGreetingCount();
                break;

            case "4":
                if (isAdmin())
                    createGreeting();
                break;

            case "5":
                if (isAdmin())
                    updateGreeting();
                break;

            case "6":
                if (isAdmin())
                    deleteGreeting();
                break;

            case "7":
                logout();
                break;

            default:
                System.out.println("Invalid Option.");
        }
    }

    private static boolean isAdmin() {
        return currentUser != null && currentUser.getRole().equals("ADMIN");
    }
    private static void register() {
        System.out.print("Enter username: ");
        String username = scanner.nextLine();
        System.out.print("Enter password: ");
        String password = scanner.nextLine();
        System.out.print("Enter email: ");
        String email = scanner.nextLine();
        System.out.print("Enter role (ADMIN/USER): ");
        String role = scanner.nextLine().toUpperCase();
        if (!role.equals("ADMIN") && !role.equals("USER")) {
            System.out.println("Invalid role choice. Choose ADMIN or USER.");
            return;
        }
        String hashedPassword = HashUtil.hashPassword(password);
        String sql = "INSERT INTO users (username, password, email, role) VALUES (?, ?, ?, ?)";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setString(2, hashedPassword);
            pstmt.setString(3, email);
            pstmt.setString(4, role);
            pstmt.executeUpdate();
            System.out.println("Registration successful! You can now log in.");
        } catch (SQLException e) {
            System.err.println("Registration failed. Username or email might already exist.");
                    e.printStackTrace();
        }
    }
    private static void login() {
        System.out.print("Enter username: ");
        String username = scanner.nextLine();
        System.out.print("Enter password: ");
        String password = scanner.nextLine();
        String hashedPassword = HashUtil.hashPassword(password);
        String sql = "SELECT id, username, password, email, role FROM users WHERE username = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String dbPassword = rs.getString("password");
                    if (dbPassword.equals(hashedPassword)) {
                        currentUser = new User(
                                rs.getInt("id"),
                                rs.getString("username"),
                                dbPassword,
                                rs.getString("email"),
                                rs.getString("role")
                        );
                        System.out.println("Login successful! Welcome " +
                                currentUser.getUsername());
                    } else {
                        System.out.println("Incorrect password.");
                    }
                } else {
                    System.out.println("Username not found.");
                }
            }
        } catch (SQLException e) {
            System.err.println("Login error encountered.");
            e.printStackTrace();
        }
    }
    private static void logout() {
        System.out.println("Logged out successfully.");
        currentUser = null;
    }
    private static void viewGreetings() {
        String sql = "SELECT g.id, g.message, g.created_by, u.username AS creator " + "FROM greetings g " +
                "LEFT JOIN users u ON g.created_by = u.id " +
                "ORDER BY g.id ASC";
        try (Connection conn = DBUtil.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            System.out.println("\n--- GREETINGS CATALOG ---");
            boolean hasData = false;
            while (rs.next()) {
                hasData = true;
                Greeting greeting = new Greeting(
                        rs.getInt("id"),
                        rs.getString("message"),
                        rs.getInt("created_by"),
                        rs.getString("creator")
                );
                System.out.println(greeting);
            }
            if (!hasData) {
                System.out.println("No greetings found.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    private static void viewAuditLogs() {
        String sql = "SELECT id, greeting_id, action_type, old_message, new_message, changed_by, changed_at " +
        "FROM greeting_audit ORDER BY changed_at DESC";
        try (Connection conn = DBUtil.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            System.out.println("\n--- GREETING CHANGE AUDIT LOGS (TRIGGER AUTOMATION) ---");
            System.out.printf("%-4s | %-9s | %-8s | %-25s | %-25s | %-12s\n",
                    "ID", "Greet_ID", "Action", "Old Message", "New Message", "Changed By");
                    System.out.println("-----------------");
            boolean hasData = false;
            while (rs.next()) {
                hasData = true;
                System.out.printf("%-4d | %-9d | %-8s | %-25s | %-25s | %-12s\n",
                        rs.getInt("id"),
                        rs.getInt("greeting_id"),
                        rs.getString("action_type"),
                        rs.getString("old_message") != null ?
                                rs.getString("old_message") : "NULL",
                        rs.getString("new_message") != null ?
                                rs.getString("new_message") : "NULL",
                        rs.getString("changed_by")
                );
            }
            if (!hasData) {
                System.out.println("No audit logs exist yet.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    private static void viewUserGreetingCount() {
        System.out.print("Enter username to check greeting count statistics: ");
        String username = scanner.nextLine();

        String sql = "{ ? = call get_user_greeting_count(?) }";
        try (Connection conn = DBUtil.getConnection();
             CallableStatement cstmt = conn.prepareCall(sql)) {

            cstmt.registerOutParameter(1, Types.INTEGER);

            cstmt.setString(2, username);

            cstmt.execute();

            int count = cstmt.getInt(1);
            System.out.println("\n>> Statistics: User '" + username + "' has created " + count + " greeting(s).");
        } catch (SQLException e) {
            System.err.println("Failed to retrieve greeting counts using Stored Procedure.");
                    e.printStackTrace();
        }
    }
    private static void createGreeting() {
        System.out.print("Enter greeting message: ");
        String message = scanner.nextLine();
        String sql = "INSERT INTO greetings (message, created_by) VALUES (?, ?)";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, message);
            pstmt.setInt(2, currentUser.getId());
            pstmt.executeUpdate();
            System.out.println("Greeting created successfully.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void updateGreeting() {
        System.out.print("Enter Greeting ID to update: ");
        int id = Integer.parseInt(scanner.nextLine());

        System.out.print("Enter new greeting message: ");
        String message = scanner.nextLine();
        String sql = "UPDATE greetings SET message = ? WHERE id = ?";
        try (Connection conn = DBUtil.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {

                pstmt.setString(1, message);
                pstmt.setInt(2, id);
                int rows = pstmt.executeUpdate();
                if (rows > 0) {
                    conn.commit();
                    System.out.println("Greeting updated successfully.");

                } else {
                    conn.rollback();
                    System.out.println("Greeting ID not found. Transaction rolled back.");
                }

            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }

        } catch (SQLException e) {

            System.err.println("Failed to update greeting transactionally.");
            e.printStackTrace();
        }
    }
    private static void deleteGreeting() {
        System.out.print("Enter Greeting ID to delete: ");
        int id = Integer.parseInt(scanner.nextLine());
        String sql = "DELETE FROM greetings WHERE id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            int rows = pstmt.executeUpdate();
            if (rows > 0) {
                System.out.println("Greeting deleted successfully.");
            } else {
                System.out.println("Greeting ID not found.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
