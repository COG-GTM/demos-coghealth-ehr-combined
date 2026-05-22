package com.medchart.ehr.domain.chronic;

import com.medchart.ehr.domain.medication.MedicationOrder;
import com.medchart.ehr.domain.patient.Patient;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Tracks individual pharmacy fills for medications.
 * 
 * Used by the MedicationAdherenceTracker to calculate PDC (Proportion of Days Covered).
 * Each fill represents a dispensing event from a pharmacy with days supply information.
 */
@Entity
@Table(name = "medication_fills", indexes = {
    @Index(name = "idx_med_fill_patient", columnList = "patient_id"),
    @Index(name = "idx_med_fill_medication", columnList = "medication_order_id"),
    @Index(name = "idx_med_fill_date", columnList = "fill_date"),
    @Index(name = "idx_med_fill_ndc", columnList = "ndc")
})
@Data
@NoArgsConstructor
public class MedicationFill {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "patient_id", nullable = false)
    private Long patientId;

    @Column(name = "medication_order_id", nullable = false)
    private Long medicationOrderId;

    @Column(name = "fill_date", nullable = false)
    private LocalDate fillDate;

    /**
     * Number of days this fill covers (e.g., 30-day supply).
     * Used in PDC calculation to determine coverage period.
     */
    @Column(name = "days_supply", nullable = false)
    private Integer daysSupply;

    /**
     * National Drug Code - standard identifier for medications.
     * Used to match fills to medication orders.
     */
    @Column(name = "ndc", length = 11)
    private String ndc;

    /**
     * Pharmacy NPI (National Provider Identifier).
     */
    @Column(name = "pharmacy_npi", length = 10)
    private String pharmacyNpi;

    /**
     * Pharmacy name.
     */
    @Column(name = "pharmacy_name", length = 200)
    private String pharmacyName;

    /**
     * Prescription number from pharmacy.
     */
    @Column(name = "rx_number", length = 30)
    private String rxNumber;

    /**
     * Quantity dispensed.
     */
    @Column(name = "quantity_dispensed")
    private Integer quantityDispensed;

    /**
     * Whether this fill was on time (within grace period).
     */
    @Column(name = "on_time")
    private Boolean onTime;

    /**
     * Days late if fill was delayed.
     */
    @Column(name = "days_late")
    private Integer daysLate;

    /**
     * Source of this fill record (e.g., NCPDP, manual entry, integration).
     */
    @Column(name = "fill_source", length = 50)
    private String fillSource;

    /**
     * External reference ID from pharmacy system.
     */
    @Column(name = "external_reference_id", length = 100)
    private String externalReferenceId;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /**
     * Calculate the end date of coverage for this fill.
     * Coverage ends on fill_date + days_supply (exclusive).
     */
    public LocalDate getCoverageEndDate() {
        return fillDate.plusDays(daysSupply);
    }

    /**
     * Check if this fill covers a specific date.
     */
    public boolean coversDate(LocalDate date) {
        return !date.isBefore(fillDate) && date.isBefore(getCoverageEndDate());
    }
}