package utils;

import model.Employee;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class PDFGenerator {
	public static void generatePayrollReceipt(Employee emp) {
		// For demo, just generate a text file as a mock PDF
		String fileName = "receipt_" + emp.getId() + ".pdf";
		try (PrintWriter pw = new PrintWriter(new FileWriter(fileName))) {
			pw.println("PAYROLL RECEIPT");
			pw.println("Date: " + new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
			pw.println("------------------------------");
			pw.println("Employee ID: " + emp.getId());
			pw.println("Name: " + emp.getName());
			pw.println("Position: " + emp.getPosition());
			pw.println("Payroll: PHP " + emp.getPayroll());
			pw.println("Bonus: PHP " + emp.getBonus());
			pw.println("Status: " + emp.getStatus());
			pw.println("------------------------------");
			pw.println("This is a mock PDF. No actual email sent.");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
