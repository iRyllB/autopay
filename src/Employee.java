// Represents an employee
public class Employee {
    private String id;
    private String name;
    private String position;
    private double basicSalary;
    private boolean flagged;

    public Employee(String id, String name, String position, double basicSalary) {
        this(id, name, position, basicSalary, false);
    }

    public Employee(String id, String name, String position, double basicSalary, boolean flagged) {
        this.id = id;
        this.name = name;
        this.position = position;
        this.basicSalary = basicSalary;
        this.flagged = flagged;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getPosition() { return position; }
    public double getBasicSalary() { return basicSalary; }
    public boolean isFlagged() { return flagged; }

    public void setName(String name) { this.name = name; }
    public void setPosition(String position) { this.position = position; }
    public void setBasicSalary(double basicSalary) { this.basicSalary = basicSalary; }
    public void setFlagged(boolean flagged) { this.flagged = flagged; }
}

