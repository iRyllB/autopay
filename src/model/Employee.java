package model;

public class Employee {
	private int id;
	private String name;
	private String position;
	private double payroll;
	private double bonus;
	private String status; // unpaid, processing, paid

	public Employee(int id, String name, String position, double payroll, double bonus, String status) {
		this.id = id;
		this.name = name;
		this.position = position;
		this.payroll = payroll;
		this.bonus = bonus;
		this.status = status;
	}

	public int getId() { return id; }
	public void setId(int id) { this.id = id; }

	public String getName() { return name; }
	public void setName(String name) { this.name = name; }

	public String getPosition() { return position; }
	public void setPosition(String position) { this.position = position; }

	public double getPayroll() { return payroll; }
	public void setPayroll(double payroll) { this.payroll = payroll; }

	public double getBonus() { return bonus; }
	public void setBonus(double bonus) { this.bonus = bonus; }

	public String getStatus() { return status; }
	public void setStatus(String status) { this.status = status; }
}
