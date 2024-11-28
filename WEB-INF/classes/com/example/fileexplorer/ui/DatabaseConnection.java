package com.example.fileexplorer.ui;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Connection;
import java.sql.SQLException;

public class DatabaseConnection {
    private static final String URL = "jdbc:mysql://localhost:3306/FileExplorerDB"; // Replace with your database URL
    private static final String USER = "root"; // Replace with your MySQL username
    private static final String PASSWORD = ""; // Replace with your MySQL password

    public static Connection getConnection() throws SQLException {
        try {
            // Load the MySQL JDBC Driver
            Class.forName("com.mysql.cj.jdbc.Driver"); // Ensure the MySQL Connector/J driver is in the classpath
        } catch (ClassNotFoundException e) {
            throw new SQLException("MySQL Driver not found. Ensure you have added MySQL Connector/J to your classpath.", e);
        }
        // Return a connection
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
    public static void main(String[] args) {
        try {
            // Test the database connection
            Connection connection = DatabaseConnection.getConnection();
            if (connection != null) {
                System.out.println("Connection established successfully!");
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}

