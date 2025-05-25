package Admin;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.*;
import Connection.DatabaseConnection;

public class GenerateReports extends JPanel {
    private JTextField searchField;
    private JButton searchByAccountButton, searchByNameButton, clearButton, exportButton, printButton;
    private JTable reportTable;
    private DefaultTableModel tableModel;

    public GenerateReports() {
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(10, 10, 10, 10));

        // Search Panel
        JPanel inputPanel = new JPanel(new BorderLayout(10, 10));
        inputPanel.setBorder(new TitledBorder("Search Transactions"));

        searchField = new JTextField();
        JPanel fieldPanel = new JPanel(new BorderLayout(5, 5));
        fieldPanel.add(new JLabel("Enter Account Number or Name:"), BorderLayout.WEST);
        fieldPanel.add(searchField, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchByAccountButton = new JButton("Search by Account Number");
        searchByNameButton = new JButton("Search by Name");
        clearButton = new JButton("Clear");
        exportButton = new JButton("Export to CSV");
        printButton = new JButton("Print Report");

        for (JButton btn : new JButton[]{searchByAccountButton, searchByNameButton, clearButton, exportButton, printButton}) {
            styleButton(btn);
            buttonPanel.add(btn);
        }

        inputPanel.add(fieldPanel, BorderLayout.NORTH);
        inputPanel.add(buttonPanel, BorderLayout.CENTER);
        add(inputPanel, BorderLayout.NORTH);

        // Report Table
        tableModel = new DefaultTableModel(new String[]{"Transaction ID", "Account Number", "Type", "Amount", "Transaction Date"}, 0) {
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        reportTable = new JTable(tableModel);
        reportTable.setFillsViewportHeight(true);
        reportTable.setRowHeight(25);
        reportTable.setFont(new Font("SansSerif", Font.PLAIN, 14));
        reportTable.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 14));
        reportTable.setAutoCreateRowSorter(true);

        JScrollPane scrollPane = new JScrollPane(reportTable);
        scrollPane.setBorder(new TitledBorder("Transaction History"));
        add(scrollPane, BorderLayout.CENTER);

        // Action Listeners
        searchByAccountButton.addActionListener(e -> searchByAccountNumber());
        searchByNameButton.addActionListener(e -> searchByName());
        clearButton.addActionListener(e -> clearInputs());
        exportButton.addActionListener(e -> exportToCSV());
        printButton.addActionListener(e -> printReport());
    }

    private void searchByAccountNumber() {
        String accNum = searchField.getText().trim();
        if (accNum.isEmpty()) {
            showError("Please enter an account number.");
            return;
        }

        clearTable();
        fetchTransactions("SELECT transaction_id, account_number, type, amount, transaction_date FROM transactions WHERE account_number = ?", accNum);
    }

    private void searchByName() {
        String name = searchField.getText().trim();
        if (name.isEmpty()) {
            showError("Please enter a customer name.");
            return;
        }

        clearTable();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT account_number FROM accounts WHERE name = ?")) {

            stmt.setString(1, name);
            ResultSet rs = stmt.executeQuery();

            boolean found = false;
            while (rs.next()) {
                String accNumber = rs.getString("account_number");
                fetchTransactions("SELECT transaction_id, account_number, type, amount, transaction_date FROM transactions WHERE account_number = ?", accNumber);
                found = true;
            }

            if (!found) {
                showError("No account found with name: " + name);
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
            showError("Error fetching account number: " + ex.getMessage());
        }
    }

    private void fetchTransactions(String query, String param) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, param);
            ResultSet rs = stmt.executeQuery();

            boolean hasResults = false;
            while (rs.next()) {
                Object[] row = {
                        rs.getInt("transaction_id"),
                        rs.getString("account_number"),
                        rs.getString("type"),
                        rs.getDouble("amount"),
                        rs.getDate("transaction_date")
                };
                tableModel.addRow(row);
                hasResults = true;
            }

            if (!hasResults) {
                showError("No transactions found.");
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
            showError("Error fetching transactions: " + ex.getMessage());
        }
    }

    private void clearTable() {
        tableModel.setRowCount(0);
    }

    private void clearInputs() {
        searchField.setText("");
        clearTable();
    }

    private void exportToCSV() {
        if (tableModel.getRowCount() == 0) {
            showError("No data to export.");
            return;
        }

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save Report as CSV");
        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try (FileWriter writer = new FileWriter(fileChooser.getSelectedFile() + ".csv")) {
                for (int i = 0; i < tableModel.getColumnCount(); i++) {
                    writer.write(tableModel.getColumnName(i) + ",");
                }
                writer.write("\n");

                for (int row = 0; row < tableModel.getRowCount(); row++) {
                    for (int col = 0; col < tableModel.getColumnCount(); col++) {
                        writer.write(tableModel.getValueAt(row, col).toString() + ",");
                    }
                    writer.write("\n");
                }

                JOptionPane.showMessageDialog(this, "Report exported successfully.");

            } catch (IOException ex) {
                ex.printStackTrace();
                showError("Error exporting CSV: " + ex.getMessage());
            }
        }
    }

    private void printReport() {
        try {
            if (!reportTable.print()) {
                showError("Printing canceled.");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            showError("Error printing report: " + ex.getMessage());
        }
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Information", JOptionPane.INFORMATION_MESSAGE);
    }

    private void styleButton(JButton button) {
        button.setFocusPainted(false);
        button.setBackground(new Color(66, 133, 244));
        button.setForeground(Color.WHITE);
        button.setFont(new Font("SansSerif", Font.BOLD, 13));
        button.setPreferredSize(new Dimension(180, 30));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }
}
