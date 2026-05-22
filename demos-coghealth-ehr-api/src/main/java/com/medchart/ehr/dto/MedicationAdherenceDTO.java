package com.medchart.ehr.dto;

import com.medchart.ehr.domain.chronic.MedicationAdherence;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DTO for medication adherence information.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MedicationAdherenceDTO {

    private Long id;
    private Long patientId;
    private Long medicationOrderId;
    private Long chronicConditionId;
    private LocalDate periodStart;
    private LocalDate periodEnd;
    
    /**
     * Proportion of Days Covered (PDC) - industry standard measure.
     * PDC >= 0.80 (80%) is considered adherent.
     */
    private BigDecimal pdcScore;
    
    private Integer daysSupply;
    private Integer daysCovered;
    private Integer refillsOnTime;
    private Integer refillsLate;
    private Integer refillsMissed;
    
    private AdherenceStatusDTO adherenceStatus;
    private LocalDate lastFillDate;
    private LocalDate nextFillDue;
    private String pharmacyNpi;
    private String pharmacyName;
    
    private boolean alertSent;
    private LocalDateTime alertSentDate;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * Convert from domain entity to DTO.
     */
    public static MedicationAdherenceDTO fromEntity(MedicationAdherence adherence) {
        if (adherence == null) {
            return null;
        }

        return MedicationAdherenceDTO.builder()
                .id(adherence.getId())
                .patientId(adherence.getPatientId())
                .medicationOrderId(adherence.getMedicationOrderId())
                .chronicConditionId(adherence.getChronicConditionId())
                .periodStart(adherence.getPeriodStart())
                .periodEnd(adherence.getPeriodEnd())
                .pdcScore(adherence.getPdcScore())
                .daysSupply(adherence.getDaysSupply())
                .daysCovered(adherence.getDaysCovered())
                .refillsOnTime(adherence.getRefillsOnTime())
                .refillsLate(adherence.getRefillsLate())
                .refillsMissed(adherence.getRefillsMissed())
                .adherenceStatus(mapAdherenceStatus(adherence.getAdherenceStatus()))
                .lastFillDate(adherence.getLastFillDate())
                .nextFillDue(adherence.getNextFillDue())
                .pharmacyNpi(adherence.getPharmacyNpi())
                .pharmacyName(adherence.getPharmacyName())
                .alertSent(adherence.isAlertSent())
                .alertSentDate(adherence.getAlertSentDate())
                .createdAt(adherence.getCreatedAt())
                .updatedAt(adherence.getUpdatedAt())
                .build();
    }

    private static AdherenceStatusDTO mapAdherenceStatus(MedicationAdherence.AdherenceStatus status) {
        if (status == null) {
            return AdherenceStatusDTO.UNKNOWN;
        }
        return AdherenceStatusDTO.valueOf(status.name());
    }

    /**
     * Simplified enum for DTO responses.
     */
    public enum AdherenceStatusDTO {
        ADHERENT,           // PDC >= 80%
        PARTIALLY_ADHERENT, // PDC 50-79%
        NON_ADHERENT,       // PDC < 50%
        UNKNOWN
    }
}