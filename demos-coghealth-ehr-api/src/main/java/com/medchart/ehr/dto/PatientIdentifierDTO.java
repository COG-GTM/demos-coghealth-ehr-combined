package com.medchart.ehr.dto;

import com.medchart.ehr.domain.patient.IdentifierType;
import lombok.*;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PatientIdentifierDTO {
    private Long id;

    @NotNull(message = "Identifier type is required")
    private IdentifierType identifierType;

    @NotBlank(message = "Identifier value is required")
    @Size(max = 50, message = "Identifier value must not exceed 50 characters")
    private String identifierValue;

    @Size(max = 100, message = "Issuing authority must not exceed 100 characters")
    private String issuingAuthority;

    private LocalDate effectiveDate;
    private LocalDate expirationDate;
    private Boolean active;
}
