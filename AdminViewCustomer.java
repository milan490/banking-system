package Admin;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class AdminViewCustomer extends JPanel {
    private JTable customerTable;
    private DefaultTableModel tableModel;
    private Connection conn;

    public AdminViewCustomer() {
        setLayout(new BorderLayout());

        // Table columns
        String[] columns = {"Customer ID", "Name", "Account No", "Account Type", "Created Date", "Balance", "Status"};
        tableModel = new DefaultTableModel(columns, 0);
        customerTable = new JTable(tableModel);

        // Add table to scroll pane and then to panel
        JScrollPane scrollPane = new JScrollPane(customerTable);
        add(scrollPane, BorderLayout.CENTER);

        // Connect to database and load customer data
        connectDatabase();
        loadCustomers();
    }

    private void connectDatabase() {
        try {
            String url = "jdbc:mysql://localhost:3306/bankingsystem";
            String user = "root";
            String password = "";  // Use your DB password if set
            conn = DriverManager.getConnection(url, user, password);
            System.out.println("Database Connected Successfully!");
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Database Connection Failed.");
        }
    }

    private void loadCustomers() {
        try {
            String sql = "SELECT * FROM accounts";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            // Clear existing rows
            tableModel.setRowCount(0);

            // Add each customer to the table
            while (rs.next()) {
                Object[] row = {
                        rs.getInt("customer_id"),
                        rs.getString("name"),
                        rs.getString("account_no"),
                        rs.getString("account_type"),
                        rs.getDate("created_date"),
                        rs.getDouble("balance"),
                        rs.getString("status")
                };
                tableModel.addRow(row);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to load customer data.");
        }
    }
}

