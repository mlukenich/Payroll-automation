package com.czen.payroll_automation.domain;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "payroll_batches")
@Data
@NoArgsConstructor
public class PayrollBatchEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String trackingId;

    private String status;
    private LocalDateTime submitDate;

    @Column(length = 5000)
    private String errors; // JSON string of errors
}
