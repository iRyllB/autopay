import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.util.*;


public class AdminDashboard extends JFrame {
    private final User admin;

    private java.util.List<Employee> employees;
    private java.util.List<Payroll> payrolls;

    private JTable employeeTable;
    private DefaultTableModel employeeModel;

    private JTable payrollTable;
    private DefaultTableModel payrollModel;

    // Right-side control panel (salary edit + flagged)
    private EmployeeControlPanel employeeControlPanel;

    // Table filtering widgets
    private JTextField searchField;
    private JComboBox<String> flaggedFilter;

    // Index in employees list (model index)
    private int selectedEmployeeModelIndex = -1;

    public AdminDashboard(User admin) {
        this.admin = admin;

        setTitle("Admin Dashboard");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(980, 650);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        JPanel topPanel = new JPanel(new BorderLayout());
        JLabel welcome = new JLabel("Welcome, Admin: " + this.admin.getUsername());
        JButton logoutBtn = new JButton("Logout");
        logoutBtn.addActionListener(e -> {
            dispose();
            new LoginFrame();
        });
        topPanel.add(welcome, BorderLayout.WEST);
        topPanel.add(logoutBtn, BorderLayout.EAST);
        add(topPanel, BorderLayout.NORTH);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Employees", employeePanel());
        tabs.addTab("Payroll", payrollPanel());
        add(tabs, BorderLayout.CENTER);

        setVisible(true);
    // Static method to update admin password in users.csv

    }

    // Static method to update admin password in users.csv
    public static void updateAdminPassword(String username, String newPassword) {
        java.util.List<String[]> data = CSVHandler.readCSV("data/users.csv");
        for (int i = 1; i < data.size(); i++) {
            String[] row = data.get(i);
            if (row[0].equals(username)) {
                row[1] = newPassword;
                break;
            }
        }
        CSVHandler.writeCSV("data/users.csv", data);
    }

    private JPanel employeePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        employees = loadEmployees();

        JPanel leftPanel = new JPanel(new BorderLayout());

        // Search + filter (top)
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filterPanel.add(new JLabel("Search:"));
        searchField = new JTextField(18);
        filterPanel.add(searchField);

        filterPanel.add(new JLabel("Flagged:"));
        flaggedFilter = new JComboBox<>(new String[]{"All", "Flagged Only"});
        filterPanel.add(flaggedFilter);

        JButton refreshBtn = new JButton("Refresh");
        filterPanel.add(refreshBtn);

        leftPanel.add(filterPanel, BorderLayout.NORTH);

        // Table model includes a hidden-ish flagged column for filtering/rendering
        String[] columns = {"ID", "Name", "Position", "Basic Salary", "Flagged"};
        employeeModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        for (Employee emp : employees) {
            employeeModel.addRow(new Object[]{
                    emp.getId(),
                    emp.getName(),
                    emp.getPosition(),
                    formatCurrencyPHP(emp.getBasicSalary()),
                    emp.isFlagged() ? 1 : 0
            });
        }

        employeeTable = new JTable(employeeModel);
        employeeTable.setAutoCreateRowSorter(true);

        // Row coloring for flagged employees
        employeeTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                int modelRow = table.convertRowIndexToModel(row);
                Object flaggedVal = table.getModel().getValueAt(modelRow, 4);

                boolean flagged = false;
                if (flaggedVal instanceof Integer) flagged = ((Integer) flaggedVal) == 1;
                else if (flaggedVal instanceof Boolean) flagged = (Boolean) flaggedVal;

