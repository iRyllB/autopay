import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.Calendar;

// Centralized CSV storage for employees + users + payroll in one file.
public class DataStore {
    public static final String MASTER_PATH = "data/master.csv";

    // Schema (one row per payroll record; users/employees duplicated per row for simplicity)
    // employeeId,firstName,lastName,position,basicSalary,flagged,username,password,role,payrollId,month,payBasicSalary,deductions,netPay,payStatus
    private static final String[] HEADER = new String[]{
            "employeeId", "firstName", "lastName", "position", "basicSalary", "flagged",
            "username", "password", "role",
            "payrollId", "month", "payBasicSalary", "deductions", "netPay", "payStatus"
    };

    public static String currentMonth() {
        Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH) + 1;
        return year + "-" + String.format("%02d", month);
    }

    public static void ensureInitialized(String currentMonth) {
        try {
            Path p = Paths.get(MASTER_PATH);
            if (Files.exists(p)) {
                List<String[]> existing = CSVHandler.readCSV(MASTER_PATH);
                if (existing.size() > 1) return; // already initialized with data
            }

            List<String[]> out = new ArrayList<>();
            out.add(HEADER);

            // Admin user
            List<String[]> users = CSVHandler.readCSV("data/users.csv");
            String adminUser = "admin";
            String adminPass = "admin123";
            for (int i = 1; i < users.size(); i++) {
                String[] r = users.get(i);
                if (r.length >= 3 && "Admin".equalsIgnoreCase(String.valueOf(r[2]).trim())) {
                    adminUser = String.valueOf(r[0]).trim();
                    adminPass = String.valueOf(r[1]);
                    break;
                }
            }
            out.add(new String[]{"", "", "", "", "", "", adminUser, adminPass, "Admin", "", "", "", "", "", ""});

            // Load employees
            List<Employee> employees = loadEmployeesFromLegacy();
            Map<String, String[]> userByUsername = new HashMap<>();
            for (int i = 1; i < users.size(); i++) {
                String[] r = users.get(i);
                if (r.length >= 3) userByUsername.put(String.valueOf(r[0]).trim(), r);
            }

            // Load payroll by employeeId+month
            Map<String, String[]> payrollByEmpMonth = new HashMap<>();
            List<String[]> payroll = CSVHandler.readCSV("data/payroll.csv");
            for (int i = 1; i < payroll.size(); i++) {
                String[] r = payroll.get(i);
                if (r.length < 7) continue;
                payrollByEmpMonth.put(key(r[1], r[2]), r);
            }

            for (Employee e : employees) {
                String username = (e.getFirstName() == null) ? "" : e.getFirstName().trim();
                String password = username + "123";
                String role = "Employee";
                String[] ur = userByUsername.get(username);
                if (ur != null && ur.length >= 3) {
                    password = ur[1];
                    role = ur[2];
                }

                // Ensure current month payroll exists; if legacy has it, keep values.
                String[] pr = payrollByEmpMonth.get(key(e.getId(), currentMonth));
                if (pr == null) {
                    // Create a default payroll row
                    double deductions = e.getBasicSalary() * 0.0666667;
                    double net = e.getBasicSalary() - deductions;
                    out.add(new String[]{
                            e.getId(), e.getFirstName(), e.getLastName(), e.getPosition(),
                            String.valueOf(e.getBasicSalary()), e.isFlagged() ? "1" : "0",
                            username, password, role,
                            "", currentMonth, String.valueOf(e.getBasicSalary()),
                            String.valueOf(deductions), String.valueOf(net), "Pending"
                    });
                } else {
                    out.add(new String[]{
                            e.getId(), e.getFirstName(), e.getLastName(), e.getPosition(),
                            String.valueOf(e.getBasicSalary()), e.isFlagged() ? "1" : "0",
                            username, password, role,
                            pr[0], pr[2], pr[3], pr[4], pr[5], pr[6]
                    });
                }
            }

            CSVHandler.writeCSV(MASTER_PATH, out);
        } catch (Exception ignored) {
        }
    }

    public static List<User> loadUsers(String currentMonth) {
        ensureInitialized(currentMonth);
        List<User> out = new ArrayList<>();
        List<String[]> data = CSVHandler.readCSV(MASTER_PATH);
        Set<String> seen = new HashSet<>();
        for (int i = 1; i < data.size(); i++) {
            String[] r = data.get(i);
            if (r.length < 9) continue;
            String username = String.valueOf(r[6]).trim();
            String password = r[7];
            String role = r[8];
            if (username.isEmpty() || seen.contains(username)) continue;
            seen.add(username);
            out.add(new User(username, password, role));
        }
        return out;
    }

    public static void updateUserPassword(String currentMonth, String username, String newPassword) {
        ensureInitialized(currentMonth);
        List<String[]> data = CSVHandler.readCSV(MASTER_PATH);
        String u = username == null ? "" : username.trim();
        for (int i = 1; i < data.size(); i++) {
            String[] r = data.get(i);
            if (r.length >= 8 && u.equals(String.valueOf(r[6]).trim())) {
                r[7] = newPassword;
            }
        }
        CSVHandler.writeCSV(MASTER_PATH, data);
    }

    public static List<Employee> loadEmployees(String currentMonth) {
        ensureInitialized(currentMonth);
        List<Employee> out = new ArrayList<>();
        List<String[]> data = CSVHandler.readCSV(MASTER_PATH);
        Map<String, String[]> latest = new LinkedHashMap<>();
        for (int i = 1; i < data.size(); i++) {
            String[] r = data.get(i);
            if (r.length < 9) continue;
            String role = String.valueOf(r[8]).trim();
            if (!"Employee".equalsIgnoreCase(role)) continue;
            String empId = String.valueOf(r[0]).trim();
            if (empId.isEmpty()) continue;
            latest.put(empId, r);
        }
        for (String[] r : latest.values()) {
            boolean flagged = "1".equals(String.valueOf(r[5]).trim()) || "true".equalsIgnoreCase(String.valueOf(r[5]).trim());
            double salary = parseDoubleSafe(r[4]);
            out.add(new Employee(r[0], r[1], r[2], r[3], salary, flagged));
        }
        return out;
    }

    public static Employee findEmployeeByUsername(String currentMonth, String username) {
        ensureInitialized(currentMonth);
        List<String[]> data = CSVHandler.readCSV(MASTER_PATH);
        String u = username == null ? "" : username.trim();
        for (int i = 1; i < data.size(); i++) {
            String[] r = data.get(i);
            if (r.length < 9) continue;
            if (!"Employee".equalsIgnoreCase(String.valueOf(r[8]).trim())) continue;
            if (u.equals(String.valueOf(r[6]).trim())) {
                boolean flagged = "1".equals(String.valueOf(r[5]).trim()) || "true".equalsIgnoreCase(String.valueOf(r[5]).trim());
                double salary = parseDoubleSafe(r[4]);
                return new Employee(r[0], r[1], r[2], r[3], salary, flagged);
            }
        }
        return null;
    }

    public static List<Payroll> loadPayrolls(String currentMonth) {
        ensureInitialized(currentMonth);
        List<Payroll> out = new ArrayList<>();
        List<String[]> data = CSVHandler.readCSV(MASTER_PATH);
        for (int i = 1; i < data.size(); i++) {
            String[] r = data.get(i);
            if (r.length < 15) continue;
            if (!"Employee".equalsIgnoreCase(String.valueOf(r[8]).trim())) continue;
            String payrollId = String.valueOf(r[9]).trim();
            String month = String.valueOf(r[10]).trim();
            String status = String.valueOf(r[14]).trim();
            if (month.isEmpty()) continue;
            // allow empty payrollId (will be assigned on ensurePayrollForCurrentMonth)
            out.add(new Payroll(
                    payrollId.isEmpty() ? "" : payrollId,
                    String.valueOf(r[0]).trim(),
                    month,
                    parseDoubleSafe(r[11]),
                    parseDoubleSafe(r[12]),
                    parseDoubleSafe(r[13]),
                    status.isEmpty() ? "Pending" : status
            ));
        }
        return out;
    }

    public static List<Payroll> loadPayrollsForEmployee(String currentMonth, String employeeId) {
        List<Payroll> all = loadPayrolls(currentMonth);
        List<Payroll> out = new ArrayList<>();
        for (Payroll p : all) {
            if (String.valueOf(employeeId).equals(String.valueOf(p.getEmployeeId()))) out.add(p);
        }
        return out;
    }

    public static void upsertEmployeeAndEnsurePayroll(String currentMonth, Employee emp) {
        ensureInitialized(currentMonth);
        List<String[]> data = CSVHandler.readCSV(MASTER_PATH);

        String empId = String.valueOf(emp.getId()).trim();
        String username = emp.getFirstName() == null ? "" : emp.getFirstName().trim();

        // Update employee fields across all rows for that employeeId
        boolean hasAnyRow = false;
        for (int i = 1; i < data.size(); i++) {
            String[] r = data.get(i);
            if (r.length < 15) continue;
            if (empId.equals(String.valueOf(r[0]).trim())) {
                r[1] = emp.getFirstName();
                r[2] = emp.getLastName();
                r[3] = emp.getPosition();
                r[4] = String.valueOf(emp.getBasicSalary());
                r[5] = emp.isFlagged() ? "1" : "0";
                r[6] = username;
                if (String.valueOf(r[8]).trim().isEmpty()) r[8] = "Employee";
                hasAnyRow = true;
            }
        }

        if (!hasAnyRow) {
            // Create a new row for current month payroll
            String password = username + "123";
            int nextPayrollId = nextPayrollId(data);
            double deductions = emp.getBasicSalary() * 0.0666667;
            double net = emp.getBasicSalary() - deductions;
            data.add(new String[]{
                    empId, emp.getFirstName(), emp.getLastName(), emp.getPosition(),
                    String.valueOf(emp.getBasicSalary()), emp.isFlagged() ? "1" : "0",
                    username, password, "Employee",
                    String.valueOf(nextPayrollId), currentMonth,
                    String.valueOf(emp.getBasicSalary()), String.valueOf(deductions), String.valueOf(net), "Pending"
            });
            CSVHandler.writeCSV(MASTER_PATH, data);
            return;
        }

        // Ensure current month payroll row exists and amounts are current
        boolean foundMonth = false;
        for (int i = 1; i < data.size(); i++) {
            String[] r = data.get(i);
            if (r.length < 15) continue;
            if (!empId.equals(String.valueOf(r[0]).trim())) continue;
            if (!currentMonth.equals(String.valueOf(r[10]).trim())) continue;
            foundMonth = true;

            if (String.valueOf(r[9]).trim().isEmpty()) {
                r[9] = String.valueOf(nextPayrollId(data));
            }
            double deductions = emp.getBasicSalary() * 0.0666667;
            double net = emp.getBasicSalary() - deductions;
            r[11] = String.valueOf(emp.getBasicSalary());
            r[12] = String.valueOf(deductions);
            r[13] = String.valueOf(net);
            if (String.valueOf(r[14]).trim().isEmpty()) r[14] = "Pending";
        }
        if (!foundMonth) {
            int nextPayrollId = nextPayrollId(data);
            double deductions = emp.getBasicSalary() * 0.0666667;
            double net = emp.getBasicSalary() - deductions;
            // Find existing password (if any) from another row
            String password = username + "123";
            for (int i = 1; i < data.size(); i++) {
                String[] r = data.get(i);
                if (r.length < 15) continue;
                if (empId.equals(String.valueOf(r[0]).trim())) {
                    password = r[7];
                    break;
                }
            }
            data.add(new String[]{
                    empId, emp.getFirstName(), emp.getLastName(), emp.getPosition(),
                    String.valueOf(emp.getBasicSalary()), emp.isFlagged() ? "1" : "0",
                    username, password, "Employee",
                    String.valueOf(nextPayrollId), currentMonth,
                    String.valueOf(emp.getBasicSalary()), String.valueOf(deductions), String.valueOf(net), "Pending"
            });
        }

        CSVHandler.writeCSV(MASTER_PATH, data);
    }

    public static void deleteEmployee(String currentMonth, String employeeId) {
        ensureInitialized(currentMonth);
        List<String[]> data = CSVHandler.readCSV(MASTER_PATH);
        String id = String.valueOf(employeeId).trim();
        List<String[]> out = new ArrayList<>();
        if (!data.isEmpty()) out.add(data.get(0));
        for (int i = 1; i < data.size(); i++) {
            String[] r = data.get(i);
            if (r.length >= 1 && id.equals(String.valueOf(r[0]).trim())) continue;
            out.add(r);
        }
        CSVHandler.writeCSV(MASTER_PATH, out);
    }

    public static void savePayrollStatuses(String currentMonth, List<Payroll> payrolls) {
        ensureInitialized(currentMonth);
        Map<String, String> statusByKey = new HashMap<>();
        for (Payroll p : payrolls) {
            statusByKey.put(key(p.getEmployeeId(), p.getMonth()), p.getStatus());
        }
        List<String[]> data = CSVHandler.readCSV(MASTER_PATH);
        for (int i = 1; i < data.size(); i++) {
            String[] r = data.get(i);
            if (r.length < 15) continue;
            if (!"Employee".equalsIgnoreCase(String.valueOf(r[8]).trim())) continue;
            String k = key(r[0], r[10]);
            if (statusByKey.containsKey(k)) {
                r[14] = statusByKey.get(k);
            }
        }
        CSVHandler.writeCSV(MASTER_PATH, data);
    }

    private static int nextPayrollId(List<String[]> data) {
        int max = 0;
        for (int i = 1; i < data.size(); i++) {
            String[] r = data.get(i);
            if (r.length >= 10) {
                try {
                    String v = String.valueOf(r[9]).trim();
                    if (!v.isEmpty()) max = Math.max(max, Integer.parseInt(v));
                } catch (Exception ignored) {
                }
            }
        }
        return max + 1;
    }

    private static String key(Object employeeId, Object month) {
        return String.valueOf(employeeId).trim() + "@" + String.valueOf(month).trim();
    }

    private static double parseDoubleSafe(Object s) {
        try {
            return Double.parseDouble(String.valueOf(s).trim());
        } catch (Exception e) {
            return 0.0;
        }
    }

    private static List<Employee> loadEmployeesFromLegacy() {
        List<Employee> list = new ArrayList<>();
        List<String[]> data = CSVHandler.readCSV("data/employees.csv");
        if (data.isEmpty()) return list;
        String[] header = data.get(0);
        boolean newFormat = header.length >= 6
                && "id".equalsIgnoreCase(header[0])
                && "firstname".equalsIgnoreCase(header[1])
                && "lastname".equalsIgnoreCase(header[2]);
        for (int i = 1; i < data.size(); i++) {
            String[] row = data.get(i);
            if (row.length < 4) continue;
            boolean flagged = false;
            if (newFormat) {
                if (row.length >= 6) {
                    flagged = "1".equals(String.valueOf(row[5]).trim()) || "true".equalsIgnoreCase(String.valueOf(row[5]).trim());
                }
                double salary = parseDoubleSafe(row[4]);
                list.add(new Employee(row[0], row[1], row[2], row[3], salary, flagged));
            } else {
                String fullName = String.valueOf(row[1]).trim();
                String[] parts = fullName.isEmpty() ? new String[0] : fullName.split("\\s+");
                String firstName = (parts.length >= 1) ? parts[0] : "";
                String lastName = (parts.length <= 1) ? "" : String.join(" ", Arrays.copyOfRange(parts, 1, parts.length));
                if (row.length >= 5) {
                    flagged = "1".equals(String.valueOf(row[4]).trim()) || "true".equalsIgnoreCase(String.valueOf(row[4]).trim());
                }
                double salary = parseDoubleSafe(row[3]);
                list.add(new Employee(row[0], firstName, lastName, row[2], salary, flagged));
            }
        }
        return list;
    }
}
