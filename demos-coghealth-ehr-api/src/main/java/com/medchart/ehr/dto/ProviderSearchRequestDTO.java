package com.medchart.ehr.dto;

import lombok.*;

import javax.validation.constraints.NotBlank;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProviderSearchRequestDTO {

    @NotBlank(message = "Last name is required")
    private String lastName;
}
