package payroll.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBUtil {

    // Database URL
    private static final String URL =
            "jdbc:postgresql://localhost:5432/employee_payroll_db";

    // PostgreSQL Username
    private static final String USER = "postgres";

    // PostgreSQL Password
    private static final String PASSWORD = "ABCD";

    // Method to get Database Connection
    public static Connection getConnection() {

        Connection connection = null;

        try {

            // Load PostgreSQL Driver
            Class.forName("org.postgresql.Driver");

            // Create Connection
            connection = DriverManager.getConnection(URL, USER, PASSWORD);

            System.out.println("Database Connected Successfully.");

        } catch (ClassNotFoundException e) {

            System.out.println("PostgreSQL JDBC Driver Not Found!");
            e.printStackTrace();

        } catch (SQLException e) {

            System.out.println("Database Connection Failed!");
            e.printStackTrace();

        }

        return connection;
    }

    // Close Connection Method
    public static void closeConnection(Connection connection) {

        if (connection != null) {

            try {
                connection.close();
                System.out.println("Database Connection Closed.");

            } catch (SQLException e) {
                e.printStackTrace();
            }

        }
    }
}