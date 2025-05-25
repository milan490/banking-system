package Admin;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import Connection.DatabaseConnection;

public class ViewTransaction extends JPanel {
    private JTable transactionsTable;
    private DefaultTableModel tableModel;

    private JButton nameSearchButton;
    private JButton accountSearchButton;
    private JButton clearButton;

    // To store current filters
    private String currentNameFilter = null;
    private String currentAccountFilter = null;

    public ViewTransaction() {
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(15, 15, 15, 15));
        setBackground(Color.WHITE);

        // Title
        JLabel titleLabel = new JLabel("Customer Transactions", JLabel.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 26));
        titleLabel.setForeground(new Color(45, 62, 80));
        add(titleLabel, BorderLayout.NORTH);

        // Buttons Panel
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        buttonsPanel.setBackground(Color.WHITE);

        nameSearchButton = new JButton("Search by Customer Name");
        accountSearchButton = new JButton("Search by Account Number");
        clearButton = new JButton("Clear Filters");

        styleButton(nameSearchButton, new Color(52, 152, 219));
        styleButton(accountSearchButton, new Color(46, 204, 113));
        styleButton(clearButton, new Color(231, 76, 60));

        buttonsPanel.add(nameSearchButton);
        buttonsPanel.add(accountSearchButton);
        buttonsPanel.add(clearButton);

        add(buttonsPanel, BorderLayout.SOUTH);

        // Table setup
        tableModel = new DefaultTableModel(new String[]{"Account Number", "Type", "Amount", "Status"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        transactionsTable = new JTable(tableModel);
        transactionsTable.setFillsViewportHeight(true);
        transactionsTable.setRowHeight(28);
        transactionsTable.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        transactionsTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 15));
        transactionsTable.getTableHeader().setBackground(new Color(52, 152, 219));
        transactionsTable.getTableHeader().setForeground(Color.WHITE);

        add(new JScrollPane(transactionsTable), BorderLayout.CENTER);

        // Load all transactions initially
        loadTransactions(null, null);

        // Button actions
        nameSearchButton.addActionListener(e -> {
            String name = JOptionPane.showInputDialog(this, "Enter customer name to search:", "Search by Name", JOptionPane.PLAIN_MESSAGE);
            if (name != null && !name.trim().isEmpty()) {
                currentNameFilter = name.trim();
                currentAccountFilter = null; // reset other filter
                loadTransactions(currentNameFilter, null);
            }
        });

        accountSearchButton.addActionListener(e -> {
            String accountNo = JOptionPane.showInputDialog(this, "Enter account number to search:", "Search by Account Number", JOptionPane.PLAIN_MESSAGE);
            if (accountNo != null && !accountNo.trim().isEmpty()) {
                currentAccountFilter = accountNo.trim();
                currentNameFilter = null; // reset other filter
                loadTransactions(null, currentAccountFilter);
            }
        });

        clearButton.addActionListener(e -> {
            currentNameFilter = null;
            currentAccountFilter = null;
            loadTransactions(null, null);
        });
    }

    private void styleButton(JButton button, Color bg) {
        button.setBackground(bg);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setPreferredSize(new Dimension(200, 35));
    }

    private void loadTransactions(String nameFilter, String accountNumberFilter) {
        StringBuilder query = new StringBuilder("""
            SELECT t.account_number, t.type, t.amount, a.status
            FROM transactions t
            JOIN accounts a ON t.account_number = a.account_number
            WHERE 1=1
        """);

        if (nameFilter != null) {
            query.append(" AND a.name LIKE ? ");
        }
        if (accountNumberFilter != null) {
            query.append(" AND t.account_number LIKE ? ");
        }

        query.append(" ORDER BY t.transaction_date DESC");

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(query.toString())) {

            int paramIndex = 1;
            if (nameFilter != null) {
                ps.setString(paramIndex++, "%" + nameFilter + "%");
            }
            if (accountNumberFilter != null) {
                ps.setString(paramIndex++, "%" + accountNumberFilter + "%");
            }

            try (ResultSet rs = ps.executeQuery()) {
                tableModel.setRowCount(0);

                boolean found = false;
                while (rs.next()) {
                    found = true;
                    String accountNumber = rs.getString("account_number");
                    String type = rs.getString("type");
                    double amount = rs.getDouble("amount");
                    String status = rs.getString("status");

                    tableModel.addRow(new Object[]{accountNumber, type, amount, status});
                }

                if (!found) {
                    JOptionPane.showMessageDialog(this, "No transactions found for given filter.", "No Results", JOptionPane.INFORMATION_MESSAGE);
                }
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading transactions: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
}
