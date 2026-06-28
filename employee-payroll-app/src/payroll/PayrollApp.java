package payroll;

import payroll.model.User;
import payroll.util.DBUtil;
import payroll.util.HashUtil;

import java.sql.*;
import java.time.LocalDate;
import java.util.Scanner;

public class PayrollApp {

    private static User currentUser = null;
    private static final Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {

        System.out.println("===== EMPLOYEE PAYROLL SYSTEM =====");

        while (true) {

            if (currentUser == null) {
                showAnonymousMenu();
            } else if ("ADMIN".equalsIgnoreCase(currentUser.getRole())) {
                showAdminMenu();
            } else {
                showUserMenu();
            }
        }
    }

    // ---------- MENU ----------
    private static void showAnonymousMenu() {
        System.out.println("\n1. Register");
        System.out.println("2. Login");
        System.out.println("3. Exit");
        System.out.print("Choice: ");

        int ch = scanner.nextInt();

        switch (ch) {
            case 1 -> register();
            case 2 -> login();
            case 3 -> System.exit(0);
        }
    }

    private static void showAdminMenu() {
        System.out.println("\n--- ADMIN MENU ---");
        System.out.println("1. Add Employee");
        System.out.println("2. View Employees");
        System.out.println("3. Update Employee");
        System.out.println("4. Delete Employee");
        System.out.println("5. Department Payroll");
        System.out.println("6. Logout");
        System.out.print("Choice: ");

        int ch = scanner.nextInt();

        switch (ch) {
            case 1 -> addEmployee();
            case 2 -> viewEmployees();
            case 3 -> updateEmployee();
            case 4 -> deleteEmployee();
            case 5 -> {
                System.out.print("Dept: ");
                scanner.nextLine();
                departmentPayroll(scanner.nextLine());
            }
            case 6 -> logout();
        }
    }

    private static void showUserMenu() {
        System.out.println("\n--- USER MENU ---");
        System.out.println("1. View Employees");
        System.out.println("2. Department Payroll");
        System.out.println("3. Logout");
        System.out.print("Choice: ");

        int ch = scanner.nextInt();

        switch (ch) {
            case 1 -> viewEmployees();
            case 2 -> {
                System.out.print("Dept: ");
                scanner.nextLine();
                departmentPayroll(scanner.nextLine());
            }
            case 3 -> logout();
        }
    }

