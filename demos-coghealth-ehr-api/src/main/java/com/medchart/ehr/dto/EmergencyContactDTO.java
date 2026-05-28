package com.medchart.ehr.dto;

import lombok.*;

import javax.validation.Valid;
import javax.validation.constraints.Email;
import javax.validation.constraints.Size;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmergencyContactDTO {
    private Long id;

    @Size(max = 100, message = "First name must not exceed 100 characters")
    private String firstName;

    @Size(max = 100, message = "Last name must not exceed 100 characters")
    private String lastName;

    @Size(max = 50, message = "Relationship must not exceed 50 characters")
    private String relationship;

    @Size(max = 20, message = "Phone number must not exceed 20 characters")
    private String phoneHome;

    @Size(max = 20, message = "Phone number must not exceed 20 characters")
    private String phoneMobile;

    @Size(max = 20, message = "Phone number must not exceed 20 characters")
    private String phoneWork;

    @Email(message = "Email must be a valid email address")
    private String email;

    @Valid
    private AddressDTO address;

    private Integer priority;
    private Boolean active;
}
