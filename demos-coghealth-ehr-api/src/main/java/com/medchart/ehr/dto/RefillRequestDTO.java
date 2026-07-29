package com.medchart.ehr.dto;

import com.medchart.ehr.domain.refill.RefillRequestStatus;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefillRequestDTO {

    private Long id;
    private RefillRequestStatus status;
    private String pharmacyName;
    private LocalDate requestedDate;
    private String notes;
    private PatientSummary patient;
    private MedicationSummary medication;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PatientSummary {
        private Long id;
        private String mrn;
        private String fullName;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class MedicationSummary {
        private Long id;
        private String genericName;
        private String brandName;
    }
}
