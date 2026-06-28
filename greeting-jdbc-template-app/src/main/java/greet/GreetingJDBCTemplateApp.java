package greet;

import greet.config.AppConfig;
import greet.model.Greeting;
import greet.model.User;
import greet.repository.GreetingRepository;
import greet.repository.UserRepository;
import greet.util.HashUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

@Component
public class GreetingJDBCTemplateApp {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GreetingRepository greetingRepository;

    @Autowired
    private TransactionTemplate transactionTemplate;

    private static final Scanner scanner = new Scanner(System.in);

    private User currentUser = null;

    public static void main(String[] args) {

        loadEnvironmentVariables();

        AnnotationConfigApplicationContext context =
                new AnnotationConfigApplicationContext(AppConfig.class);

        GreetingJDBCTemplateApp app =
                context.getBean(GreetingJDBCTemplateApp.class);

        System.out.println("===== Greeting Spring JdbcTemplate Console =====");

        app.runLoop();

        context.close();
    }

    private static void loadEnvironmentVariables() {

        try {

            if (Files.exists(Paths.get(".env"))) {

                List<String> lines = Files.readAllLines(Paths.get(".env"));

                for (String line : lines) {

                    line = line.trim();

                    if (!line.isEmpty() && !line.startsWith("#")) {

                        String[] parts = line.split("=", 2);

                        if (parts.length == 2) {

                            System.setProperty(parts[0].trim(), parts[1].trim());

                        }
                    }
                }

            } else {

                System.out.println(".env file not found.");

            }

        } catch (IOException e) {

            System.out.println("Unable to read .env");

        }
    }

    private void runLoop() {

        while (true) {

            if (currentUser == null) {

                showAnonymousMenu();

            } else {

                showUserMenu();

            }

        }

    }

    private void showAnonymousMenu() {

        System.out.println();
        System.out.println("1. Login");
        System.out.println("2. Register");
        System.out.println("3. Exit");

        System.out.print("Select Option : ");

        String choice = scanner.nextLine();

        switch (choice) {

            case "1":
                login();
                break;

            case "2":
                register();
                break;

            case "3":
                System.out.println("Good Bye");
                System.exit(0);

            default:
                System.out.println("Invalid Choice");

        }

    }

    private void showUserMenu() {

        System.out.println("\nLogged In : "
                + currentUser.getUsername()
                + " ("
                + currentUser.getRole()
                + ")");

        System.out.println("1. View Greetings");
        System.out.println("2. View Audit Logs");
        System.out.println("3. Greeting Count");

        if (currentUser.getRole().equals("ADMIN")) {

            System.out.println("4. Create Greeting");
            System.out.println("5. Update Greeting");
            System.out.println("6. Delete Greeting");

        }

        System.out.println("7. Logout");

        System.out.print("Select Option : ");

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
                else
                    System.out.println("Access Denied");
                break;

            case "5":
                if (isAdmin())
                    updateGreeting();
                else
                    System.out.println("Access Denied");
                break;

            case "6":
                if (isAdmin())
                    deleteGreeting();
                else
                    System.out.println("Access Denied");
                break;

            case "7":
                logout();
                break;

            default:
                System.out.println("Invalid Choice");

        }

    }

    private boolean isAdmin() {

        return currentUser != null &&
                currentUser.getRole().equals("ADMIN");

    }
    private void register() {

        System.out.print("Enter username: ");
        String username = scanner.nextLine();

        System.out.print("Enter password: ");
        String password = scanner.nextLine();

        System.out.print("Enter email: ");
        String email = scanner.nextLine();

        System.out.print("Enter role (ADMIN/USER): ");
        String role = scanner.nextLine().toUpperCase();

        if (!role.equals("ADMIN") && !role.equals("USER")) {
            System.out.println("Invalid Role.");
            return;
        }

        User user = new User(
                0,
                username,
                HashUtil.hashPassword(password),
                email,
                role
        );

        try {

            userRepository.save(user);

            System.out.println("Registration Successful. Please Login.");

        } catch (Exception e) {

            System.out.println("Registration Failed.");

        }
    }

    private void login() {

        System.out.print("Enter Username: ");
        String username = scanner.nextLine();

        System.out.print("Enter Password: ");
        String password = scanner.nextLine();

        User user = userRepository.findByUsername(username);

        if (user != null) {

            String hashedPassword =
                    HashUtil.hashPassword(password);

            if (user.getPassword().equals(hashedPassword)) {

                currentUser = user;

                System.out.println(
                        "Login Successful. Welcome "
                                + currentUser.getUsername()
                );

            } else {

                System.out.println("Incorrect Password.");

            }

        } else {

            System.out.println("Username Not Found.");

        }

    }

    private void logout() {

        currentUser = null;

        System.out.println("Logged Out Successfully.");

    }

    private void viewGreetings() {

        List<Greeting> greetings =
                greetingRepository.findAll();

        System.out.println("\n===== GREETINGS =====");

        for (Greeting greeting : greetings) {

            System.out.println(greeting);

        }

        if (greetings.isEmpty()) {

            System.out.println("No Greetings Found.");

        }

    }

    private void viewAuditLogs() {

        List<Map<String, Object>> logs =
                greetingRepository.getAuditLogs();

        System.out.println("\n===== AUDIT LOGS =====");

        System.out.printf(
                "%-5s %-10s %-10s %-25s %-25s %-15s%n",
                "ID",
                "Greeting",
                "Action",
                "Old Message",
                "New Message",
                "Changed By"
        );

        for (Map<String, Object> log : logs) {

            System.out.printf(
                    "%-5s %-10s %-10s %-25s %-25s %-15s%n",
                    log.get("id"),
                    log.get("greeting_id"),
                    log.get("action_type"),
                    log.get("old_message"),
                    log.get("new_message"),
                    log.get("changed_by")
            );

        }

    }

    private void viewUserGreetingCount() {

        System.out.print("Enter Username: ");

        String username = scanner.nextLine();

        try {

            int count =
                    greetingRepository.getGreetingCountForUser(username);

            System.out.println(
                    "User '" +
                            username +
                            "' has created "
                            + count +
                            " greeting(s)."
            );

        } catch (Exception e) {

            System.out.println("Unable To Fetch Count.");

        }

    }

    private void createGreeting() {

        System.out.print("Enter Greeting: ");

        String message = scanner.nextLine();

        greetingRepository.save(
                message,
                currentUser.getId()
        );

        System.out.println("Greeting Created Successfully.");

    }

    private void updateGreeting() {

        System.out.print("Enter Greeting ID: ");

        int id =
                Integer.parseInt(scanner.nextLine());

        System.out.print("Enter New Message: ");

        String message =
                scanner.nextLine();

        transactionTemplate.execute(status -> {

            int rows =
                    greetingRepository.update(id, message);

            if (rows > 0) {

                System.out.println("Greeting Updated.");

            } else {

                status.setRollbackOnly();

                System.out.println(
                        "Greeting ID Not Found. Transaction Rolled Back."
                );

            }

            return null;

        });

    }

    private void deleteGreeting() {

        System.out.print("Enter Greeting ID: ");

        int id =
                Integer.parseInt(scanner.nextLine());

        int rows =
                greetingRepository.delete(id);

        if (rows > 0) {

            System.out.println("Greeting Deleted.");

        } else {

            System.out.println("Greeting ID Not Found.");

        }

    }

}