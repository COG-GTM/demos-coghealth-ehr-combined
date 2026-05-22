package com.medchart.ehr.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Response DTO for PDC calculation.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PdcCalculationResponse {

    private Long patientId;
    private Long medicationOrderId;
    private LocalDate periodStart;
    private LocalDate periodEnd;
    
    /**
     * PDC score as decimal (0.0 - 1.0).
     */
    private BigDecimal pdcScore;
    
    /**
     * PDC score as percentage (0 - 100).
     */
    private BigDecimal pdcPercentage;
    
    /**
     * Total days in the measurement period.
     */
    private Long totalDays;
    
    /**
     * Number of days covered by medication.
     */
    private Long daysCovered;
    
    /**
     * Adherence classification based on PDC.
     */
    private String adherenceStatus;
    
    /**
     * Whether the patient meets the 80% adherence threshold.
     */
    private boolean isAdherent;

    /**
     * Create response from calculation results.
     */
    public static PdcCalculationResponse fromCalculation(Long patientId, Long medicationOrderId,
                                                          LocalDate periodStart, LocalDate periodEnd,
                                                          BigDecimal pdcScore, Long totalDays, Long daysCovered) {
        BigDecimal pdcPercentage = pdcScore.multiply(new BigDecimal("100"));
        
        String adherenceStatus;
        boolean isAdherent;
        
        if (pdcScore.compareTo(new BigDecimal("0.80")) >= 0) {
            adherenceStatus = "ADHERENT";
            isAdherent = true;
        } else if (pdcScore.compareTo(new BigDecimal("0.50")) >= 0) {
            adherenceStatus = "PARTIALLY_ADHERENT";
            isAdherent = false;
        } else {
            adherenceStatus = "NON_ADHERENT";
            isAdherent = false;
        }
        
        return PdcCalculationResponse.builder()
                .patientId(patientId)
                .medicationOrderId(medicationOrderId)
                .periodStart(periodStart)
                .periodEnd(periodEnd)
                .pdcScore(pdcScore)
                .pdcPercentage(pdcPercentage)
                .totalDays(totalDays)
                .daysCovered(daysCovered)
                .adherenceStatus(adherenceStatus)
                .isAdherent(isAdherent)
                .build();
    }
}