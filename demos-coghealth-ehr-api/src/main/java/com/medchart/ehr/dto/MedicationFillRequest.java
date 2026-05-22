package com.medchart.ehr.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.time.LocalDate;

/**
 * Request DTO for recording a medication fill from pharmacy.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MedicationFillRequest {

    @NotNull(message = "Patient ID is required")
    private Long patientId;

    @NotNull(message = "Medication order ID is required")
    private Long medicationOrderId;

    @NotNull(message = "Fill date is required")
    private LocalDate fillDate;

    @NotNull(message = "Days supply is required")
    @Positive(message = "Days supply must be positive")
    private Integer daysSupply;

    @NotBlank(message = "NDC is required")
    private String ndc;

    private String pharmacyNpi;
    private String pharmacyName;
    private String rxNumber;
    
    @NotBlank(message = "Fill source is required")
    private String fillSource;
}