package com.dyxia.nexuserp.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

/**
 * Entité représentant un cycle de pointage mensuel (du 16 au 15).
 * Permet de valider si le mois a été entièrement travaillé et de déclencher
 * l'accumulation automatique de 2.5 jours de congés.
 */
@Entity
@Table(name = "monthly_cycles")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MonthlyCycle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_profile_id", nullable = false)
    private EmployeeProfile employeeProfile;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(name = "validated_as_worked", nullable = false)
    private boolean validatedAsWorked;

    @Column(name = "processed_for_accrual", nullable = false)
    private boolean processedForAccrual;
}
