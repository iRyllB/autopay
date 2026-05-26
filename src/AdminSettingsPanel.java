import javax.swing.*;
import java.awt.*;

public class AdminSettingsPanel extends JPanel {
    private final User admin;
    private JTextField cardField;
    private JPasswordField passwordField;
    private JButton saveBtn;

    public AdminSettingsPanel(User admin) {
        this.admin = admin;
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(BorderFactory.createTitledBorder("Settings"));

        add(Box.createVerticalStrut(10));
        add(new JLabel("Change Password:"));
        passwordField = new JPasswordField(16);
        passwordField.setMaximumSize(new Dimension(200, 28));
        add(passwordField);

        add(Box.createVerticalStrut(10));
        add(new JLabel("Payment Method (Demo):"));
        cardField = new JTextField("Card: 1234 5678 9012 3456, Exp: 12/34, CVV: 123");
        cardField.setMaximumSize(new Dimension(250, 28));
        add(cardField);

        add(Box.createVerticalStrut(10));
        saveBtn = new JButton("Save Changes");
        add(saveBtn);

        saveBtn.addActionListener(e -> {
            String newPass = new String(passwordField.getPassword()).trim();
            if (!newPass.isEmpty()) {
                AdminDashboard.updateAdminPassword(admin.getUsername(), newPass);
                JOptionPane.showMessageDialog(this, "Password updated!");
            }
        });
    }
}
