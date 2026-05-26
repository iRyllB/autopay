import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.table.DefaultTableModel;

public class AdminDashboard extends JFrame {
    private User admin;
    private java.util.List<Employee> employees;
    private java.util.List<Payroll> payrolls;
    private JTable employeeTable;
    private JTable payrollTable;
    private DefaultTableModel empModel;
    private DefaultTableModel payrollModel;

    public AdminDashboard(User admin) {
        this.admin = admin;
        setTitle("Admin Dashboard");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // Top panel with logout
        JPanel topPanel = new JPanel(new BorderLayout());
        JLabel welcome = new JLabel("Welcome, Admin: " + admin.getUsername());
        JButton logoutBtn = new JButton("Logout");
        logoutBtn.addActionListener(e -> { dispose(); new LoginFrame(); });
        topPanel.add(welcome, BorderLayout.WEST);
        topPanel.add(logoutBtn, BorderLayout.EAST);
        add(topPanel, BorderLayout.NORTH);

        // Tabs for Employees and Payroll
        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Employees", employeePanel());
        tabs.addTab("Payroll", payrollPanel());
        add(tabs, BorderLayout.CENTER);

        setVisible(true);
    }

    private JPanel employeePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        employees = loadEmployees();
        String[] columns = {"ID", "Name", "Position", "Basic Salary"};
        empModel = new DefaultTableModel(columns, 0);
        for (Employee emp : employees) {
            empModel.addRow(new Object[]{emp.getId(), emp.getName(), emp.getPosition(), formatCurrency(emp.getBasicSalary())});
        }
        employeeTable = new JTable(empModel);
        panel.add(new JScrollPane(employeeTable), BorderLayout.CENTER);

        JPanel btnPanel = new JPanel();
        JButton addBtn = new JButton("Add");
        JButton editBtn = new JButton("Edit");
        JButton delBtn = new JButton("Delete");
        btnPanel.add(addBtn); btnPanel.add(editBtn); btnPanel.add(delBtn);
        panel.add(btnPanel, BorderLayout.SOUTH);

        addBtn.addActionListener(e -> addEmployee());
        editBtn.addActionListener(e -> editEmployee());
        delBtn.addActionListener(e -> deleteEmployee());
        return panel;
    }

    private JPanel payrollPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        payrolls = loadPayrolls();
        String[] columns = {"Payroll ID", "Employee ID", "Month", "Basic Salary", "Deductions", "Net Pay", "Status"};
        payrollModel = new DefaultTableModel(columns, 0);
        for (Payroll p : payrolls) {
            payrollModel.addRow(new Object[]{p.getPayrollId(), p.getEmployeeId(), p.getMonth(), formatCurrency(p.getBasicSalary()), formatCurrency(p.getDeductions()), formatCurrency(p.getNetPay()), p.getStatus()});
        }
        payrollTable = new JTable(payrollModel);
        panel.add(new JScrollPane(payrollTable), BorderLayout.CENTER);

        JButton processBtn = new JButton("Process Payroll");
        processBtn.addActionListener(e -> processPayroll());
        panel.add(processBtn, BorderLayout.SOUTH);
        return panel;
    }

    private java.util.List<Employee> loadEmployees() {
        java.util.List<Employee> list = new ArrayList<>();
        java.util.List<String[]> data = CSVHandler.readCSV("data/employees.csv");
        for (int i = 1; i < data.size(); i++) {
            String[] row = data.get(i);
            list.add(new Employee(row[0], row[1], row[2], Double.parseDouble(row[3])));
        }
        return list;
    }

    private java.util.List<Payroll> loadPayrolls() {
        java.util.List<Payroll> list = new ArrayList<>();
        java.util.List<String[]> data = CSVHandler.readCSV("data/payroll.csv");
        for (int i = 1; i < data.size(); i++) {
            String[] r = data.get(i);
            list.add(new Payroll(r[0], r[1], r[2], Double.parseDouble(r[3]), Double.parseDouble(r[4]), Double.parseDouble(r[5]), r[6]));
        }
        return list;
    }

    private String formatCurrency(double amount) {
        return "₱" + String.format("%,.2f", amount);
    }

    private void addEmployee() {
        JTextField idField = new JTextField();
        JTextField nameField = new JTextField();
        JTextField posField = new JTextField();
        JTextField salaryField = new JTextField();
        Object[] fields = {"ID:", idField, "Name:", nameField, "Position:", posField, "Basic Salary:", salaryField};
        int res = JOptionPane.showConfirmDialog(this, fields, "Add Employee", JOptionPane.OK_CANCEL_OPTION);
        if (res == JOptionPane.OK_OPTION) {
            Employee emp = new Employee(idField.getText(), nameField.getText(), posField.getText(), Double.parseDouble(salaryField.getText()));
            employees.add(emp);
            empModel.addRow(new Object[]{emp.getId(), emp.getName(), emp.getPosition(), emp.getBasicSalary()});
            saveEmployees();
        }
    }

    private void editEmployee() {
        int row = employeeTable.getSelectedRow();
        if (row == -1) return;
        Employee emp = employees.get(row);
        JTextField nameField = new JTextField(emp.getName());
        JTextField posField = new JTextField(emp.getPosition());
        JTextField salaryField = new JTextField(String.valueOf(emp.getBasicSalary()));
        Object[] fields = {"Name:", nameField, "Position:", posField, "Basic Salary:", salaryField};
        int res = JOptionPane.showConfirmDialog(this, fields, "Edit Employee", JOptionPane.OK_CANCEL_OPTION);
        if (res == JOptionPane.OK_OPTION) {
            emp.setName(nameField.getText());
            emp.setPosition(posField.getText());
            emp.setBasicSalary(Double.parseDouble(salaryField.getText()));
            empModel.setValueAt(emp.getName(), row, 1);
            empModel.setValueAt(emp.getPosition(), row, 2);
            empModel.setValueAt(emp.getBasicSalary(), row, 3);
            saveEmployees();
        }
    }

    private void deleteEmployee() {
        int row = employeeTable.getSelectedRow();
        if (row == -1) return;
        employees.remove(row);
        empModel.removeRow(row);
        saveEmployees();
    }

    private void saveEmployees() {
        java.util.List<String[]> data = new ArrayList<>();
        data.add(new String[]{"id","name","position","basicSalary"});
        for (Employee emp : employees) {
            data.add(new String[]{emp.getId(), emp.getName(), emp.getPosition(), String.valueOf(emp.getBasicSalary())});
        }
        CSVHandler.writeCSV("data/employees.csv", data);
    }

    private void processPayroll() {
        int row = payrollTable.getSelectedRow();
        if (row == -1) return;
        Payroll p = payrolls.get(row);
        if (!p.getStatus().equals("Processed")) {
            p.setStatus("Processed");
            payrollModel.setValueAt("Processed", row, 6);
            savePayrolls();
        }
    }

    private void savePayrolls() {
        java.util.List<String[]> data = new ArrayList<>();
        data.add(new String[]{"payrollId","employeeId","month","basicSalary","deductions","netPay","status"});
        for (Payroll p : payrolls) {
            data.add(new String[]{p.getPayrollId(), p.getEmployeeId(), p.getMonth(), String.valueOf(p.getBasicSalary()), String.valueOf(p.getDeductions()), String.valueOf(p.getNetPay()), p.getStatus()});
        }
        CSVHandler.writeCSV("data/payroll.csv", data);
    }
}
