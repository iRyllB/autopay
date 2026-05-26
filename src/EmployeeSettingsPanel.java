import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

public class EmployeeSettingsPanel extends JPanel {
    private final User employeeUser;
    private final Employee employeeModel;
    private JPasswordField passwordField;
    private JButton saveBtn;

    public EmployeeSettingsPanel(User employeeUser, Employee employeeModel) {
        this.employeeUser = employeeUser;
        this.employeeModel = employeeModel;
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
                updateEmployeePassword(employeeUser.getUsername(), newPass);
                JOptionPane.showMessageDialog(this, "Password updated!");
            }
        });

        add(Box.createVerticalStrut(20));
        add(payoutAccountPanel());
    }

    // Update password for employee in users.csv
    private void updateEmployeePassword(String username, String newPassword) {
        java.util.List<String[]> data = CSVHandler.readCSV("data/users.csv");
        for (int i = 1; i < data.size(); i++) {
            String[] row = data.get(i);
            if (row.length >= 1 && row[0] != null && row[0].trim().equals(username == null ? "" : username.trim())) {
                row[1] = newPassword;
                break;
            }
        }
        CSVHandler.writeCSV("data/users.csv", data);
    }

    private JPanel payoutAccountPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createTitledBorder("Payout Account (Demo)"));

        JLabel statusLabel = new JLabel();
        JButton addUpdateBtn = new JButton("Add / Update Card");
        JButton removeBtn = new JButton("Remove Card");

        Runnable refreshStatus = () -> {
            String[] acct = getPayoutAccount(employeeModel == null ? null : employeeModel.getId());
            if (acct == null) {
                statusLabel.setText("Complete or add payout account.");
                removeBtn.setEnabled(false);
            } else {
                String masked = maskCard(acct[1]);
                String expiry = acct[2];
                statusLabel.setText("Card on file: " + masked + " (Exp: " + expiry + ")");
                removeBtn.setEnabled(true);
            }
        };

        addUpdateBtn.addActionListener(e -> {
            if (employeeModel == null) {
                JOptionPane.showMessageDialog(this, "Employee record not found; cannot save payout account.");
                return;
            }
            JTextField cardNumberField = new JTextField();
            JTextField expiryField = new JTextField("12/34");
            JTextField cvvField = new JTextField("123");

            Object[] fields = new Object[]{
                    "Card Number (digits only):", cardNumberField,
                    "Expiry (MM/YY):", expiryField,
                    "CVV:", cvvField
            };

            int res = JOptionPane.showConfirmDialog(this, fields, "Add / Update Card (Demo)", JOptionPane.OK_CANCEL_OPTION);
            if (res != JOptionPane.OK_OPTION) return;

            String cardNumber = cardNumberField.getText().trim().replaceAll("\\s+", "");
            String expiry = expiryField.getText().trim();
            String cvv = cvvField.getText().trim();

            if (!cardNumber.matches("\\d{12,19}")) {
                JOptionPane.showMessageDialog(this, "Card number must be 12 to 19 digits.");
                return;
            }
            if (!expiry.matches("\\d{2}/\\d{2}")) {
                JOptionPane.showMessageDialog(this, "Expiry must be in MM/YY format.");
                return;
            }
            if (!cvv.matches("\\d{3,4}")) {
                JOptionPane.showMessageDialog(this, "CVV must be 3 or 4 digits.");
                return;
            }

            upsertPayoutAccount(employeeModel.getId(), cardNumber, expiry, cvv);
            refreshStatus.run();
            JOptionPane.showMessageDialog(this, "Payout account saved (demo).");
        });

        removeBtn.addActionListener(e -> {
            if (employeeModel == null) return;
            int confirm = JOptionPane.showConfirmDialog(this, "Remove saved payout account?", "Confirm", JOptionPane.YES_NO_OPTION);
            if (confirm != JOptionPane.YES_OPTION) return;
            deletePayoutAccount(employeeModel.getId());
            refreshStatus.run();
            JOptionPane.showMessageDialog(this, "Payout account removed.");
        });

        panel.add(statusLabel);
        panel.add(Box.createVerticalStrut(8));
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.LEFT));
        btnRow.add(addUpdateBtn);
        btnRow.add(removeBtn);
        panel.add(btnRow);

        refreshStatus.run();
        return panel;
    }

    private static String maskCard(String cardNumber) {
        if (cardNumber == null) return "****";
        String digits = cardNumber.replaceAll("\\s+", "");
        if (digits.length() <= 4) return "**** " + digits;
        return "**** **** **** " + digits.substring(digits.length() - 4);
    }

    private static String[] getPayoutAccount(String employeeId) {
        if (employeeId == null) return null;
        java.util.List<String[]> data = CSVHandler.readCSV("data/payout_accounts.csv");
        for (int i = 1; i < data.size(); i++) {
            String[] row = data.get(i);
            if (row.length >= 4 && employeeId.equals(row[0])) return row;
        }
        return null;
    }

    private static void upsertPayoutAccount(String employeeId, String cardNumber, String expiry, String cvv) {
        java.util.List<String[]> data = CSVHandler.readCSV("data/payout_accounts.csv");
        if (data.isEmpty()) {
            data = new ArrayList<>();
            data.add(new String[]{"employeeId", "cardNumber", "expiry", "cvv"});
        }

        boolean updated = false;
        for (int i = 1; i < data.size(); i++) {
            String[] row = data.get(i);
            if (row.length >= 1 && employeeId.equals(row[0])) {
                row[1] = cardNumber;
                row[2] = expiry;
                row[3] = cvv;
                updated = true;
                break;
            }
        }
        if (!updated) {
            data.add(new String[]{employeeId, cardNumber, expiry, cvv});
        }

        CSVHandler.writeCSV("data/payout_accounts.csv", data);
    }

    private static void deletePayoutAccount(String employeeId) {
        java.util.List<String[]> data = CSVHandler.readCSV("data/payout_accounts.csv");
        if (data.size() <= 1) return;

        java.util.List<String[]> out = new ArrayList<>();
        out.add(data.get(0));
        for (int i = 1; i < data.size(); i++) {
            String[] row = data.get(i);
            if (row.length >= 1 && employeeId.equals(row[0])) continue;
            out.add(row);
        }
        CSVHandler.writeCSV("data/payout_accounts.csv", out);
    }
}
