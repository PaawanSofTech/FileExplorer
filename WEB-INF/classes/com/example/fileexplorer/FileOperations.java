package com.example.fileexplorer;

import java.io.File;

public class FileOperations {

    public static String[] listFiles(String path) {
        File directory = new File(path);
        if (directory.isDirectory()) {
            return directory.list();
        }
        return null;
    }

    public static boolean createFile(String path, boolean isDirectory) {
        try {
            File file = new File(path);
            if (isDirectory) {
                return file.mkdir();
            } else {
                return file.createNewFile();
            }
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean deleteFile(String path) {
        File file = new File(path);
        return file.delete();
    }
}
