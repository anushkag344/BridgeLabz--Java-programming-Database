package payroll;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import payroll.config.AppConfig;
import payroll.model.Employee;
import payroll.model.User;
import payroll.repository.EmployeeRepository;
import payroll.repository.UserRepository;
import payroll.util.HashUtil;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

public class PayrollApp {

    private static User currentUser;
    private static Scanner scanner = new Scanner(System.in);

    private static UserRepository userRepository;
    private static EmployeeRepository employeeRepository;

    public static void main(String[] args) {

        AnnotationConfigApplicationContext context =
                new AnnotationConfigApplicationContext(AppConfig.class);

        userRepository = context.getBean(UserRepository.class);
        employeeRepository = context.getBean(EmployeeRepository.class);

        System.out.println("=================================");
        System.out.println("   EMPLOYEE PAYROLL SYSTEM");
        System.out.println("=================================");

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

    // =========================
    // ANONYMOUS MENU
    // =========================
    private static void showAnonymousMenu() {

        System.out.println("\n1. Login");
        System.out.println("2. Register");
        System.out.println("3. Exit");
        System.out.print("Enter choice: ");

        int choice = getInt();

        switch (choice) {
            case 1 -> login();
            case 2 -> register();
            case 3 -> System.exit(0);
            default -> System.out.println("Invalid choice");
        }
    }

    // =========================
    // ADMIN MENU
    // =========================
    private static void showAdminMenu() {

        System.out.println("\n--- ADMIN MENU ---");
        System.out.println("1. Add Employee");
        System.out.println("2. View All Employees");
        System.out.println("3. Edit Employee");
        System.out.println("4. Delete Employee");
        System.out.println("5. Department Payroll");
        System.out.println("6. Audit Logs");
        System.out.println("7. Logout");
        System.out.print("Enter choice: ");

        int choice = getInt();

        switch (choice) {
            case 1 -> addEmployee();
            case 2 -> viewAllEmployees();
            case 3 -> editEmployee();
            case 4 -> deleteEmployee();
            case 5 -> getDeptPayroll();
            case 6 -> viewAuditLogs();
            case 7 -> logout();
            default -> System.out.println("Invalid choice");
        }
    }

    // =========================
    // USER MENU
    // =========================
    private static void showUserMenu() {

        System.out.println("\n--- USER MENU ---");
        System.out.println("1. View My Details");
        System.out.println("2. Logout");
        System.out.print("Enter choice: ");

        int choice = getInt();

        switch (choice) {
            case 1 -> viewMyDetails();
            case 2 -> logout();
            default -> System.out.println("Invalid choice");
        }
    }

    // =========================
    // LOGIN
    // =========================
    private static void login() {

        System.out.print("Username: ");
        String username = scanner.nextLine();

        System.out.print("Password: ");
        String password = scanner.nextLine();

        User user = userRepository.findByUsername(username);

        if (user != null &&
                HashUtil.verifyPassword(password, user.getPassword())) {

            currentUser = user;
            System.out.println("Login successful! Welcome " + user.getUsername());

        } else {
            System.out.println("Invalid credentials");
        }
    }

    // =========================
    // REGISTER
    // =========================
    private static void register() {

        System.out.print("Username: ");
        String username = scanner.nextLine();

        System.out.print("Email: ");
        String email = scanner.nextLine();

        System.out.print("Password: ");
        String password = scanner.nextLine();

        if (userRepository.usernameExists(username)) {
            System.out.println("Username already exists");
            return;
        }

        if (userRepository.emailExists(email)) {
            System.out.println("Email already exists");
            return;
        }

        userRepository.registerUser(
                username,
                HashUtil.hashPassword(password),
                email,
                "USER"
        );

        System.out.println("User registered successfully");
    }

    // =========================
    // ADD EMPLOYEE
    // =========================
    private static void addEmployee() {

        Employee emp = new Employee();

        System.out.print("Name: ");
        emp.setName(scanner.nextLine());

        System.out.print("Profile Image: ");
        emp.setProfileImage(scanner.nextLine());

        System.out.print("Gender (Male/Female): ");
        emp.setGender(scanner.nextLine());

        System.out.print("Salary: ");
        emp.setSalary(new BigDecimal(scanner.nextLine()));

        System.out.print("Start Date (YYYY-MM-DD): ");
        emp.setStartDate(LocalDate.parse(scanner.nextLine()));

        System.out.print("Notes: ");
        emp.setNotes(scanner.nextLine());

        emp.setCreatedBy(currentUser.getId());

        System.out.print("Departments (comma separated): ");
        String[] depts = scanner.nextLine().split(",");
        emp.setDepartments(Arrays.asList(depts));

        employeeRepository.addEmployee(emp);

        System.out.println("Employee added successfully!");
    }

    // =========================
    // VIEW ALL EMPLOYEES
    // =========================
    private static void viewAllEmployees() {

        List<Employee> list = employeeRepository.findAll();

        for (Employee e : list) {
            System.out.println(e);
        }
    }

    // =========================
    // EDIT EMPLOYEE
    // =========================
    private static void editEmployee() {

        System.out.print("Employee ID: ");
        int id = getInt();

        System.out.print("New Salary: ");
        BigDecimal salary = new BigDecimal(scanner.nextLine());

        System.out.print("New Notes: ");
        String notes = scanner.nextLine();

        employeeRepository.updateEmployee(id, salary, notes, currentUser.getId());

        System.out.println("Employee updated");
    }

    // =========================
    // DELETE EMPLOYEE
    // =========================
    private static void deleteEmployee() {

        System.out.print("Employee ID: ");
        int id = getInt();

        employeeRepository.deleteEmployee(id);

        System.out.println("Employee deleted");
    }

    // =========================
    // DEPARTMENT PAYROLL
    // =========================
    private static void getDeptPayroll() {

        System.out.print("Department: ");
        String dept = scanner.nextLine();

        BigDecimal total = employeeRepository.getDeptPayroll(dept);

        System.out.println("Total Payroll: " + total);
    }

    // =========================
    // AUDIT LOGS
    // =========================
    private static void viewAuditLogs() {

        List<Map<String, Object>> logs = employeeRepository.findAuditLogs();

        for (Map<String, Object> log : logs) {
            System.out.println(log);
        }
    }

    // =========================
    // USER DETAILS
    // =========================
    private static void viewMyDetails() {

        System.out.println(currentUser);
    }

    // =========================
    // LOGOUT
    // =========================
    private static void logout() {

        currentUser = null;
        System.out.println("Logged out successfully");
    }

    // =========================
    // INPUT HELPERS
    // =========================
    private static int getInt() {

        int val = Integer.parseInt(scanner.nextLine());
        return val;
    }
}