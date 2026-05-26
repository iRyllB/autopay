// Represents a payroll record
public class Payroll {
    private String payrollId;
    private String employeeId;
    private String month;
    private double basicSalary;
    private double deductions;
    private double netPay;
    private String status; // Processed or Pending

    public Payroll(String payrollId, String employeeId, String month, double basicSalary, double deductions, double netPay, String status) {
        this.payrollId = payrollId;
        this.employeeId = employeeId;
        this.month = month;
        this.basicSalary = basicSalary;
        this.deductions = deductions;
        this.netPay = netPay;
        this.status = status;
    }

    public String getPayrollId() { return payrollId; }
    public String getEmployeeId() { return employeeId; }
    public String getMonth() { return month; }
    public double getBasicSalary() { return basicSalary; }
    public double getDeductions() { return deductions; }
    public double getNetPay() { return netPay; }
    public String getStatus() { return status; }

    public void setStatus(String status) { this.status = status; }
}
