import javax.swing.*;
import java.awt.*;

// EmployeeControlPanel: right-side panel for salary/flagged controls
public class EmployeeControlPanel extends JPanel {
    private final JTextField salaryField;
    private final JCheckBox flaggedCheckBox;
    private final JButton updateSalaryBtn;
    private AdminDashboard adminDashboard;
    private int selectedEmployeeModelIndex = -1;

    public EmployeeControlPanel(AdminDashboard adminDashboard) {
        this.adminDashboard = adminDashboard;
        setPreferredSize(new Dimension(320, 0));
        setBorder(BorderFactory.createTitledBorder("Employee Control"));
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        add(new JLabel("Select an employee row"));
        add(Box.createVerticalStrut(10));
        add(new JLabel("Basic Salary (PHP):"));
        salaryField = new JTextField();
        salaryField.setMaximumSize(new Dimension(120, 28));
        salaryField.setPreferredSize(new Dimension(120, 28));
        add(salaryField);

        add(Box.createVerticalStrut(10));
        flaggedCheckBox = new JCheckBox("Flagged (disable auto payment)");
        add(flaggedCheckBox);

        add(Box.createVerticalStrut(10));
        updateSalaryBtn = new JButton("Update Salary / Save Flag");
        add(updateSalaryBtn);

        flaggedCheckBox.addActionListener(ev -> {
            if (selectedEmployeeModelIndex < 0) return;
            adminDashboard.syncFlagToModelAndEmployee(selectedEmployeeModelIndex, flaggedCheckBox.isSelected());
        });

        updateSalaryBtn.addActionListener(ev -> {
            if (selectedEmployeeModelIndex < 0) return;
            try {
                double newSalary = Double.parseDouble(salaryField.getText().trim());
                adminDashboard.syncSalaryToModelAndEmployee(selectedEmployeeModelIndex, newSalary);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Invalid salary input.");
            }
        });

        setVisible(false);
    }

    public void updateFromEmployee(Employee emp, int modelIndex) {
        if (emp == null) {
            setVisible(false);
            selectedEmployeeModelIndex = -1;
            return;
        }
        salaryField.setText(String.valueOf(emp.getBasicSalary()));
        flaggedCheckBox.setSelected(emp.isFlagged());
        selectedEmployeeModelIndex = modelIndex;
        setVisible(true);
        revalidate();
        repaint();
    }
}
