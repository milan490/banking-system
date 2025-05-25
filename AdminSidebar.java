// File: src/admin/AdminSidebar.java
package Admin;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

public class AdminSidebar extends JPanel {
    private final JButton btnPending, btnCustomers, btnTransactions, btnReports, btnLogout;

    public AdminSidebar() {
        setLayout(new GridLayout(5,1,0,10));
        setBackground(new Color(55, 71, 79));
        setPreferredSize(new Dimension(200,0));
        setBorder(BorderFactory.createEmptyBorder(20,10,20,10));

        btnPending     = makeButton("Pending Accounts");
        btnCustomers   = makeButton("Manage Customers");
        btnTransactions= makeButton("View Transactions");
        btnReports     = makeButton("Generate Reports");
        btnLogout      = makeButton("Logout");

        add(btnPending);
        add(btnCustomers);
        add(btnTransactions);
        add(btnReports);
        add(btnLogout);
    }

    private JButton makeButton(String text) {
        JButton b = new JButton(text);
        b.setFocusPainted(false);
        b.setForeground(Color.WHITE);
        b.setBackground(new Color(38, 50, 56));
        b.setFont(new Font("Segoe UI", Font.BOLD, 14));
        return b;
    }

    public void addPendingListener(ActionListener l)     { btnPending.addActionListener(l); }
    public void addCustomersListener(ActionListener l)   { btnCustomers.addActionListener(l); }
    public void addTransactionsListener(ActionListener l){ btnTransactions.addActionListener(l); }
    public void addReportsListener(ActionListener l)     { btnReports.addActionListener(l); }
    public void addLogoutListener(ActionListener l)      { btnLogout.addActionListener(l); }
}
