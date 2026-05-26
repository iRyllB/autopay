// Represents an employee
public class Employee {
    private String id;
    private String name;
    private String position;
    private double basicSalary;

    public Employee(String id, String name, String position, double basicSalary) {
        this.id = id;
        this.name = name;
        this.position = position;
        this.basicSalary = basicSalary;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getPosition() { return position; }
    public double getBasicSalary() { return basicSalary; }

    public void setName(String name) { this.name = name; }
    public void setPosition(String position) { this.position = position; }
    public void setBasicSalary(double basicSalary) { this.basicSalary = basicSalary; }
}