    // ---------- REGISTER ----------
    private static void register() {

        try (Connection conn = DBUtil.getConnection()) {

            System.out.print("Username: ");
            String username = scanner.next();

            System.out.print("Password: ");
            String password = scanner.next();

            System.out.print("Email: ");
            String email = scanner.next();

            System.out.print("Role (ADMIN/USER): ");
            String role = scanner.next();

            password = HashUtil.sha256(password);

            String sql = "INSERT INTO users(username, password, email, role) VALUES (?, ?, ?, ?)";

            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, username);
            ps.setString(2, password);
            ps.setString(3, email);
            ps.setString(4, role);

            ps.executeUpdate();

            System.out.println("User registered!");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ---------- LOGIN ----------
    private static void login() {

        try (Connection conn = DBUtil.getConnection()) {

            System.out.print("Username: ");
            String username = scanner.next();

            System.out.print("Password: ");
            String password = scanner.next();

            password = HashUtil.sha256(password);

            String sql = "SELECT * FROM users WHERE username=? AND password=?";

            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, username);
            ps.setString(2, password);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                currentUser = new User();
                currentUser.setId(rs.getInt("id"));
                currentUser.setUsername(rs.getString("username"));
                currentUser.setRole(rs.getString("role"));

                System.out.println("Login successful!");
            } else {
                System.out.println("Invalid credentials!");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void logout() {
        currentUser = null;
        System.out.println("Logged out!");
    }

    // ---------- EMPLOYEE METHODS ----------

    // employees table needs: name, profile_image, gender, salary, start_date,
    // notes, created_by. Department(s) go in the separate employee_departments
    // table (one employee can have multiple departments), so we insert the
    // employee first, then insert one row per department typed in (comma separated).
    private static void addEmployee() {

        try (Connection conn = DBUtil.getConnection()) {

            conn.setAutoCommit(false);

            System.out.print("Name: ");
            String name = scanner.next();

            System.out.print("Profile image (e.g. ellipse-1.png): ");
            String profileImage = scanner.next();

            System.out.print("Gender (Male/Female): ");
            String gender = scanner.next();

            System.out.print("Salary: ");
            double salary = scanner.nextDouble();

            System.out.print("Departments (comma separated, e.g. Sales,HR): ");
            scanner.nextLine();
            String deptInput = scanner.nextLine();

            System.out.print("Notes (optional, press Enter to skip): ");
            String notes = scanner.nextLine();
            if (notes.isBlank()) {
                notes = null;
            }

            String empSql = "INSERT INTO employees(name, profile_image, gender, salary, start_date, notes, created_by) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?) RETURNING id";

            PreparedStatement empPs = conn.prepareStatement(empSql);
            empPs.setString(1, name);
            empPs.setString(2, profileImage);
            empPs.setString(3, gender);
            empPs.setDouble(4, salary);
            empPs.setDate(5, Date.valueOf(LocalDate.now()));
            empPs.setString(6, notes);
            empPs.setInt(7, currentUser != null ? currentUser.getId() : 1);

            ResultSet rs = empPs.executeQuery();

            int newEmployeeId;
            if (rs.next()) {
                newEmployeeId = rs.getInt(1);
            } else {
                throw new SQLException("Failed to retrieve new employee id");
            }

            String deptSql = "INSERT INTO employee_departments(employee_id, department) VALUES (?, ?)";
            PreparedStatement deptPs = conn.prepareStatement(deptSql);

            for (String dept : deptInput.split(",")) {
                dept = dept.trim();
                if (dept.isEmpty()) continue;
                deptPs.setInt(1, newEmployeeId);
                deptPs.setString(2, dept);
                deptPs.executeUpdate();
            }

            conn.commit();

            System.out.println("Employee added!");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Joins employees to their departments (string-aggregated) for display.
    private static void viewEmployees() {

        try (Connection conn = DBUtil.getConnection()) {

            String sql = "SELECT e.id, e.name, e.gender, e.salary, e.start_date, " +
                    "STRING_AGG(ed.department, ', ') AS departments " +
                    "FROM employees e " +
                    "LEFT JOIN employee_departments ed ON e.id = ed.employee_id " +
                    "GROUP BY e.id, e.name, e.gender, e.salary, e.start_date " +
                    "ORDER BY e.id";

            ResultSet rs = conn.createStatement().executeQuery(sql);

            System.out.println("ID | Name | Gender | Salary | Start Date | Departments");

            while (rs.next()) {
                System.out.println(rs.getInt("id") + " | " +
                        rs.getString("name") + " | " +
                        rs.getString("gender") + " | " +
                        rs.getDouble("salary") + " | " +
                        rs.getDate("start_date") + " | " +
                        rs.getString("departments"));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Updates core employee fields. Department list is replaced (delete + reinsert)
    // since employee_departments is a separate junction table.
    private static void updateEmployee() {

        try (Connection conn = DBUtil.getConnection()) {

            conn.setAutoCommit(false);

            System.out.print("ID: ");
            int id = scanner.nextInt();

            System.out.print("Name: ");
            String name = scanner.next();

            System.out.print("Gender (Male/Female): ");
            String gender = scanner.next();

            System.out.print("Salary: ");
            double salary = scanner.nextDouble();

            System.out.print("Departments (comma separated, e.g. Sales,HR): ");
            scanner.nextLine();
            String deptInput = scanner.nextLine();

            String sql = "UPDATE employees SET name=?, gender=?, salary=? WHERE id=?";

            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, name);
            ps.setString(2, gender);
            ps.setDouble(3, salary);
            ps.setInt(4, id);

            int rowsAffected = ps.executeUpdate();

            if (rowsAffected == 0) {
                System.out.println("No employee found with that ID.");
                conn.rollback();
                return;
            }

            PreparedStatement deletePs = conn.prepareStatement(
                    "DELETE FROM employee_departments WHERE employee_id=?");
            deletePs.setInt(1, id);
            deletePs.executeUpdate();

            PreparedStatement deptPs = conn.prepareStatement(
                    "INSERT INTO employee_departments(employee_id, department) VALUES (?, ?)");

            for (String dept : deptInput.split(",")) {
                dept = dept.trim();
                if (dept.isEmpty()) continue;
                deptPs.setInt(1, id);
                deptPs.setString(2, dept);
                deptPs.executeUpdate();
            }

            conn.commit();

            System.out.println("Updated!");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // employee_departments and payroll_audit rows are cleaned up automatically:
    // employee_departments has ON DELETE CASCADE on employee_id.
    private static void deleteEmployee() {

        try (Connection conn = DBUtil.getConnection()) {

            System.out.print("ID: ");
            int id = scanner.nextInt();

            PreparedStatement ps = conn.prepareStatement("DELETE FROM employees WHERE id=?");
            ps.setInt(1, id);

            int rowsAffected = ps.executeUpdate();

            if (rowsAffected > 0) {
                System.out.println("Deleted!");
            } else {
                System.out.println("No employee found with that ID.");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Uses the existing get_total_payroll_by_dept(p_dept) stored function
    // from the schema instead of a raw query, since the schema already
    // joins employees -> employee_departments for us.
    private static void departmentPayroll(String dept) {

        try (Connection conn = DBUtil.getConnection()) {

            PreparedStatement ps = conn.prepareStatement(
                    "SELECT get_total_payroll_by_dept(?)");

            ps.setString(1, dept);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                System.out.println("Total Payroll for '" + dept + "': " + rs.getDouble(1));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}