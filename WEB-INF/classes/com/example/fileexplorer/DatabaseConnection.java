package com.example.fileexplorer;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    private static final String URL = "jdbc:mysql://localhost:3306/FileExplorerDB"; // Update with your database URL
    private static final String USER = "root"; // Replace with your MySQL username
    private static final String PASSWORD = ""; // Replace with your MySQL password

    public static Connection getConnection() throws SQLException {
        try {
            // Load the MySQL Connector/J 9.1.0 driver
            Class.forName("com.mysql.cj.jdbc.Driver"); // Class name remains the same for 9.1.0
        } catch (ClassNotFoundException e) {
            throw new SQLException("MySQL Driver not found", e);
        }

        // Establish the connection
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
