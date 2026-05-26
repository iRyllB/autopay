import javax.swing.*;
import java.awt.*;

public class EmployeeSettingsPanel extends JPanel {
    private final User employee;
    private JPasswordField passwordField;
    private JButton saveBtn;

    public EmployeeSettingsPanel(User employee) {
        this.employee = employee;
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(BorderFactory.createTitledBorder("Settings"));

        add(Box.createVerticalStrut(10));
        add(new JLabel("Change Password:"));
        passwordField = new JPasswordField(16);
        passwordField.setMaximumSize(new Dimension(200, 28));
        add(passwordField);

        add(Box.createVerticalStrut(10));
        saveBtn = new JButton("Save Changes");
        add(saveBtn);

        saveBtn.addActionListener(e -> {
            String newPass = new String(passwordField.getPassword()).trim();
            if (!newPass.isEmpty()) {
                updateEmployeePassword(employee.getUsername(), newPass);
                JOptionPane.showMessageDialog(this, "Password updated!");
            }
        });
    }

    // Update password for employee in users.csv
    private void updateEmployeePassword(String username, String newPassword) {
        java.util.List<String[]> data = CSVHandler.readCSV("data/users.csv");
        for (int i = 1; i < data.size(); i++) {
            String[] row = data.get(i);
            if (row[0].equals(username)) {
                row[1] = newPassword;
                break;
            }
        }
        CSVHandler.writeCSV("data/users.csv", data);
    }
}
