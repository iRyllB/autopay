import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class EmployeeDashboard extends JFrame {
    private User employeeUser;
    private Employee employee;
    private java.util.List<Payroll> payrolls;
    private boolean hasPayoutAccount;

    public EmployeeDashboard(User user) {

        this.employeeUser = user;
        setTitle("Employee Dashboard");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(780, 540);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // Load employee info first
        employee = DataStore.findEmployeeByUsername(DataStore.currentMonth(), user.getUsername());
        payrolls = (employee == null) ? new ArrayList<>() : DataStore.loadPayrollsForEmployee(DataStore.currentMonth(), employee.getId());
        hasPayoutAccount = employee != null && hasPayoutAccount(employee.getId());

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

        if (employee != null && !hasPayoutAccount) {
            JOptionPane.showMessageDialog(this,
                    "You have no payout account; please setup payout account.",
                    "Missing Payout Account",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private String formatCurrency(double amount) {
        return "₱" + String.format("%,.2f", amount);
    }

    private JPanel buildPayrollTab() {
        JPanel root = new JPanel(new BorderLayout());

        JPanel north = new JPanel();
        north.setLayout(new BoxLayout(north, BoxLayout.Y_AXIS));
        north.setOpaque(false);

        if (!hasPayoutAccount) {
            JPanel warnPanel = new JPanel(new BorderLayout());
            warnPanel.setBackground(new Color(255, 230, 230));
            warnPanel.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(200, 60, 60)),
                    BorderFactory.createEmptyBorder(10, 10, 10, 10)
            ));
            JLabel warn = new JLabel("You have no payout account; please setup payout account.");
            warn.setForeground(new Color(150, 0, 0));
            warnPanel.add(warn, BorderLayout.CENTER);
            north.add(warnPanel);
        }

        // Actions (print/export)
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT));
        actions.add(new JLabel("Payslip:"));
        JComboBox<String> monthSelect = new JComboBox<>();
        if (payrolls != null) {
            java.util.Set<String> months = new java.util.LinkedHashSet<>();
            for (Payroll p : payrolls) months.add(p.getMonth());
            for (String m : months) monthSelect.addItem(m);
        }
        actions.add(monthSelect);

        JButton printBtn = new JButton("Print / Export PDF");
        printBtn.addActionListener(e -> {
            String month = String.valueOf(monthSelect.getSelectedItem());
            Payroll p = findPayrollByMonth(month);
            if (p == null) {
                JOptionPane.showMessageDialog(this, "No payroll record found for selected month.");
                return;
            }
            try {
                Path out = exportPayslipHtml(p);
                openInBrowser(out);
                JOptionPane.showMessageDialog(this,
                        "Opened payslip in your browser.\nUse your browser Print -> Save as PDF.",
                        "Payslip Ready",
                        JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Failed to export payslip: " + ex.getMessage());
            }
        });
        actions.add(printBtn);

        north.add(actions);
        root.add(north, BorderLayout.NORTH);

        JPanel listPanel = new JPanel();
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
        listPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        listPanel.setBackground(new Color(245, 245, 245));
        listPanel.setOpaque(true);

        if (payrolls == null || payrolls.isEmpty()) {
            JLabel empty = new JLabel("No payroll records found.");
            empty.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
            root.add(empty, BorderLayout.NORTH);
            return root;
        }

        for (Payroll p : payrolls) {
            JPanel card = buildPayrollCard(p);

            JPanel centered = new JPanel(new GridBagLayout());
            centered.setOpaque(false);
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.anchor = GridBagConstraints.NORTH;
            gbc.insets = new Insets(0, 0, 0, 0);
            centered.add(card, gbc);

            listPanel.add(centered);
            listPanel.add(Box.createVerticalStrut(10));
        }

        JScrollPane scroll = new JScrollPane(listPanel);
        scroll.getViewport().setBackground(new Color(245, 245, 245));
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
        card.setOpaque(true);
        card.setBackground(Color.WHITE);
        card.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.setMaximumSize(new Dimension(640, Integer.MAX_VALUE));

        JPanel topRow = new JPanel(new BorderLayout());
        topRow.setOpaque(false);
        JLabel monthLabel = new JLabel("Month: " + p.getMonth());
        monthLabel.setFont(monthLabel.getFont().deriveFont(Font.BOLD));
        topRow.add(monthLabel, BorderLayout.WEST);
        String statusToShow = hasPayoutAccount ? p.getStatus() : "No account";
        topRow.add(buildStatusBadge(statusToShow), BorderLayout.EAST);
        card.add(topRow);

        card.add(Box.createVerticalStrut(8));
        card.add(new JLabel("Basic Salary: " + formatCurrency(p.getBasicSalary())));
        card.add(new JLabel("Deductions: " + formatCurrency(p.getDeductions())));
        card.add(new JLabel("Net Pay: " + formatCurrency(p.getNetPay())));

        if (hasPayoutAccount && "Processed".equalsIgnoreCase(String.valueOf(p.getStatus()).trim())) {
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
        boolean noAccount = "No account".equalsIgnoreCase(normalized) || "No payout account".equalsIgnoreCase(normalized);

        JLabel badge = new JLabel(noAccount ? "NO ACCOUNT" : (processed ? "PROCESSED" : "PENDING"));
        badge.setOpaque(true);
        badge.setForeground(Color.WHITE);
        badge.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
        badge.setFont(badge.getFont().deriveFont(Font.BOLD, 12f));
        badge.setBackground(noAccount ? new Color(90, 90, 90) : (processed ? new Color(34, 139, 34) : new Color(190, 70, 70)));
        return badge;
    }

    private boolean hasPayoutAccount(String employeeId) {
        java.util.List<String[]> data = CSVHandler.readCSV("data/payout_accounts.csv");
        for (int i = 1; i < data.size(); i++) {
            String[] row = data.get(i);
            if (row.length >= 1 && String.valueOf(employeeId).equals(String.valueOf(row[0]))) {
                return true;
            }
        }
        return false;
    }

    private Payroll findPayrollByMonth(String month) {
        if (month == null || payrolls == null) return null;
        for (Payroll p : payrolls) {
            if (month.equals(p.getMonth())) return p;
        }
        return null;
    }

    private Path exportPayslipHtml(Payroll p) throws IOException {
        Path exportsDir = Paths.get("exports");
        Files.createDirectories(exportsDir);

        String ts = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String safeMonth = String.valueOf(p.getMonth()).replaceAll("[^0-9A-Za-z\\-]", "_");
        String fileName = "payslip_emp" + employee.getId() + "_" + safeMonth + "_" + ts + ".html";
        Path out = exportsDir.resolve(fileName);

        String statusText = hasPayoutAccount ? String.valueOf(p.getStatus()) : "No account";
        String note = ("Processed".equalsIgnoreCase(String.valueOf(p.getStatus()).trim()) && hasPayoutAccount)
                ? "<p><em>Payment has been sent; it may take a while to reflect on your payroll account.</em></p>"
                : "";

        String html = ""
                + "<!doctype html><html><head><meta charset=\"utf-8\">"
                + "<title>Payslip</title>"
                + "<style>"
                + "body{font-family:Arial,Helvetica,sans-serif;margin:24px;background:#f5f5f5;}"
                + ".card{background:#fff;border:1px solid #ddd;border-radius:6px;padding:18px;max-width:720px;margin:0 auto;}"
                + ".row{display:flex;justify-content:space-between;gap:12px;}"
                + ".badge{padding:6px 10px;border-radius:999px;color:#fff;font-weight:700;font-size:12px;display:inline-block;}"
                + ".pending{background:#be4646;} .processed{background:#228b22;} .noacct{background:#5a5a5a;}"
                + "table{width:100%;border-collapse:collapse;margin-top:12px;}"
                + "td{padding:8px 0;border-bottom:1px solid #eee;} td:first-child{color:#555;}"
                + "</style></head><body>"
                + "<div class=\"card\">"
                + "<div class=\"row\"><h2 style=\"margin:0\">Payslip</h2>"
                + "<div class=\"badge " + (statusText.equalsIgnoreCase("Processed") ? "processed" : (statusText.equalsIgnoreCase("Pending") ? "pending" : "noacct")) + "\">"
                + statusText.toUpperCase() + "</div></div>"
                + "<p><strong>Employee:</strong> " + escapeHtml(employee.getName()) + " (ID " + escapeHtml(employee.getId()) + ")</p>"
                + "<p><strong>Month:</strong> " + escapeHtml(p.getMonth()) + "</p>"
                + "<table>"
                + "<tr><td>Basic Salary</td><td style=\"text-align:right\">" + escapeHtml(formatCurrency(p.getBasicSalary())) + "</td></tr>"
                + "<tr><td>Deductions</td><td style=\"text-align:right\">" + escapeHtml(formatCurrency(p.getDeductions())) + "</td></tr>"
                + "<tr><td><strong>Net Pay</strong></td><td style=\"text-align:right\"><strong>" + escapeHtml(formatCurrency(p.getNetPay())) + "</strong></td></tr>"
                + "</table>"
                + note
                + "<p style=\"color:#777;font-size:12px;margin-top:14px\">Generated: " + escapeHtml(ts) + "</p>"
                + "</div></body></html>";

        Files.writeString(out, html, StandardCharsets.UTF_8);
        return out;
    }

    private void openInBrowser(Path file) throws IOException {
        if (!Desktop.isDesktopSupported()) throw new IOException("Desktop integration not supported.");
        Desktop.getDesktop().browse(file.toUri());
    }

    private static String escapeHtml(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }

    // Employee and payroll data are loaded via DataStore (data/master.csv)
}
