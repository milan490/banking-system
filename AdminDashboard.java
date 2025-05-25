// File: src/Admin/AdminDashboard.java
package Admin;

import javax.swing.*;
import java.awt.*;

public class AdminDashboard extends JFrame {
    private final JPanel mainPanel;

    public AdminDashboard() {
        setTitle("Admin Dashboard");
        setSize(1000, 650);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // Sidebar
        AdminSidebar sidebar = new AdminSidebar();
        add(sidebar, BorderLayout.WEST);

        // Main content panel
        mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(Color.WHITE);
        add(mainPanel, BorderLayout.CENTER);

        showWelcome();

        // Button Listeners
        sidebar.addPendingListener(e -> switchPanel(new PendingAccountsPanel()));
        sidebar.addCustomersListener(e -> switchPanel(new ManageCustomersPanel()));
        sidebar.addTransactionsListener(e -> switchPanel(new ViewTransaction()));
        sidebar.addReportsListener(e -> switchPanel(new GenerateReports())); // Updated here
        sidebar.addLogoutListener(e -> {
            JOptionPane.showMessageDialog(this, "Admin logged out.");
            dispose();
        });

        setVisible(true);
    }

    private void showWelcome() {
        mainPanel.removeAll();
        JLabel lbl = new JLabel(
                "<html><div style='text-align:center;'>"
                        + "<h1>Admin Console</h1><p>Choose an action.</p></div></html>",
                SwingConstants.CENTER
        );
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 20));
        mainPanel.add(lbl, BorderLayout.CENTER);
        refresh();
    }

    private void switchPanel(JPanel panel) {
        mainPanel.removeAll();
        mainPanel.add(panel, BorderLayout.CENTER);
        refresh();
    }

    private void refresh() {
        mainPanel.revalidate();
        mainPanel.repaint();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(AdminDashboard::new);
    }
}
