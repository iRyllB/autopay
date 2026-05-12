

package gui;

import model.Employee;
import services.EmployeeService;
import utils.PDFGenerator;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.util.List;

public class Dashboard extends JFrame {
    private JTable employeeTable;
    private DefaultTableModel tableModel;
    private EmployeeService employeeService;

    public Dashboard() {
        setTitle("Payroll Systems - Admin Console");
        setSize(1200, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        employeeService = new EmployeeService();
        initUI();
    }

    private void initUI() {
        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(247, 249, 251));
        headerPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(198, 198, 205)));
        headerPanel.setPreferredSize(new Dimension(1200, 80));

        // Search bar
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.setOpaque(false);
        JTextField searchField = new JTextField(30);
        searchField.setFont(new Font("Inter", Font.PLAIN, 16));
        searchField.setToolTipText("Search employee records...");
        searchPanel.add(new JLabel("🔍"));
        searchPanel.add(searchField);
        headerPanel.add(searchPanel, BorderLayout.WEST);

        // Admin info
        JPanel adminPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        adminPanel.setOpaque(false);
        JLabel adminName = new JLabel("Admin User");
        adminName.setFont(new Font("IBM Plex Sans", Font.BOLD, 16));
        JLabel adminRole = new JLabel("Payroll Manager");
        adminRole.setFont(new Font("Inter", Font.PLAIN, 12));
        adminRole.setForeground(new Color(118, 133, 155));
        adminPanel.add(adminName);
        adminPanel.add(Box.createHorizontalStrut(10));
        adminPanel.add(adminRole);
        headerPanel.add(adminPanel, BorderLayout.EAST);

        add(headerPanel, BorderLayout.NORTH);

        // Stats cards
        JPanel statsPanel = new JPanel(new GridLayout(1, 3, 24, 0));
        statsPanel.setBorder(BorderFactory.createEmptyBorder(24, 32, 24, 32));
        statsPanel.setBackground(new Color(247, 249, 251));
        statsPanel.add(createStatCard("Total Monthly Payroll", "₱" + getTotalPayroll(), "+2.4%"));
        statsPanel.add(createStatCard("Active Employees", String.valueOf(employeeService.getAllEmployees().size()), "+" + employeeService.getAllEmployees().size()));
        statsPanel.add(createStatCard("Upcoming Payout", "May 28", "In 4 days"));

        // Main content panel
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());
        mainPanel.add(statsPanel, BorderLayout.NORTH);

        // Table
        String[] columns = {"ID", "Name", "Position", "Payroll", "Bonus", "Status", "Actions"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 3 || column == 4 || column == 5;
            }
        };
        employeeTable = new JTable(tableModel);
        employeeTable.setRowHeight(32);
        employeeTable.setFont(new Font("Inter", Font.PLAIN, 16));
        employeeTable.getTableHeader().setFont(new Font("Inter", Font.BOLD, 16));
        loadEmployees();

        JScrollPane scrollPane = new JScrollPane(employeeTable);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        // Table actions
        employeeTable.getColumn("Actions").setCellRenderer(new ButtonRenderer());
        employeeTable.getColumn("Actions").setCellEditor(new ButtonEditor(new JCheckBox()));

        // Save button
        JButton saveBtn = new JButton("Save Changes");
        saveBtn.setFont(new Font("Inter", Font.BOLD, 16));
        saveBtn.addActionListener(e -> saveChanges());
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottomPanel.add(saveBtn);
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        add(mainPanel, BorderLayout.CENTER);
    }

    private JPanel createStatCard(String title, String value, String sub) {
        JPanel card = new JPanel();
        card.setLayout(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(224, 227, 229)),
                BorderFactory.createEmptyBorder(16, 16, 16, 16)));
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Inter", Font.PLAIN, 14));
        titleLabel.setForeground(new Color(69, 70, 77));
        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("Inter", Font.BOLD, 28));
        valueLabel.setForeground(new Color(19, 28, 46));
        JLabel subLabel = new JLabel(sub);
        subLabel.setFont(new Font("Inter", Font.ITALIC, 12));
        subLabel.setForeground(new Color(76, 120, 77));
        card.add(titleLabel, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        card.add(subLabel, BorderLayout.SOUTH);
        return card;
    }

    private String getTotalPayroll() {
        double total = 0;
        for (Employee e : employeeService.getAllEmployees()) {
            total += e.getPayroll() + e.getBonus();
        }
        return String.format("%,.2f", total);
    }

    private void loadEmployees() {
        tableModel.setRowCount(0);
        List<Employee> employees = employeeService.getAllEmployees();
        for (Employee emp : employees) {
            tableModel.addRow(new Object[]{
                    emp.getId(),
                    emp.getName(),
                    emp.getPosition(),
                    emp.getPayroll(),
                    emp.getBonus(),
                    emp.getStatus(),
                    "PDF Receipt"
            });
        }
    }

    private void saveChanges() {
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            int id = Integer.parseInt(tableModel.getValueAt(i, 0).toString());
            double payroll = Double.parseDouble(tableModel.getValueAt(i, 3).toString());
            double bonus = Double.parseDouble(tableModel.getValueAt(i, 4).toString());
            String status = tableModel.getValueAt(i, 5).toString();
            employeeService.updateEmployeePayroll(id, payroll, bonus, status);
        }
        JOptionPane.showMessageDialog(this, "Changes saved successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
        loadEmployees();
    }

    // ButtonRenderer for PDF Receipt
    class ButtonRenderer extends JButton implements javax.swing.table.TableCellRenderer {
        public ButtonRenderer() {
            setOpaque(true);
        }
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus, int row, int column) {
            setText((value == null) ? "PDF Receipt" : value.toString());
            return this;
        }
    }

    // ButtonEditor for PDF Receipt
    class ButtonEditor extends DefaultCellEditor {
        private JButton button;
        private String label;
        private boolean clicked;
        private int row;

        public ButtonEditor(JCheckBox checkBox) {
            super(checkBox);
            button = new JButton();
            button.setOpaque(true);
            button.addActionListener(e -> fireEditingStopped());
        }

        public Component getTableCellEditorComponent(JTable table, Object value,
                                                     boolean isSelected, int row, int column) {
            label = (value == null) ? "PDF Receipt" : value.toString();
            button.setText(label);
            this.row = row;
            clicked = true;
            return button;
        }

        public Object getCellEditorValue() {
            if (clicked) {
                int id = Integer.parseInt(tableModel.getValueAt(row, 0).toString());
                Employee emp = employeeService.getEmployeeById(id);
                PDFGenerator.generatePayrollReceipt(emp);
                JOptionPane.showMessageDialog(button, "Mock email sent with PDF receipt!", "PDF Generated", JOptionPane.INFORMATION_MESSAGE);
            }
            clicked = false;
            return label;
        }

        public boolean stopCellEditing() {
            clicked = false;
            return super.stopCellEditing();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Dashboard().setVisible(true));
    }
}