package Customer;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.*;
import java.awt.*;
import java.sql.*;
import java.util.HashSet;
import java.util.Set;

import Connection.DatabaseConnection;

public class CustomerTransactionViewer extends JPanel {

    private JTextField searchField;
    private JButton btnSearchAccount, btnSearchName, btnClear;
    private JTable transactionTable;
    private DefaultTableModel tableModel;

    public CustomerTransactionViewer() {
        setLayout(new BorderLayout(20, 20));
        setBackground(Color.WHITE);
        setBorder(new EmptyBorder(20, 20, 20, 20));

        // Create header label
        JLabel headerLabel = new JLabel("View Transactions");
        headerLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        headerLabel.setForeground(new Color(33, 33, 33));
        headerLabel.setHorizontalAlignment(SwingConstants.CENTER);
        add(headerLabel, BorderLayout.NORTH);

        // Create search panel (input + buttons)
        JPanel searchPanel = createSearchPanel();
        add(searchPanel, BorderLayout.CENTER);

        // Create table panel
        JPanel tablePanel = createTablePanel();
        add(tablePanel, BorderLayout.SOUTH);
    }

    private JPanel createSearchPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);

        // Label
        JLabel lblSearch = new JLabel("Enter Account Number or Customer Name:");
        lblSearch.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        lblSearch.setForeground(new Color(55, 55, 55));
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 3; gbc.anchor = GridBagConstraints.WEST;
        panel.add(lblSearch, gbc);

        // Text Field
        searchField = new JTextField(30);
        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        gbc.gridy = 1; gbc.gridwidth = 3; gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(searchField, gbc);

        // Buttons
        btnSearchAccount = createButton("Search by Account Number");
        btnSearchName = createButton("Search by Name");
        btnClear = createButton("Clear");

        gbc.gridwidth = 1; gbc.fill = GridBagConstraints.NONE; gbc.gridy = 2; gbc.gridx = 0;
        panel.add(btnSearchAccount, gbc);
        gbc.gridx = 1;
        panel.add(btnSearchName, gbc);
        gbc.gridx = 2;
        panel.add(btnClear, gbc);

        // Button actions
        btnSearchAccount.addActionListener(e -> searchByAccountNumber());
        btnSearchName.addActionListener(e -> searchByName());
        btnClear.addActionListener(e -> clearAll());

        return panel;
    }

    private JButton createButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setBackground(new Color(25, 118, 210));
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(200, 35));
        btn.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        return btn;
    }

    private JPanel createTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
                "Transaction History", TitledBorder.LEADING, TitledBorder.TOP,
                new Font("Segoe UI", Font.BOLD, 18), new Color(50, 50, 50)));

        // Table setup
        String[] columns = {"Transaction ID", "Account Number", "Type", "Amount", "Transaction Date"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };

        transactionTable = new JTable(tableModel);
        transactionTable.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        transactionTable.setRowHeight(28);
        transactionTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 15));
        transactionTable.setFillsViewportHeight(true);
        transactionTable.setAutoCreateRowSorter(true);

        // Alternating row colors
        transactionTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            private final Color evenColor = new Color(245, 245, 245);
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus,
                                                           int row, int column) {
                super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (!isSelected) {
                    setBackground(row % 2 == 0 ? evenColor : Color.WHITE);
                }
                return this;
            }
        });

        JScrollPane scrollPane = new JScrollPane(transactionTable);
        panel.add(scrollPane, BorderLayout.CENTER);

        panel.setPreferredSize(new Dimension(900, 350));
        return panel;
    }

    private void searchByAccountNumber() {
        String accNum = searchField.getText().trim();
        if (accNum.isEmpty()) {
            showWarning("Please enter an account number.");
            return;
        }
        clearTable();
        String sql = "SELECT transaction_id, account_number, type, amount, transaction_date FROM transactions WHERE account_number = ?";
        fetchTransactions(sql, accNum);
    }

    private void searchByName() {
        String name = searchField.getText().trim();
        if (name.isEmpty()) {
            showWarning("Please enter a customer name.");
            return;
        }
        clearTable();

        String sqlAccounts = "SELECT account_number FROM accounts WHERE name = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement psAcc = conn.prepareStatement(sqlAccounts)) {

            psAcc.setString(1, name);
            ResultSet rsAcc = psAcc.executeQuery();

            Set<String> accounts = new HashSet<>();
            while (rsAcc.next()) {
                accounts.add(rsAcc.getString("account_number"));
            }

            if (accounts.isEmpty()) {
                showInfo("No accounts found for name: " + name);
                return;
            }

            String sqlTrans = "SELECT transaction_id, account_number, type, amount, transaction_date FROM transactions WHERE account_number = ?";
            try (PreparedStatement psTrans = conn.prepareStatement(sqlTrans)) {
                boolean foundAny = false;
                for (String acc : accounts) {
                    psTrans.setString(1, acc);
                    ResultSet rsTrans = psTrans.executeQuery();

                    while (rsTrans.next()) {
                        tableModel.addRow(new Object[]{
                                rsTrans.getInt("transaction_id"),
                                rsTrans.getString("account_number"),
                                rsTrans.getString("type"),
                                rsTrans.getDouble("amount"),
                                rsTrans.getDate("transaction_date")
                        });
                        foundAny = true;
                    }
                }
                if (!foundAny) {
                    showInfo("No transactions found for name: " + name);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
            showError("Error accessing database: " + e.getMessage());
        }
    }

    private void fetchTransactions(String query, String param) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setString(1, param);
            ResultSet rs = ps.executeQuery();

            boolean found = false;
            while (rs.next()) {
                tableModel.addRow(new Object[]{
                        rs.getInt("transaction_id"),
                        rs.getString("account_number"),
                        rs.getString("type"),
                        rs.getDouble("amount"),
                        rs.getDate("transaction_date")
                });
                found = true;
            }

            if (!found) {
                showInfo("No transactions found.");
            }

        } catch (SQLException e) {
            e.printStackTrace();
            showError("Error fetching transactions: " + e.getMessage());
        }
    }

    private void clearTable() {
        tableModel.setRowCount(0);
    }

    private void clearAll() {
        searchField.setText("");
        clearTable();
    }

    private void showInfo(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Information", JOptionPane.INFORMATION_MESSAGE);
    }

    private void showWarning(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Warning", JOptionPane.WARNING_MESSAGE);
    }

    private void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Error", JOptionPane.ERROR_MESSAGE);
    }
}
