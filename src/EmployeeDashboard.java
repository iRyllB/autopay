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

        // Load employee info first
        employee = findEmployee(user.getUsername());
        payrolls = loadPayrolls(employee.getId());

        // Top panel with logout
        JPanel topPanel = new JPanel(new BorderLayout());
        String welcomeText = (employee != null)
            ? ("Welcome, Employee: " + employee.getName())
            : ("Welcome, Employee: " + user.getUsername());
        JLabel welcome = new JLabel(welcomeText);
        JButton logoutBtn = new JButton("Logout");
        logoutBtn.addActionListener(e -> { dispose(); new LoginFrame(); });
        topPanel.add(welcome, BorderLayout.WEST);
        topPanel.add(logoutBtn, BorderLayout.EAST);
        add(topPanel, BorderLayout.NORTH);

        // Main tabbed pane
        JTabbedPane tabs = new JTabbedPane();

        // Payroll tab
        tabs.addTab("Payroll", buildPayrollTab());

        // Settings tab
        tabs.addTab("Settings", new EmployeeSettingsPanel(employeeUser, employee));

        // Show flagged warning if employee is flagged (top box)
        if (employee != null && employee.isFlagged()) {
            JPanel flaggedPanel = new JPanel();
            flaggedPanel.setBackground(new Color(255, 220, 220));
            flaggedPanel.setBorder(BorderFactory.createLineBorder(Color.RED));
            flaggedPanel.add(new JLabel("You have been flagged. For more information, please contact management +639123456788."));
            JPanel flaggedBox = new JPanel(new BorderLayout());
            flaggedBox.add(flaggedPanel, BorderLayout.CENTER);
            add(flaggedBox, BorderLayout.NORTH);
        }

        add(tabs, BorderLayout.CENTER);
        setVisible(true);
    }

    private String formatCurrency(double amount) {
        return "₱" + String.format("%,.2f", amount);
    }

    private JPanel buildPayrollTab() {
        JPanel root = new JPanel(new BorderLayout());

        JPanel listPanel = new JPanel();
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
        listPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        if (payrolls == null || payrolls.isEmpty()) {
            JLabel empty = new JLabel("No payroll records found.");
            empty.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
            root.add(empty, BorderLayout.NORTH);
            return root;
        }

        for (Payroll p : payrolls) {
            listPanel.add(buildPayrollCard(p));
            listPanel.add(Box.createVerticalStrut(10));
        }

        JScrollPane scroll = new JScrollPane(listPanel);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        root.add(scroll, BorderLayout.CENTER);
        return root;
    }

    private JPanel buildPayrollCard(Payroll p) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220)),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));

        JPanel topRow = new JPanel(new BorderLayout());
        topRow.setOpaque(false);
        JLabel monthLabel = new JLabel("Month: " + p.getMonth());
        monthLabel.setFont(monthLabel.getFont().deriveFont(Font.BOLD));
        topRow.add(monthLabel, BorderLayout.WEST);
        topRow.add(buildStatusBadge(p.getStatus()), BorderLayout.EAST);
        card.add(topRow);

        card.add(Box.createVerticalStrut(8));
        card.add(new JLabel("Basic Salary: " + formatCurrency(p.getBasicSalary())));
        card.add(new JLabel("Deductions: " + formatCurrency(p.getDeductions())));
        card.add(new JLabel("Net Pay: " + formatCurrency(p.getNetPay())));

        if ("Processed".equalsIgnoreCase(String.valueOf(p.getStatus()).trim())) {
            card.add(Box.createVerticalStrut(8));
            JLabel note = new JLabel("Payment has been sent; it may take a while to reflect on your payroll account.");
            note.setForeground(new Color(70, 70, 70));
            card.add(note);
        }

        return card;
    }

    private JComponent buildStatusBadge(String status) {
        String normalized = status == null ? "" : status.trim();
        boolean processed = "Processed".equalsIgnoreCase(normalized);

        JLabel badge = new JLabel(processed ? "PROCESSED" : "PENDING");
        badge.setOpaque(true);
        badge.setForeground(Color.WHITE);
        badge.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
        badge.setFont(badge.getFont().deriveFont(Font.BOLD, 12f));
        badge.setBackground(processed ? new Color(34, 139, 34) : new Color(190, 70, 70));
        return badge;
    }

    private Employee findEmployee(String username) {
        java.util.List<String[]> data = CSVHandler.readCSV("data/employees.csv");
        if (data.isEmpty()) return null;

        String[] header = data.get(0);
        boolean newFormat = header.length >= 6
                && "id".equalsIgnoreCase(header[0])
                && "firstname".equalsIgnoreCase(header[1])
                && "lastname".equalsIgnoreCase(header[2]);

        for (int i = 1; i < data.size(); i++) {
            String[] row = data.get(i);
            // Username is the employee FIRST name (default generated by Admin)
            if (newFormat) {
                if (row.length < 5) continue;
                String firstName = String.valueOf(row[1]).trim();
                if (!username.equals(firstName)) continue;

                String lastName = (row.length >= 3) ? String.valueOf(row[2]).trim() : "";
                String position = (row.length >= 4) ? row[3] : "";
                double salary = (row.length >= 5) ? Double.parseDouble(row[4]) : 0.0;
                boolean flagged = false;
                if (row.length >= 6) {
                    flagged = "1".equals(String.valueOf(row[5])) || "true".equalsIgnoreCase(String.valueOf(row[5]));
                }
                return new Employee(row[0], firstName, lastName, position, salary, flagged);
            } else {
                if (row.length < 4) continue;
                String fullName = String.valueOf(row[1]);
                String firstName = fullName.trim().split("\\s+")[0];
                if (!username.equals(firstName)) continue;

                String[] parts = String.valueOf(row[1]).trim().split("\\s+");
                String lastName = (parts.length <= 1) ? "" : String.join(" ", java.util.Arrays.copyOfRange(parts, 1, parts.length));
                boolean flagged = false;
                if (row.length >= 5) {
                    flagged = "1".equals(String.valueOf(row[4])) || "true".equalsIgnoreCase(String.valueOf(row[4]));
                }
                return new Employee(row[0], firstName, lastName, row[2], Double.parseDouble(row[3]), flagged);
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
