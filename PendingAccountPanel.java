package Admin;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import Connection.DatabaseConnection;

class PendingAccountsPanel extends JPanel {
    private final DefaultTableModel model;
    private final JTable table;

    public PendingAccountsPanel() {
        setLayout(new BorderLayout());

        // 1. Build table model with an "Approve" button column
        model = new DefaultTableModel(new String[]{
                "Account No", "Name", "Type", "Approve"
        }, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                // Only the Approve column is editable (i.e. clickable)
                return col == 3;
            }
        };

        table = new JTable(model);
        loadPending();

        // 2. Set up renderer & editor for the button column
        table.getColumn("Approve").setCellRenderer(new ButtonRenderer());
        table.getColumn("Approve").setCellEditor(new ButtonEditor());

        add(new JScrollPane(table), BorderLayout.CENTER);
    }

    private void loadPending() {
        model.setRowCount(0);
        String sql = "SELECT account_number, name, account_type FROM accounts WHERE status='Pending'";
        try (Connection con = DatabaseConnection.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getString("account_number"),
                        rs.getString("name"),
                        rs.getString("account_type"),
                        "Approve"
                });
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, ex.getMessage(),
                    "Error loading pending", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Renderer draws the button
    private static class ButtonRenderer extends JButton implements TableCellRenderer {
        public ButtonRenderer() {
            setOpaque(true);
        }
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus, int row, int column) {
            setText((value == null) ? "" : value.toString());
            return this;
        }
    }

    // Editor handles click events
    private class ButtonEditor extends DefaultCellEditor {
        private final JButton button = new JButton();
        private String accountNo;

        public ButtonEditor() {
            super(new JCheckBox()); // underlying editor component
            button.setOpaque(true);
            button.addActionListener(e -> approveCurrentRow());
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value,
                                                     boolean isSelected, int row, int column) {
            accountNo = table.getValueAt(row, 0).toString();
            button.setText("Approve");
            return button;
        }

        private void approveCurrentRow() {
            // 3. Run the approval update
            String sql = "UPDATE accounts SET status='Approved' WHERE account_number=?";
            try (Connection con = DatabaseConnection.getConnection();
                 PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, accountNo);
                if (ps.executeUpdate() > 0) {
                    JOptionPane.showMessageDialog(PendingAccountsPanel.this,
                            "Account " + accountNo + " approved!");
                    // remove the row from the model
                    int row = table.getSelectedRow();
                    model.removeRow(row);
                } else {
                    JOptionPane.showMessageDialog(PendingAccountsPanel.this,
                            "Approval failed.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(PendingAccountsPanel.this,
                        ex.getMessage(), "SQL Error", JOptionPane.ERROR_MESSAGE);
            } finally {
                // tell the table editing is done
                fireEditingStopped();
            }
        }

        @Override
        public Object getCellEditorValue() {
            return "Approve";
        }
    }
}
