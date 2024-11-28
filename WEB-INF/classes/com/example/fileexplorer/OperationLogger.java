package com.example.fileexplorer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class OperationLogger {
    public static void log(String operationType, String filePath) {
        String sql = "INSERT INTO FileOperationsLog (operation_type, file_name, timestamp) VALUES (?, ?, CURRENT_TIMESTAMP)";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, operationType);
            statement.setString(2, filePath);
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
