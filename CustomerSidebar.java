// File: src/Customer/CustomerSidebar.java
package Customer;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

public class CustomerSidebar extends JPanel {
    private final JButton btnCreate, btnViewTx, btnDeposit, btnWithdraw, btnLogout;

    public CustomerSidebar() {
        setLayout(new GridLayout(5,1,0,10));
        setBackground(new Color(33, 80, 123));
        setPreferredSize(new Dimension(180,0));
        setBorder(BorderFactory.createEmptyBorder(20,10,20,10));

        btnCreate    = makeButton("Create Account");
        btnViewTx    = makeButton("View Transactions");
        btnDeposit   = makeButton("Deposit");
        btnWithdraw  = makeButton("Withdraw");
        btnLogout    = makeButton("Logout");

        add(btnCreate);
        add(btnViewTx);
        add(btnDeposit);
        add(btnWithdraw);
        add(btnLogout);
    }

    private JButton makeButton(String text) {
        JButton b = new JButton(text);
        b.setFocusPainted(false);
        b.setForeground(Color.WHITE);
        b.setBackground(new Color(50,100,160));
        b.setFont(new Font("Segoe UI", Font.BOLD, 14));
        return b;
    }

    public void addCreateAccountListener(ActionListener l) { btnCreate.addActionListener(l); }
    public void addViewTransactionsListener(ActionListener l) { btnViewTx.addActionListener(l); }
    public void addDepositListener(ActionListener l)       { btnDeposit.addActionListener(l); }
    public void addWithdrawListener(ActionListener l)      { btnWithdraw.addActionListener(l); }
    public void addLogoutListener(ActionListener l)        { btnLogout.addActionListener(l); }
}
