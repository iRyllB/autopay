package services;

import model.Employee;
import java.io.*;
import java.util.*;

public class EmployeeService {
	private static final String CSV_PATH = "src/database/employees.csv";
	private List<Employee> employees;

	public EmployeeService() {
		employees = loadEmployees();
	}

	private List<Employee> loadEmployees() {
		List<Employee> list = new ArrayList<>();
		try (BufferedReader br = new BufferedReader(new FileReader(CSV_PATH))) {
			String line = br.readLine(); // skip header
			while ((line = br.readLine()) != null) {
				String[] parts = line.split(",");
				if (parts.length < 4) continue;
				int id = Integer.parseInt(parts[0]);
				String name = parts[1];
				String position = parts[2];
				double payroll = Double.parseDouble(parts[3]);
				double bonus = parts.length > 4 ? Double.parseDouble(parts[4]) : 0.0;
				String status = parts.length > 5 ? parts[5] : "unpaid";
				list.add(new Employee(id, name, position, payroll, bonus, status));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return list;
	}

	public List<Employee> getAllEmployees() {
		return employees;
	}

	public Employee getEmployeeById(int id) {
		for (Employee e : employees) {
			if (e.getId() == id) return e;
		}
		return null;
	}

	public void updateEmployeePayroll(int id, double payroll, double bonus, String status) {
		for (Employee e : employees) {
			if (e.getId() == id) {
				e.setPayroll(payroll);
				e.setBonus(bonus);
				e.setStatus(status);
				break;
			}
		}
		saveEmployees();
	}

	private void saveEmployees() {
		try (PrintWriter pw = new PrintWriter(new FileWriter(CSV_PATH))) {
			pw.println("id,name,position,salary,bonus,status");
			for (Employee e : employees) {
				pw.printf("%d,%s,%s,%.2f,%.2f,%s\n",
						e.getId(), e.getName(), e.getPosition(), e.getPayroll(), e.getBonus(), e.getStatus());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
