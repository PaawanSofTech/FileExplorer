package com.example.fileexplorer;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;

public class FileController extends HttpServlet {

    public static String listFilesWithDetails(String path) {
        File directory = new File(path);
        if (!directory.isDirectory()) {
            return "[]";
        }

        File[] files = directory.listFiles();
        if (files == null) {
            return "[]";
        }

        StringBuilder json = new StringBuilder("[");
        for (File file : files) {
            json.append("{");
            json.append("\"name\":\"").append(file.getName()).append("\",");
            json.append("\"type\":\"").append(file.isDirectory() ? "directory" : "file").append("\",");
            json.append("\"size\":").append(file.isFile() ? file.length() : 0).append(",");
            json.append("\"lastModified\":\"").append(new Date(file.lastModified())).append("\"");
            json.append("},");
        }
        if (json.length() > 1) {
            json.setLength(json.length() - 1); // Remove trailing comma
        }
        json.append("]");
        return json.toString();
    }

    @Override
protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    String path = req.getParameter("path");

    if (path != null) {
        path = java.net.URLDecoder.decode(path, "UTF-8");
    }

    resp.setContentType("application/json");
    PrintWriter out = resp.getWriter();

    if (path == null || path.isEmpty()) {
        resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        out.write("{\"error\": \"Path parameter is required\"}");
        return;
    }

    String json = listFilesWithDetails(path);
    out.write(json);
}


    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String operation = req.getParameter("operation");
        String path = req.getParameter("path");

        if (path != null) {
            path = java.net.URLDecoder.decode(path, "UTF-8");
        }

        boolean result = false;

        resp.setContentType("application/json");
        PrintWriter out = resp.getWriter();

        if (operation == null || path == null || path.isEmpty()) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.write("{\"error\": \"Operation and path parameters are required\"}");
            return;
        }

        try {
            if ("create".equalsIgnoreCase(operation)) {
                boolean isDirectory = Boolean.parseBoolean(req.getParameter("isDirectory"));
                result = FileOperations.createFile(path, isDirectory);
            } else if ("delete".equalsIgnoreCase(operation)) {
                result = FileOperations.deleteFile(path);
            } else {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.write("{\"error\": \"Unsupported operation: " + operation + "\"}");
                return;
            }
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.write("{\"error\": \"An error occurred: " + e.getMessage() + "\"}");
            return;
        }

        if (result) {
            out.write("{\"status\": \"success\"}");
        } else {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.write("{\"status\": \"error\"}");
        }
    }

}
