import javax.swing.*;
import java.awt.*;
import java.util.*;

public class EmployeeDashboard extends JFrame {
    private User employeeUser;
    private Employee employee;
    private java.util.List<Payroll> payrolls;

    public EmployeeDashboard(User user) {
        this.employeeUser = user;
        setTitle("Employee Dashboard");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(500, 400);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // Top panel with logout
        JPanel topPanel = new JPanel(new BorderLayout());
        JLabel welcome = new JLabel("Welcome, Employee: " + user.getUsername());
        JButton logoutBtn = new JButton("Logout");
        logoutBtn.addActionListener(e -> { dispose(); new LoginFrame(); });
        topPanel.add(welcome, BorderLayout.WEST);
        topPanel.add(logoutBtn, BorderLayout.EAST);
        add(topPanel, BorderLayout.NORTH);

        // Load employee info
        employee = findEmployee(user.getUsername());
        payrolls = loadPayrolls(employee.getId());

        JPanel infoPanel = new JPanel(new GridLayout(0, 1));
        infoPanel.setBorder(BorderFactory.createTitledBorder("Payroll Details"));
        for (Payroll p : payrolls) {
            infoPanel.add(new JLabel("Month: " + p.getMonth()));
            infoPanel.add(new JLabel("Basic Salary: " + formatCurrency(p.getBasicSalary())));
            infoPanel.add(new JLabel("Deductions: " + formatCurrency(p.getDeductions())));
            infoPanel.add(new JLabel("Net Pay: " + formatCurrency(p.getNetPay())));
            infoPanel.add(new JLabel("Status: " + p.getStatus()));
            infoPanel.add(new JLabel("----------------------"));
        }
        add(infoPanel, BorderLayout.CENTER);
        setVisible(true);
    }

    private String formatCurrency(double amount) {
        return "₱" + String.format("%,.2f", amount);
    }

    private Employee findEmployee(String username) {
        java.util.List<String[]> data = CSVHandler.readCSV("data/employees.csv");
        for (int i = 1; i < data.size(); i++) {
            String[] row = data.get(i);
            // For demo, assume username = emp1 → id=1, emp2 → id=2
            if (username.equals("emp" + row[0])) {
                return new Employee(row[0], row[1], row[2], Double.parseDouble(row[3]));
            }
        }
        return null;
    }

    private java.util.List<Payroll> loadPayrolls(String empId) {
        java.util.List<Payroll> list = new ArrayList<>();
        java.util.List<String[]> data = CSVHandler.readCSV("data/payroll.csv");
        for (int i = 1; i < data.size(); i++) {
            String[] r = data.get(i);
            if (r[1].equals(empId)) {
                list.add(new Payroll(r[0], r[1], r[2], Double.parseDouble(r[3]), Double.parseDouble(r[4]), Double.parseDouble(r[5]), r[6]));
            }
        }
        return list;
    }
}
