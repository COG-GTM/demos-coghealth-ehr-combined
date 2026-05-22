package com.medchart.ehr.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO for patients at risk of medication non-adherence.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AtRiskPatientDTO {

    private Long patientId;
    private String patientMrn;
    private String patientName;
    private String medicationName;
    private BigDecimal currentPdc;
    private LocalDate lastFillDate;
    private LocalDate nextFillDue;
    private String riskReason;

    /**
     * Convert from service inner class to DTO.
     */
    public static AtRiskPatientDTO fromServiceObject(
            com.medchart.ehr.service.chronic.MedicationAdherenceTracker.AtRiskPatient atRiskPatient) {
        if (atRiskPatient == null) {
            return null;
        }

        return AtRiskPatientDTO.builder()
                .patientId(atRiskPatient.getPatientId())
                .patientMrn(atRiskPatient.getPatientMrn())
                .patientName(atRiskPatient.getPatientName())
                .medicationName(atRiskPatient.getMedicationName())
                .currentPdc(atRiskPatient.getCurrentPdc())
                .lastFillDate(atRiskPatient.getLastFillDate())
                .nextFillDue(atRiskPatient.getNextFillDue())
                .riskReason(atRiskPatient.getRiskReason())
                .build();
    }
}