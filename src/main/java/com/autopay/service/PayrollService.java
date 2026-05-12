package com.autopay.service;

import com.autopay.model.Employee;
import com.autopay.model.PayrollRecord;
import com.autopay.repository.EmployeeRepository;
import com.autopay.repository.PayrollRecordRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class PayrollService {
    @Autowired
    private PayrollRecordRepository payrollRecordRepository;
    @Autowired
    private EmployeeRepository employeeRepository;

    public PayrollRecord calculatePayroll(Employee e) {
        double gross = e.getBaseSalary();
        double tax = gross * 0.10;
        double net = gross - tax;

        PayrollRecord record = new PayrollRecord();
        record.setEmployee(e);
        record.setPayPeriod(LocalDate.now().format(DateTimeFormatter.ofPattern("MMM/yyyy")));
        record.setGrossPay(gross);
        record.setTaxDeduction(tax);
        record.setNetPay(net);
        record.setStatus("PAID");
        return record;
    }

    @Transactional
    public void runPayrollForAllEmployees() {
        List<Employee> employees = employeeRepository.findAll();
        for (Employee e : employees) {
            PayrollRecord record = calculatePayroll(e);
            payrollRecordRepository.save(record);
            e.setNextPayrollDate(LocalDate.now().plusDays(30));
            employeeRepository.save(e);
        }
    }
}
