package com.autopay.repository;

import com.autopay.model.PayrollRecord;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PayrollRecordRepository extends JpaRepository<PayrollRecord, Long> {
}