                if (!isSelected && flagged) {
                    c.setBackground(new Color(255, 200, 200));
                } else {
                    if (!isSelected) c.setBackground(Color.WHITE);
                }
                return c;
            }
        });

        // Selection listener: right control panel updates
        employeeTable.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        employeeTable.getSelectionModel().addListSelectionListener(e -> {
            if (e.getValueIsAdjusting()) return;
            int viewRow = employeeTable.getSelectedRow();
            if (viewRow < 0) {
                selectedEmployeeModelIndex = -1;
                employeeControlPanel.updateFromEmployee(null, -1);
                return;
            }
            selectedEmployeeModelIndex = employeeTable.convertRowIndexToModel(viewRow);
            employeeControlPanel.updateFromEmployee(employees.get(selectedEmployeeModelIndex), selectedEmployeeModelIndex);
        });

        leftPanel.add(new JScrollPane(employeeTable), BorderLayout.CENTER);

        // Bottom buttons (Add/Delete)
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton addBtn = new JButton("Add");
        JButton delBtn = new JButton("Delete");
        btnPanel.add(addBtn);
        btnPanel.add(delBtn);
        leftPanel.add(btnPanel, BorderLayout.SOUTH);

        addBtn.addActionListener(e -> addEmployee());
        delBtn.addActionListener(e -> deleteSelectedEmployee());

        // Right-side control panel
        employeeControlPanel = new EmployeeControlPanel(this);

        // Sort/filter wiring
        TableRowSorter<DefaultTableModel> sorter = (TableRowSorter<DefaultTableModel>) employeeTable.getRowSorter();
        // Fix: Sort ID column numerically
        sorter.setComparator(0, (a, b) -> {
            try {
                return Integer.compare(Integer.parseInt(a.toString()), Integer.parseInt(b.toString()));
            } catch (Exception e) {
                return a.toString().compareTo(b.toString());
            }
        });

        refreshBtn.addActionListener(e -> {
            dispose();
            new AdminDashboard(admin);
        });

        Runnable applyFilters = () -> {
            String text = (searchField.getText() == null) ? "" : searchField.getText().trim().toLowerCase();
            boolean onlyFlagged = "Flagged Only".equals(String.valueOf(flaggedFilter.getSelectedItem()));

            sorter.setRowFilter(new RowFilter<DefaultTableModel, Integer>() {
                @Override
                public boolean include(Entry<? extends DefaultTableModel, ? extends Integer> entry) {
                    String id = String.valueOf(entry.getValue(0));
                    String name = String.valueOf(entry.getValue(1));
                    String pos = String.valueOf(entry.getValue(2));
                    int flaggedVal = Integer.parseInt(String.valueOf(entry.getValue(4)));

                    boolean matchesText = text.isEmpty()
                            || id.toLowerCase().contains(text)
                            || name.toLowerCase().contains(text)
                            || pos.toLowerCase().contains(text);
                    boolean matchesFlag = !onlyFlagged || flaggedVal == 1;
                    return matchesText && matchesFlag;
                }
            });
        };

        searchField.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyReleased(java.awt.event.KeyEvent e) {
                applyFilters.run();
            }
        });
        flaggedFilter.addActionListener(e -> applyFilters.run());

        panel.add(leftPanel, BorderLayout.CENTER);
        panel.add(employeeControlPanel, BorderLayout.EAST);
        return panel;
    }

    private JPanel payrollPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        payrolls = loadPayrolls();

        String[] columns = {"Payroll ID", "Employee ID", "Month", "Basic Salary", "Deductions", "Net Pay", "Status"};
        payrollModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        for (Payroll p : payrolls) {
            }

            // Determine if all payrolls for the current month are still pending
            boolean allPending = true;
            for (Payroll p : payrolls) {
                if (currentMonth().equals(p.getMonth()) && !"Pending".equalsIgnoreCase(p.getStatus())) {
                    allPending = false;
                    break;
                }
            }

            for (Payroll p : payrolls) {
                String statusToShow = p.getStatus();
                if (allPending && currentMonth().equals(p.getMonth())) {
                    statusToShow = "Pending";
                }
                payrollModel.addRow(new Object[]{
                    p.getPayrollId(),
                    p.getEmployeeId(),
                    p.getMonth(),
                    formatCurrencyPHP(p.getBasicSalary()),
                    formatCurrencyPHP(p.getDeductions()),
                    formatCurrencyPHP(p.getNetPay()),
                    statusToShow
                });
        }

        payrollTable = new JTable(payrollModel);
        panel.add(new JScrollPane(payrollTable), BorderLayout.CENTER);

        // Presentation buttons
        JPanel southPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton startBtn = new JButton("Start Payroll");
        startBtn.addActionListener(e -> processPayroll());

        JButton redoBtn = new JButton("Redo Payroll");
        redoBtn.addActionListener(e -> redoPayroll());

        southPanel.add(startBtn);
        southPanel.add(redoBtn);
        panel.add(southPanel, BorderLayout.SOUTH);

        return panel;
    }

    private java.util.List<Employee> loadEmployees() {
        java.util.List<Employee> list = new ArrayList<>();
        java.util.List<String[]> data = CSVHandler.readCSV("data/employees.csv");
        for (int i = 1; i < data.size(); i++) {
            String[] row = data.get(i);
            if (row.length < 4) continue;

            boolean flagged = false;
            if (row.length >= 5) {
                flagged = "1".equals(String.valueOf(row[4])) || "true".equalsIgnoreCase(String.valueOf(row[4]));
            }

            list.add(new Employee(row[0], row[1], row[2], Double.parseDouble(row[3]), flagged));
        }
        return list;
    }

    private java.util.List<Payroll> loadPayrolls() {
        java.util.List<Payroll> list = new ArrayList<>();
        java.util.List<String[]> data = CSVHandler.readCSV("data/payroll.csv");
        for (int i = 1; i < data.size(); i++) {
            String[] r = data.get(i);
            if (r.length < 7) continue;
            list.add(new Payroll(r[0], r[1], r[2],
                    Double.parseDouble(r[3]),
                    Double.parseDouble(r[4]),
                    Double.parseDouble(r[5]),
                    r[6]));
        }
        return list;
    }

    private String formatCurrencyPHP(double amount) {
        return "PHP " + String.format("%,.2f", amount);
    }

    private void addEmployee() {
        JTextField idField = new JTextField();
        JTextField nameField = new JTextField();
        JTextField posField = new JTextField();
        JTextField salaryFieldInput = new JTextField();

        Object[] fields = {
                "ID:", idField,
                "Name:", nameField,
                "Position:", posField,
                "Basic Salary:", salaryFieldInput
        };

        int res = JOptionPane.showConfirmDialog(this, fields, "Add Employee", JOptionPane.OK_CANCEL_OPTION);
        if (res != JOptionPane.OK_OPTION) return;

        String id = idField.getText().trim();
        String name = nameField.getText().trim();
        String pos = posField.getText().trim();
        double salary = Double.parseDouble(salaryFieldInput.getText().trim());

        Employee emp = new Employee(id, name, pos, salary, false);
        employees.add(emp);

        createEmployeeUserAccount(emp);
        ensurePayrollForCurrentMonth(emp);
        saveEmployees();

        dispose();
        new AdminDashboard(admin);
    }

    private void deleteSelectedEmployee() {
        int viewRow = employeeTable.getSelectedRow();
        if (viewRow < 0) return;

        int modelRow = employeeTable.convertRowIndexToModel(viewRow);
        employees.remove(modelRow);
        saveEmployees();

        dispose();
        new AdminDashboard(admin);
    }

    private void saveEmployees() {
        java.util.List<String[]> data = new ArrayList<>();
        data.add(new String[]{"id", "name", "position", "basicSalary", "flagged"});

        for (Employee emp : employees) {
            data.add(new String[]{
                    emp.getId(),
                    emp.getName(),
                    emp.getPosition(),
                    String.valueOf(emp.getBasicSalary()),
                    emp.isFlagged() ? "1" : "0"
            });
        }
        CSVHandler.writeCSV("data/employees.csv", data);
    }


    // Called by EmployeeControlPanel
    public void syncFlagToModelAndEmployee(int modelIndex, boolean flagged) {
        if (modelIndex < 0) return;
        Employee emp = employees.get(modelIndex);
        emp.setFlagged(flagged);
        saveEmployees();
        employeeModel.setValueAt(emp.isFlagged() ? 1 : 0, modelIndex, 4);
        employeeTable.repaint();
    }

    // Called by EmployeeControlPanel
    public void syncSalaryToModelAndEmployee(int modelIndex, double newSalary) {
        if (modelIndex < 0) return;
        Employee emp = employees.get(modelIndex);
        emp.setBasicSalary(newSalary);
        saveEmployees();
        employeeModel.setValueAt(formatCurrencyPHP(emp.getBasicSalary()), modelIndex, 3);
        employeeTable.repaint();
        ensurePayrollForCurrentMonth(emp);
        dispose();
        new AdminDashboard(admin);
    }

    private void createEmployeeUserAccount(Employee emp) {
        String username = "emp" + emp.getId();
        String password = "emp123";

        java.util.List<String[]> data = CSVHandler.readCSV("data/users.csv");
        boolean exists = false;
        for (int i = 1; i < data.size(); i++) {
            String[] row = data.get(i);
            if (row.length >= 1 && username.equals(row[0])) {
                exists = true;
                break;
            }
        }

        if (!exists) {
            data.add(new String[]{username, password, "Employee"});
            CSVHandler.writeCSV("data/users.csv", data);
        }
    }

    private String currentMonth() {
        Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH) + 1;
        return year + "-" + String.format("%02d", month);
    }

    private String currentDate() {
        Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH) + 1;
        int day = cal.get(Calendar.DAY_OF_MONTH);
        return String.format("%04d-%02d-%02d", year, month, day);
    }

    private void ensurePayrollForCurrentMonth(Employee emp) {
        String month = currentMonth();

        java.util.List<String[]> raw = CSVHandler.readCSV("data/payroll.csv");

        int maxId = 0;
        for (int i = 1; i < raw.size(); i++) {
            String[] row = raw.get(i);
            if (row.length >= 1) {
                try {
                    maxId = Math.max(maxId, Integer.parseInt(row[0]));
                } catch (Exception ignored) {
                }
            }
        }

        boolean found = false;
        for (int i = 1; i < raw.size(); i++) {
            String[] r = raw.get(i);
            if (r.length >= 3 && emp.getId().equals(r[1]) && month.equals(r[2])) {
                found = true;

                double deductions;
                try {
                    deductions = Double.parseDouble(r[4]);
                } catch (Exception ex) {
                    deductions = emp.getBasicSalary() * 0.0666667;
                }

                double net = emp.getBasicSalary() - deductions;
                r[3] = String.valueOf(emp.getBasicSalary());
                r[4] = String.valueOf(deductions);
                r[5] = String.valueOf(net);
                break;
            }
        }

        if (!found) {
            double deductions = emp.getBasicSalary() * 0.0666667;
            double net = emp.getBasicSalary() - deductions;

            raw.add(new String[]{
                    String.valueOf(maxId + 1),
                    emp.getId(),
                    month,
                    String.valueOf(emp.getBasicSalary()),
                    String.valueOf(deductions),
                    String.valueOf(net),
                    "Pending"
            });
        }

        CSVHandler.writeCSV("data/payroll.csv", raw);
    }

    private void savePayrolls() {
        java.util.List<String[]> data = new ArrayList<>();
        data.add(new String[]{"payrollId", "employeeId", "month", "basicSalary", "deductions", "netPay", "status"});

        payrolls = loadPayrolls();
        for (Payroll p : payrolls) {
            data.add(new String[]{
                    p.getPayrollId(),
                    p.getEmployeeId(),
                    p.getMonth(),
                    String.valueOf(p.getBasicSalary()),
                    String.valueOf(p.getDeductions()),
                    String.valueOf(p.getNetPay()),
                    p.getStatus()
            });
        }
        CSVHandler.writeCSV("data/payroll.csv", data);
    }

    private void processPayroll() {
        String month = currentMonth();
        String date = currentDate();

        payrolls = loadPayrolls();
        double totalToPay = 0.0;
        int countToProcess = 0;
        for (Payroll p : payrolls) {
            if (!month.equals(p.getMonth())) continue;
            Employee emp = findEmployeeById(p.getEmployeeId());
            if (emp != null && emp.isFlagged()) {
                continue;
            }
            if (!"Processed".equalsIgnoreCase(p.getStatus())) {
                totalToPay += p.getNetPay();
                countToProcess++;
            }
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "You are about to process payroll for " + countToProcess + " employees.\n"
                + "Total to be paid out: " + formatCurrencyPHP(totalToPay) + "\nContinue?",
                "Confirm Payroll Processing",
                JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        boolean anyChanged = false;
        for (Payroll p : payrolls) {
            if (!month.equals(p.getMonth())) continue;
            Employee emp = findEmployeeById(p.getEmployeeId());
            if (emp != null && emp.isFlagged()) {
                continue;
            }
            if (!"Processed".equalsIgnoreCase(p.getStatus())) {
                p.setStatus("Processed");
                anyChanged = true;
            }
        }
        // Always save payrolls, even if nothing changed, to guarantee CSV is up to date
        savePayrolls();

        JOptionPane.showMessageDialog(this,
                "Payroll processed for " + countToProcess + " employees.\nFlagged employees were skipped.\nTotal paid: " + formatCurrencyPHP(totalToPay) + "\nDate: " + date);

        dispose();
        new AdminDashboard(admin);
    }

    private void redoPayroll() {
        // For demonstration only: resets all payrolls for the current month to Pending
        String month = currentMonth();
        payrolls = loadPayrolls();
        int resetCount = 0;
        for (Payroll p : payrolls) {
            if (month.equals(p.getMonth())) {
                p.setStatus("Pending");
                resetCount++;
            }
        }
        savePayrolls();
        JOptionPane.showMessageDialog(this, "Payroll statuses reset to Pending for demonstration. (" + resetCount + " records)");
        dispose();
        new AdminDashboard(admin);
    }

    private Employee findEmployeeById(String empId) {
        // ensure we use latest loaded employees
        for (Employee e : employees) {
            if (String.valueOf(e.getId()).equals(String.valueOf(empId))) return e;
        }
        return null;
    }
}

