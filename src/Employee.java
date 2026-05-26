// Represents an employee
public class Employee {
    private String id;
    private String firstName;
    private String lastName;
    private String position;
    private double basicSalary;
    private boolean flagged;

    public Employee(String id, String firstName, String lastName, String position, double basicSalary) {
        this(id, firstName, lastName, position, basicSalary, false);
    }

    public Employee(String id, String firstName, String lastName, String position, double basicSalary, boolean flagged) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.position = position;
        this.basicSalary = basicSalary;
        this.flagged = flagged;
    }

    public String getId() { return id; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }

    public String getName() {
        if (lastName == null || lastName.trim().isEmpty()) return String.valueOf(firstName).trim();
        return String.valueOf(firstName).trim() + " " + String.valueOf(lastName).trim();
    }
    public String getPosition() { return position; }
    public double getBasicSalary() { return basicSalary; }
    public boolean isFlagged() { return flagged; }

    public void setFirstName(String firstName) { this.firstName = firstName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public void setName(String name) {
        if (name == null) {
            this.firstName = "";
            this.lastName = "";
            return;
        }
        String trimmed = name.trim();
        if (trimmed.isEmpty()) {
            this.firstName = "";
            this.lastName = "";
            return;
        }
        String[] parts = trimmed.split("\\s+");
        this.firstName = parts[0];
        this.lastName = (parts.length <= 1) ? "" : String.join(" ", java.util.Arrays.copyOfRange(parts, 1, parts.length));
    }
    public void setPosition(String position) { this.position = position; }
    public void setBasicSalary(double basicSalary) { this.basicSalary = basicSalary; }
    public void setFlagged(boolean flagged) { this.flagged = flagged; }
}

