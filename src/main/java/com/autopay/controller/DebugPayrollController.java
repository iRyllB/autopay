package com.autopay.controller;

import com.autopay.service.PayrollService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/debug")
@PreAuthorize("hasRole('ADMIN')")
public class DebugPayrollController {
    @Autowired
    private PayrollService payrollService;

    @PostMapping("/run-payroll")
    public ResponseEntity<?> runPayroll() {
        payrollService.runPayrollForAllEmployees();
        return ResponseEntity.ok(Map.of("message", "Payroll run successfully for all employees."));
    }
}
