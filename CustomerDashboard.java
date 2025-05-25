// File: src/Customer/CustomerDashboard.java
package Customer;

import javax.swing.*;
import java.awt.*;

public class CustomerDashboard extends JFrame {
    private final JPanel mainPanel;

    public CustomerDashboard() {
        setTitle("Customer Dashboard");
        setSize(900,600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        CustomerSidebar sidebar = new CustomerSidebar();
        add(sidebar, BorderLayout.WEST);

        mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(Color.WHITE);
        add(mainPanel, BorderLayout.CENTER);

        showWelcome();

        sidebar.addCreateAccountListener(e -> {
            mainPanel.removeAll();
            mainPanel.add(new CreateAccountPanel(), BorderLayout.CENTER);
            refresh();
        });

        sidebar.addViewTransactionsListener(e -> {
            mainPanel.removeAll();
            mainPanel.add(new CustomerTransactionViewer(), BorderLayout.CENTER);
            refresh();
        });

        // Updated: No need to pass account number now
        sidebar.addDepositListener(e -> {
            mainPanel.removeAll();
            mainPanel.add(new CustomerDepositPanel(), BorderLayout.CENTER);
            refresh();
        });

        sidebar.addWithdrawListener(e -> {
            mainPanel.removeAll();
            mainPanel.add(new CustomerWithdrawPanel(), BorderLayout.CENTER);
            refresh();
        });

        sidebar.addLogoutListener(e -> {
            JOptionPane.showMessageDialog(this,"Logged out");
            dispose();
        });

        setVisible(true);
    }

    private void showWelcome() {
        mainPanel.removeAll();
        JLabel lbl = new JLabel(
                "<html><div style='text-align:center;'>"
                        + "<h1>Welcome!</h1><p>Select an option</p></div></html>",
                SwingConstants.CENTER
        );
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        mainPanel.add(lbl, BorderLayout.CENTER);
        refresh();
    }

    private void refresh() {
        mainPanel.revalidate();
        mainPanel.repaint();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(CustomerDashboard::new);
    }
}
