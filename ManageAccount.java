package Admin;

import javax.swing.*;
import java.awt.event.*;
import java.sql.*;
import Connection.DatabaseConnection;

public class ManageAccount extends JFrame {
    JTable pendingTable, approvedTable;
    JButton approveBtn;
    JTabbedPane tabbedPane;

    public ManageAccount() {
        setTitle("Manage Accounts");
        setSize(600, 450);
        setLayout(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        tabbedPane = new JTabbedPane();
        tabbedPane.setBounds(20, 20, 550, 290);
        add(tabbedPane);

        approveBtn = new JButton("Approve Selected");
        approveBtn.setBounds(200, 320, 180, 30);
        add(approveBtn);

        loadPendingAccounts();  // Load pending accounts in the first tab
        loadApprovedAccounts(); // Load approved accounts in the second tab

        approveBtn.addActionListener(e -> approveSelected());

        setVisible(true);
    }

    private void loadPendingAccounts() {
        try (Connection con = DatabaseConnection.getConnection()) {
            String sql = "SELECT customer_id, name, account_no, account_type, created_date, balance, status FROM customers WHERE status='Pending'";
            Statement st = con.createStatement();
            ResultSet rs = st.executeQuery(sql);

            String[] cols = {"ID", "Name", "Account No", "Type", "Date", "Balance", "Status"};
            Object[][] data = new Object[100][7];
            int i = 0;

            while (rs.next()) {
                data[i][0] = rs.getInt("customer_id");
                data[i][1] = rs.getString("name");
                data[i][2] = rs.getString("account_no");
                data[i][3] = rs.getString("account_type");
                data[i][4] = rs.getDate("created_date");
                data[i][5] = rs.getDouble("balance");
                data[i][6] = rs.getString("status");
                i++;
            }

            pendingTable = new JTable(data, cols);
            JScrollPane sp = new JScrollPane(pendingTable);
            tabbedPane.addTab("Pending Accounts", sp);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void loadApprovedAccounts() {
        try (Connection con = DatabaseConnection.getConnection()) {
            String sql = "SELECT customer_id, name, account_no, account_type, created_date, balance, status FROM customers WHERE status='Approved'";
            Statement st = con.createStatement();
            ResultSet rs = st.executeQuery(sql);

            String[] cols = {"ID", "Name", "Account No", "Type", "Date", "Balance", "Status"};
            Object[][] data = new Object[100][7];
            int i = 0;

            while (rs.next()) {
                data[i][0] = rs.getInt("customer_id");
                data[i][1] = rs.getString("name");
                data[i][2] = rs.getString("account_no");
                data[i][3] = rs.getString("account_type");
                data[i][4] = rs.getDate("created_date");
                data[i][5] = rs.getDouble("balance");
                data[i][6] = rs.getString("status");
                i++;
            }

            approvedTable = new JTable(data, cols);
            JScrollPane sp = new JScrollPane(approvedTable);
            tabbedPane.addTab("Approved Accounts", sp);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void approveSelected() {
        int row = pendingTable.getSelectedRow();
        if (row != -1 && pendingTable.getValueAt(row, 0) != null) {
            int customerId = (int) pendingTable.getValueAt(row, 0);

            try (Connection con = DatabaseConnection.getConnection()) {
                String sql = "UPDATE customers SET status='Approved' WHERE customer_id=?";
                PreparedStatement ps = con.prepareStatement(sql);
                ps.setInt(1, customerId);
                ps.executeUpdate();

                JOptionPane.showMessageDialog(this, "Account approved!");
                dispose();
                new ManageAccount(); // reload
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        new ManageAccount();
    }
}
