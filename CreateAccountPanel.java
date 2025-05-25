package Customer;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.Random;
import Connection.DatabaseConnection;

public class CreateAccountPanel extends JPanel {
    private final JTextField nameField, accNoField;
    private final JComboBox<String> accTypeBox;
    private final JButton createBtn;

    public CreateAccountPanel() {
        setLayout(new GridBagLayout());
        setBackground(new Color(245,248,255));
        setBorder(BorderFactory.createEmptyBorder(20,30,20,30));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;

        Font labelF = new Font("Segoe UI", Font.BOLD, 14);
        Font fieldF = new Font("Segoe UI", Font.PLAIN, 14);

        // Title
        JLabel title = new JLabel("Create New Bank Account", JLabel.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        title.setForeground(new Color(33,80,123));
        gbc.gridwidth = 2; gbc.gridx=0; gbc.gridy=0; gbc.insets=new Insets(0,0,20,0);
        add(title, gbc);

        // Name
        gbc.gridy++; gbc.gridwidth=1; gbc.insets=new Insets(5,5,5,5);
        add(new JLabel("Full Name:"), gbc);
        nameField = new JTextField(20);
        nameField.setFont(fieldF);
        ((AbstractDocument) nameField.getDocument()).setDocumentFilter(new AlphabetOnlyFilter());
        gbc.gridx=1; add(nameField, gbc);

        // Account No
        gbc.gridy++; gbc.gridx=0;
        add(new JLabel("Account Number:"), gbc);
        accNoField = new JTextField(generateAccNo());
        accNoField.setFont(fieldF);
        accNoField.setEditable(false);
        accNoField.setBackground(new Color(230,230,230));
        gbc.gridx=1; add(accNoField, gbc);

        // Type
        gbc.gridy++; gbc.gridx=0;
        add(new JLabel("Account Type:"), gbc);
        accTypeBox = new JComboBox<>(new String[]{"Savings","Current"});
        accTypeBox.setFont(fieldF);
        gbc.gridx=1; add(accTypeBox, gbc);

        // Button
        gbc.gridy++; gbc.gridx=0; gbc.gridwidth=2; gbc.insets=new Insets(20,5,5,5);
        createBtn = new JButton("Submit Request");
        createBtn.setFont(new Font("Segoe UI",Font.BOLD,14));
        createBtn.setForeground(Color.WHITE);
        createBtn.setBackground(new Color(33,80,123));
        createBtn.setFocusPainted(false);
        createBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        createBtn.addActionListener(e->submit());
        add(createBtn, gbc);
    }

    private String generateAccNo() {
        return "AC"+(100000+new Random().nextInt(900000));
    }

    private void submit() {
        String name = nameField.getText().trim();
        if(name.isEmpty()) {
            JOptionPane.showMessageDialog(this,"Enter full name","Validation",JOptionPane.WARNING_MESSAGE);
            return;
        }
        String sql = "INSERT INTO accounts(name,account_number,account_type,status) VALUES(?,?,?,'Pending')";
        try(Connection con=DatabaseConnection.getConnection();
            PreparedStatement ps=con.prepareStatement(sql)) {
            ps.setString(1,name);
            ps.setString(2,accNoField.getText());
            ps.setString(3,(String)accTypeBox.getSelectedItem());
            if(ps.executeUpdate()>0) {
                JOptionPane.showMessageDialog(this,"Request submitted");
                nameField.setText("");
                accNoField.setText(generateAccNo());
            }
        } catch(SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,ex.getMessage(),"Error",JOptionPane.ERROR_MESSAGE);
        }
    }

    // Inner class for allowing only alphabets and space
    static class AlphabetOnlyFilter extends DocumentFilter {
        @Override
        public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr) throws BadLocationException {
            if (string != null && string.matches("[a-zA-Z\\s]+")) {
                super.insertString(fb, offset, string, attr);
            } else {
                Toolkit.getDefaultToolkit().beep();
            }
        }

        @Override
        public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
            if (text != null && text.matches("[a-zA-Z\\s]+")) {
                super.replace(fb, offset, length, text, attrs);
            } else {
                Toolkit.getDefaultToolkit().beep();
            }
        }
    }
}
