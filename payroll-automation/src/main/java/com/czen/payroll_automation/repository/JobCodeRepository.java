package com.czen.payroll_automation.repository;

import com.czen.payroll_automation.model.JobCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JobCodeRepository extends JpaRepository<JobCode, Long> {
}