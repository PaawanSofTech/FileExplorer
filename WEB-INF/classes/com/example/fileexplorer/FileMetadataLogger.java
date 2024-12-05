package com.example.fileexplorer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class FileMetadataLogger {

    public static void saveFileMetadata(String name, String path, String type, long size, long lastModified) {
        String sql = "INSERT INTO FileMetadata (name, path, type, size, last_modified) VALUES (?, ?, ?, ?, ?)";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, name);
            statement.setString(2, path);
            statement.setString(3, type);
            statement.setLong(4, size);
            statement.setTimestamp(5, new java.sql.Timestamp(lastModified));
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void fetchFileMetadata(String name) {
        String sql = "SELECT * FROM FileMetadata WHERE name LIKE ?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, "%" + name + "%");
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                System.out.println("Name: " + resultSet.getString("name"));
                System.out.println("Path: " + resultSet.getString("path"));
                System.out.println("Type: " + resultSet.getString("type"));
                System.out.println("Size: " + resultSet.getLong("size"));
                System.out.println("Last Modified: " + resultSet.getTimestamp("last_modified"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
