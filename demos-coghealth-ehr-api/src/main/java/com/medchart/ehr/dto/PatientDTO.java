package com.medchart.ehr.dto;

import com.medchart.ehr.domain.patient.Gender;
import com.medchart.ehr.domain.patient.MaritalStatus;
import lombok.*;

import javax.validation.Valid;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Past;
import javax.validation.constraints.Size;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PatientDTO {

    private Long id;
    private String mrn;

    @NotBlank(message = "First name is required")
    @Size(max = 100, message = "First name must not exceed 100 characters")
    private String firstName;

    @Size(max = 100, message = "Middle name must not exceed 100 characters")
    private String middleName;

    @NotBlank(message = "Last name is required")
    @Size(max = 100, message = "Last name must not exceed 100 characters")
    private String lastName;

    @Past(message = "Date of birth must be in the past")
    private LocalDate dateOfBirth;

    private Gender gender;
    private MaritalStatus maritalStatus;

    @Email(message = "Email must be a valid email address")
    private String email;

    @Size(max = 20, message = "Phone number must not exceed 20 characters")
    private String phoneHome;

    @Size(max = 20, message = "Phone number must not exceed 20 characters")
    private String phoneMobile;

    @Size(max = 20, message = "Phone number must not exceed 20 characters")
    private String phoneWork;

    @Valid
    private AddressDTO address;

    @Valid
    private AddressDTO mailingAddress;

    private String preferredLanguage;
    private String ethnicity;
    private String race;
    private String religion;

    private List<@Valid PatientIdentifierDTO> identifiers;
    private List<@Valid EmergencyContactDTO> emergencyContacts;

    private Boolean active;
    private Boolean deceased;
    private LocalDateTime deceasedDate;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public String getFullName() {
        if (middleName != null && !middleName.isBlank()) {
            return firstName + " " + middleName + " " + lastName;
        }
        return firstName + " " + lastName;
    }

    public Integer getAge() {
        if (dateOfBirth == null) return null;
        return java.time.Period.between(dateOfBirth, LocalDate.now()).getYears();
    }
}
