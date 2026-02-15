package com.czen.payroll_automation.repository;

import com.czen.payroll_automation.domain.PayrollBatchEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface PayrollBatchRepository extends JpaRepository<PayrollBatchEntity, Long> {
    Optional<PayrollBatchEntity> findByTrackingId(String trackingId);
}
