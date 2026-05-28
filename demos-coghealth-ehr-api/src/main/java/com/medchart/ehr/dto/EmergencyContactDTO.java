package com.medchart.ehr.dto;

import lombok.*;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmergencyContactDTO {
    private Long id;

    @NotBlank(message = "Emergency contact first name is required")
    @Size(max = 100, message = "First name must not exceed 100 characters")
    private String firstName;

    @NotBlank(message = "Emergency contact last name is required")
    @Size(max = 100, message = "Last name must not exceed 100 characters")
    private String lastName;

    @NotBlank(message = "Relationship is required")
    @Size(max = 50, message = "Relationship must not exceed 50 characters")
    private String relationship;

    @Size(max = 20, message = "Phone number must not exceed 20 characters")
    private String phoneHome;

    @Size(max = 20, message = "Phone number must not exceed 20 characters")
    private String phoneMobile;

    @Size(max = 20, message = "Phone number must not exceed 20 characters")
    private String phoneWork;

    private String email;

    @Valid
    private AddressDTO address;

    private Integer priority;
    private Boolean active;
}
