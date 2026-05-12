package gui;

import javax.swing.*;

public class Dashboard extends JFrame {

    public Dashboard() {

        setTitle("Payroll System");
        setSize(800, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JLabel title = new JLabel("Automated Payroll System");
        title.setBounds(250, 30, 300, 30);

        JButton employeeBtn = new JButton("Employees");
        employeeBtn.setBounds(100, 100, 150, 40);

        JButton payrollBtn = new JButton("Payroll");
        payrollBtn.setBounds(300, 100, 150, 40);

        JButton bonusBtn = new JButton("Bonuses");
        bonusBtn.setBounds(500, 100, 150, 40);

        setLayout(null);

        add(title);
        add(employeeBtn);
        add(payrollBtn);
        add(bonusBtn);
    }
}