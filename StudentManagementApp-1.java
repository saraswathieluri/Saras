import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class StudentManagementApp extends JFrame {

    // ===== MySQL DETAILS =====
    static final String URL =
            "jdbc:mysql://localhost:3306/studentdb?useSSL=false&serverTimezone=UTC";
    static final String USER = "root";
    static final String PASSWORD = "your_password"; // Change this

    JTextField nameField, branchField, emailField;
    JTable table;
    DefaultTableModel tableModel;

    public StudentManagementApp() {
        setTitle("Student Management System");
        setSize(800, 550);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel mainPanel = new JPanel(new BorderLayout(15, 15));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel title = new JLabel("Student Management System", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 25));
        mainPanel.add(title, BorderLayout.NORTH);

        // ===== FORM =====
        JPanel formPanel = new JPanel(new GridLayout(4, 2, 10, 10));

        formPanel.add(new JLabel("Name:"));
        nameField = new JTextField();
        formPanel.add(nameField);

        formPanel.add(new JLabel("Branch:"));
        branchField = new JTextField();
        formPanel.add(branchField);

        formPanel.add(new JLabel("Email:"));
        emailField = new JTextField();
        formPanel.add(emailField);

        JButton addButton = new JButton("Add Student");
        JButton deleteButton = new JButton("Delete Selected");

        formPanel.add(addButton);
        formPanel.add(deleteButton);

        mainPanel.add(formPanel, BorderLayout.CENTER);

        // ===== TABLE =====
        tableModel = new DefaultTableModel(
                new String[]{"ID", "Name", "Branch", "Email"}, 0);

        table = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(table);

        mainPanel.add(scrollPane, BorderLayout.SOUTH);

        add(mainPanel);

        // ===== BUTTON ACTIONS =====
        addButton.addActionListener(e -> addStudent());

        deleteButton.addActionListener(e -> deleteStudent());

        // Create table if it does not exist, then load data
        createDatabaseAndTable();
        loadStudents();
    }

    // ===== DATABASE CONNECTION =====
    static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    // ===== CREATE DATABASE/TABLE =====
    void createDatabaseAndTable() {
        try {
            // First connect to MySQL without selecting studentdb
            String serverURL =
                    "jdbc:mysql://localhost:3306/?useSSL=false&serverTimezone=UTC";

            try (Connection con =
                         DriverManager.getConnection(serverURL, USER, PASSWORD);
                 Statement st = con.createStatement()) {

                st.executeUpdate("CREATE DATABASE IF NOT EXISTS studentdb");
            }

            try (Connection con = getConnection();
                 Statement st = con.createStatement()) {

                st.executeUpdate(
                        "CREATE TABLE IF NOT EXISTS student (" +
                        "id INT PRIMARY KEY AUTO_INCREMENT," +
                        "name VARCHAR(50) NOT NULL," +
                        "branch VARCHAR(30) NOT NULL," +
                        "email VARCHAR(100) NOT NULL)");
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(
                    this,
                    "Database connection failed!\n\n" +
                    "Check:\n" +
                    "1. MySQL is running\n" +
                    "2. Username/password are correct\n" +
                    "3. MySQL Connector/J is added",
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    // ===== ADD STUDENT =====
    void addStudent() {

        String name = nameField.getText().trim();
        String branch = branchField.getText().trim();
        String email = emailField.getText().trim();

        if (name.isEmpty() || branch.isEmpty() || email.isEmpty()) {
            JOptionPane.showMessageDialog(
                    this,
                    "Please enter all details.");
            return;
        }

        String sql =
                "INSERT INTO student(name, branch, email) VALUES (?, ?, ?)";

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, name);
            ps.setString(2, branch);
            ps.setString(3, email);

            ps.executeUpdate();

            JOptionPane.showMessageDialog(
                    this,
                    "Student added successfully!");

            nameField.setText("");
            branchField.setText("");
            emailField.setText("");

            loadStudents();

        } catch (Exception e) {
            JOptionPane.showMessageDialog(
                    this,
                    "Error adding student:\n" + e.getMessage());
        }
    }

    // ===== VIEW STUDENTS =====
    void loadStudents() {

        tableModel.setRowCount(0);

        String sql = "SELECT * FROM student ORDER BY id";

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {

                tableModel.addRow(new Object[]{
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("branch"),
                        rs.getString("email")
                });
            }

        } catch (Exception e) {
            System.out.println("Error loading students: "
                    + e.getMessage());
        }
    }

    // ===== DELETE STUDENT =====
    void deleteStudent() {

        int row = table.getSelectedRow();

        if (row == -1) {
            JOptionPane.showMessageDialog(
                    this,
                    "Please select a student first.");
            return;
        }

        int id = (int) tableModel.getValueAt(row, 0);

        int choice = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to delete this student?",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION);

        if (choice != JOptionPane.YES_OPTION) {
            return;
        }

        String sql = "DELETE FROM student WHERE id = ?";

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);
            ps.executeUpdate();

            JOptionPane.showMessageDialog(
                    this,
                    "Student deleted successfully!");

            loadStudents();

        } catch (Exception e) {
            JOptionPane.showMessageDialog(
                    this,
                    "Error deleting student:\n" + e.getMessage());
        }
    }

    // ===== MAIN METHOD =====
    public static void main(String[] args) {

        SwingUtilities.invokeLater(() -> {

            StudentManagementApp app =
                    new StudentManagementApp();

            app.setVisible(true);
        });
    }
}
