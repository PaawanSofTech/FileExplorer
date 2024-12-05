package com.example.fileexplorer.ui;

import java.awt.*;
import java.awt.dnd.*;
import java.awt.datatransfer.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import javax.swing.*;
import javax.swing.table.*;
import javax.swing.tree.*;
import org.json.*;

import com.example.fileexplorer.FileController;
import com.example.fileexplorer.OperationLogger;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public class FileExplorerUI extends JFrame {
    private JTree fileTree;
    private JTable fileTable;
    private DefaultTableModel tableModel;
    private File currentDirectory;
    private boolean isDarkMode = false;
    private JTextArea filePreviewArea;

    public FileExplorerUI() {
        setTitle("Enhanced File Explorer");
        setSize(1000, 700);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        fileChooser.setDialogTitle("Select Root Directory");
        int result = fileChooser.showOpenDialog(null);

        File rootFile;
        if (result == JFileChooser.APPROVE_OPTION) {
            rootFile = fileChooser.getSelectedFile();
        } else {
            rootFile = new File("C:/");
        }

        currentDirectory = rootFile;

        fileTree = new JTree(createTreeNodes(rootFile));
        fileTree.addTreeSelectionListener(e -> {
            DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) fileTree.getLastSelectedPathComponent();
            if (selectedNode != null) {
                String selectedPath = selectedNode.toString();
                currentDirectory = new File(selectedPath);
                updateTable(currentDirectory);
            }
        });

        JScrollPane treeScrollPane = new JScrollPane(fileTree);
        treeScrollPane.setPreferredSize(new Dimension(300, 700));

        // File Table
        tableModel = new DefaultTableModel(new String[] { "Name", "Type", "Size", "Last Modified" }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Disable editing
            }
        };
        
        fileTable = new JTable(tableModel);
        fileTable.setFocusable(false);
        fileTable.setComponentPopupMenu(createContextMenu());

        fileTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                // Check for double-click (2 clicks)
                if (e.getClickCount() == 2) {
                    int selectedRow = fileTable.getSelectedRow();
                    if (selectedRow != -1) {
                        String fileName = (String) tableModel.getValueAt(selectedRow, 0);
                        File selectedFile = new File(currentDirectory, fileName);

                        if (selectedFile.isDirectory()) {
                            currentDirectory = selectedFile;
                            updateTable(currentDirectory);
                        } else if (selectedFile.isFile()) {
                            try {
                                Desktop.getDesktop().open(selectedFile);
                            } catch (IOException ex) {
                                JOptionPane.showMessageDialog(
                                        FileExplorerUI.this,
                                        "Unable to open file: " + ex.getMessage(),
                                        "Error",
                                        JOptionPane.ERROR_MESSAGE);
                            }
                        }
                    }
                }
            }
        });

        JScrollPane tableScrollPane = new JScrollPane(fileTable);

        // File Preview
        filePreviewArea = new JTextArea();
        filePreviewArea.setEditable(false);
        filePreviewArea.setText("Select a file to preview its contents.");
        JScrollPane previewScrollPane = new JScrollPane(filePreviewArea);
        previewScrollPane.setPreferredSize(new Dimension(300, 700));

        // Split Pane
        JSplitPane rightSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, tableScrollPane, previewScrollPane);
        JSplitPane mainSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, treeScrollPane, rightSplitPane);
        add(mainSplitPane, BorderLayout.CENTER);

        // Button Panel
        JPanel buttonPanel = new JPanel();
        JButton createButton = new JButton("Create");
        JButton deleteButton = new JButton("Delete");
        JButton renameButton = new JButton("Rename");
        JButton refreshButton = new JButton("Refresh");
        JButton uploadButton = new JButton("Upload File");
        JButton themeButton = new JButton("Toggle Theme");

        buttonPanel.add(createButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(renameButton);
        buttonPanel.add(refreshButton);
        buttonPanel.add(uploadButton);
        buttonPanel.add(themeButton);

        add(buttonPanel, BorderLayout.SOUTH);

        // Search Panel
        JTextField searchField = new JTextField(20);
        JButton searchButton = new JButton("Search");
        JPanel searchPanel = new JPanel();
        searchPanel.add(searchField);
        searchPanel.add(searchButton);
        add(searchPanel, BorderLayout.NORTH);

        // Event Listeners
        createButton.addActionListener(e -> createFileOrFolder());
        deleteButton.addActionListener(e -> deleteSelectedFile());
        renameButton.addActionListener(e -> renameSelectedFile());
        refreshButton.addActionListener(e -> refreshDirectory());
        uploadButton.addActionListener(e -> uploadFile());
        themeButton.addActionListener(e -> toggleTheme());
        searchButton.addActionListener(e -> searchFiles(searchField.getText()));

        fileTable.getSelectionModel().addListSelectionListener(e -> previewFile());

        // Drag-and-Drop Support
        fileTable.setDropTarget(new DropTarget() {
            @Override
            public synchronized void drop(DropTargetDropEvent dtde) {
                try {
                    dtde.acceptDrop(DnDConstants.ACTION_COPY);
                    java.util.List<File> droppedFiles = (java.util.List<File>) dtde.getTransferable()
                            .getTransferData(DataFlavor.javaFileListFlavor);
                    for (File file : droppedFiles) {
                        copyFileToCurrentDirectory(file);
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(null, "Error uploading file: " + ex.getMessage());
                }
            }
        });

        updateTable(rootFile);

        setVisible(true);
    }

    private DefaultMutableTreeNode createTreeNodes(File rootFile) {
        DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode(rootFile.getAbsolutePath());
        File[] files = rootFile.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    rootNode.add(new DefaultMutableTreeNode(file.getAbsolutePath()));
                }
            }
        }
        return rootNode;
    }

    private void updateTable(File directory) {
        tableModel.setRowCount(0);
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                tableModel.addRow(new Object[] {
                        file.getName(),
                        file.isDirectory() ? "Directory" : "File",
                        file.isFile() ? file.length() : "",
                        new java.util.Date(file.lastModified())
                });
            }
        }
    }

    private void refreshDirectory() {
        updateTable(currentDirectory);
    }

    private void createFileOrFolder() {
        String[] options = { "File", "Folder" };
        int choice = JOptionPane.showOptionDialog(
                this,
                "What do you want to create?",
                "Create File or Folder",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[0]
        );
    
        if (choice == JOptionPane.CLOSED_OPTION) {
            return; // User closed the dialog, do nothing
        }
    
        String name = JOptionPane.showInputDialog("Enter name for new " + options[choice] + ":");
        if (name != null && !name.trim().isEmpty()) {
            File newFile = new File(currentDirectory, name.trim());
            boolean success = false;
    
            if (choice == 0) { // Create File
                try {
                    success = newFile.createNewFile();
                } catch (IOException e) {
                    JOptionPane.showMessageDialog(this, "Error creating file: " + e.getMessage(), "Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            } else if (choice == 1) { // Create Folder
                success = newFile.mkdir();
            }
    
            if (success) {
                updateTable(currentDirectory);
                JOptionPane.showMessageDialog(this, options[choice] + " created successfully!");
                
                // Log the operation
                OperationLogger.log("Create " + options[choice], newFile.getAbsolutePath());
            } else {
                JOptionPane.showMessageDialog(this, "Failed to create " + options[choice] + "!", "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(this, "Invalid name entered!", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    

    private void deleteSelectedFile() {
        int selectedRow = fileTable.getSelectedRow();
        if (selectedRow != -1) {
            String fileName = (String) tableModel.getValueAt(selectedRow, 0);
            File fileToDelete = new File(currentDirectory, fileName);
            boolean success = fileToDelete.delete();
    
            if (success) {
                updateTable(currentDirectory);
                JOptionPane.showMessageDialog(this, "File/Folder deleted successfully!");
    
                // Log delete operation
                OperationLogger.log("Delete", fileToDelete.getAbsolutePath());
            } else {
                JOptionPane.showMessageDialog(this, "Failed to delete file/folder!");
            }
        } else {
            JOptionPane.showMessageDialog(this, "No file/folder selected!");
        }
    }
    

    private void renameSelectedFile() {
    int selectedRow = fileTable.getSelectedRow();
    if (selectedRow != -1) {
        String oldName = (String) tableModel.getValueAt(selectedRow, 0);
        File oldFile = new File(currentDirectory, oldName);

        String newName = JOptionPane.showInputDialog(this, "Enter new name (including extension):", oldName);

        if (newName != null && !newName.trim().isEmpty()) {
            File newFile = new File(currentDirectory, newName.trim());
            boolean success = oldFile.renameTo(newFile);

            if (success) {
                updateTable(currentDirectory);
                JOptionPane.showMessageDialog(this, "File renamed successfully!");

                // Log rename operation
                OperationLogger.log("Rename", oldName + " -> " + newName);
            } else {
                JOptionPane.showMessageDialog(this, "Failed to rename file!");
            }
        }
    } else {
        JOptionPane.showMessageDialog(this, "No file/folder selected!");
    }
}


    private void uploadFile() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            copyFileToCurrentDirectory(file);
        }
    }

    private void copyFileToCurrentDirectory(File file) {
        try {
            File target = new File(currentDirectory, file.getName());
            Files.copy(file.toPath(), target.toPath());
            updateTable(currentDirectory);
    
            JOptionPane.showMessageDialog(this, "File uploaded successfully!");
    
            // Log upload operation
           OperationLogger.log("Upload", target.getAbsolutePath());
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Error uploading file: " + ex.getMessage(), "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
    

    // Get the extension of a file
    private String getFileExtension(File file) {
        String name = file.getName();
        int lastDot = name.lastIndexOf('.');
        if (lastDot > 0 && lastDot < name.length() - 1) {
            return name.substring(lastDot + 1).toLowerCase();
        }
        return ""; // No extension
    }

    private boolean isPreviewSupported(String extension) {
        // List of supported extensions
        Set<String> supportedExtensions = new HashSet<>(Arrays.asList(
                "txt", "csv", "log", "java", "html", "css", "js", "py", "cpp", "c", "h", "xml", "json"));
        return supportedExtensions.contains(extension);
    }

    private void previewFile() {
        int selectedRow = fileTable.getSelectedRow();
        if (selectedRow != -1) {
            String fileName = (String) tableModel.getValueAt(selectedRow, 0);
            File selectedFile = new File(currentDirectory, fileName);

            if (selectedFile.isFile()) {
                String extension = getFileExtension(selectedFile);

                // Check if the file type is supported for preview
                if (isPreviewSupported(extension)) {
                    try (BufferedReader reader = new BufferedReader(new FileReader(selectedFile))) {
                        filePreviewArea.setText(""); // Clear previous preview
                        String line;
                        while ((line = reader.readLine()) != null) {
                            filePreviewArea.append(line + "\n");
                        }
                    } catch (IOException e) {
                        filePreviewArea.setText("Error reading the file.");
                    }
                } else {
                    filePreviewArea.setText("Preview not available for this file type.");
                }
            } else {
                filePreviewArea.setText("Select a valid file to preview.");
            }
        }
    }

    private JPopupMenu createContextMenu() {
        JPopupMenu menu = new JPopupMenu();
        JMenuItem openItem = new JMenuItem("Open");
        JMenuItem propertiesItem = new JMenuItem("Properties");

        openItem.addActionListener(e -> openSelectedFile());
        propertiesItem.addActionListener(e -> showFileProperties());

        menu.add(openItem);
        menu.add(propertiesItem);
        return menu;
    }

    private void openSelectedFile() {
        int selectedRow = fileTable.getSelectedRow();
        if (selectedRow != -1) {
            String fileName = (String) tableModel.getValueAt(selectedRow, 0);
            File selectedFile = new File(currentDirectory, fileName);
            if (selectedFile.exists()) {
                try {
                    Desktop.getDesktop().open(selectedFile);
                } catch (IOException e) {
                    JOptionPane.showMessageDialog(this, "Unable to open file: " + e.getMessage(), "Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }

    private void showFileProperties() {
        int selectedRow = fileTable.getSelectedRow();
        if (selectedRow != -1) {
            String fileName = (String) tableModel.getValueAt(selectedRow, 0);
            File selectedFile = new File(currentDirectory, fileName);
            if (selectedFile.exists()) {
                JOptionPane.showMessageDialog(this,
                        "Name: " + selectedFile.getName() + "\n" +
                                "Path: " + selectedFile.getAbsolutePath() + "\n" +
                                "Size: " + selectedFile.length() + " bytes\n" +
                                "Last Modified: " + new Date(selectedFile.lastModified()),
                        "File Properties", JOptionPane.INFORMATION_MESSAGE);
            }
        }
    }

    private void toggleTheme() {
        isDarkMode = !isDarkMode;
        UIManager.put("control", isDarkMode ? Color.DARK_GRAY : Color.LIGHT_GRAY);
        UIManager.put("text", isDarkMode ? Color.WHITE : Color.BLACK);
        SwingUtilities.updateComponentTreeUI(this);
    }

    private void searchFiles(String searchTerm) {
        tableModel.setRowCount(0);
        File[] files = currentDirectory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.getName().toLowerCase().contains(searchTerm.toLowerCase())) {
                    tableModel.addRow(new Object[] {
                            file.getName(),
                            file.isDirectory() ? "Directory" : "File",
                            file.isFile() ? file.length() : "",
                            new java.util.Date(file.lastModified())
                    });
                }
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(FileExplorerUI::new);
    }
}
