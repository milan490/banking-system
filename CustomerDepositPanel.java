package Customer;

import Connection.DatabaseConnection;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class CustomerDepositPanel extends JPanel {
    private final JComboBox<String> accountDropdown;
    private final JTextField nameField, accNoField, amountField;
    private final JButton searchByNameBtn, searchByAccNoBtn, depositButton;

    public CustomerDepositPanel() {
        setLayout(new BorderLayout());

        // Title
        JLabel title = new JLabel("Deposit Funds", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
        add(title, BorderLayout.NORTH);

        // Form
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createEmptyBorder(10, 40, 10, 40));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(12, 12, 12, 12);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Search by Account Number
        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("Search by Account Number:"), gbc);
        accNoField = new JTextField(10);
        gbc.gridx = 1;
        formPanel.add(accNoField, gbc);
        searchByAccNoBtn = new JButton("Search");
        gbc.gridx = 2;
        formPanel.add(searchByAccNoBtn, gbc);

        // Search by Name
        gbc.gridx = 0; gbc.gridy = 1;
        formPanel.add(new JLabel("Search by Name:"), gbc);
        nameField = new JTextField(10);
        gbc.gridx = 1;
        formPanel.add(nameField, gbc);
        searchByNameBtn = new JButton("Search");
        gbc.gridx = 2;
        formPanel.add(searchByNameBtn, gbc);

        // Account dropdown
        gbc.gridx = 0; gbc.gridy = 2;
        formPanel.add(new JLabel("Select Account:"), gbc);
        accountDropdown = new JComboBox<>();
        gbc.gridx = 1; gbc.gridwidth = 2;
        formPanel.add(accountDropdown, gbc);
        gbc.gridwidth = 1;

        // Amount
        gbc.gridx = 0; gbc.gridy = 3;
        formPanel.add(new JLabel("Amount:"), gbc);
        amountField = new JTextField(10);
        gbc.gridx = 1;
        formPanel.add(amountField, gbc);

        // Deposit Button
        depositButton = new JButton("Deposit");
        depositButton.setBackground(new Color(46, 204, 113));
        depositButton.setForeground(Color.WHITE);
        depositButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        gbc.gridx = 1; gbc.gridy = 4;
        formPanel.add(depositButton, gbc);

        add(formPanel, BorderLayout.CENTER);

        // Action listeners
        searchByAccNoBtn.addActionListener(e -> searchAccounts("account_number", accNoField.getText().trim()));
        searchByNameBtn.addActionListener(e -> searchAccounts("name", nameField.getText().trim()));
        depositButton.addActionListener(this::handleDeposit);
    }

    private void searchAccounts(String criteria, String value) {
        accountDropdown.removeAllItems();
        if (value.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a value to search.");
            return;
        }

        String sql = "SELECT account_number FROM accounts WHERE " + criteria + " = ? AND status = 'approved'";
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, value);
            ResultSet rs = ps.executeQuery();
            boolean found = false;
            while (rs.next()) {
                found = true;
                accountDropdown.addItem(rs.getString("account_number"));
            }
            if (!found) {
                JOptionPane.showMessageDialog(this, "No approved accounts found.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void handleDeposit(ActionEvent e) {
        String account = (String) accountDropdown.getSelectedItem();
        String amt = amountField.getText().trim();
        if (account == null || amt.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill in all fields.");
            return;
        }

        try {
            double amount = Double.parseDouble(amt);
            if (amount <= 0) {
                JOptionPane.showMessageDialog(this, "Amount must be greater than 0.");
                return;
            }

            try (Connection con = DatabaseConnection.getConnection()) {
                con.setAutoCommit(false);

                // Update balance
                String updateSQL = "UPDATE accounts SET balance = balance + ? WHERE account_number = ?";
                try (PreparedStatement ps = con.prepareStatement(updateSQL)) {
                    ps.setDouble(1, amount);
                    ps.setString(2, account);
                    ps.executeUpdate();
                }

                // Insert transaction
                String transSQL = "INSERT INTO transactions (account_number, type, amount) VALUES (?, 'deposit', ?)";
                try (PreparedStatement ps = con.prepareStatement(transSQL)) {
                    ps.setString(1, account);
                    ps.setDouble(2, amount);
                    ps.executeUpdate();
                }

                con.commit();
                JOptionPane.showMessageDialog(this, "Deposit successful!");
                amountField.setText("");

            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Deposit failed.");
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Invalid amount.");
        }
    }
}
