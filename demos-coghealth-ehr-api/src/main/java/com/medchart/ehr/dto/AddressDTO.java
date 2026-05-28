package com.medchart.ehr.dto;

import lombok.*;

import javax.validation.constraints.Size;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AddressDTO {
    @Size(max = 200, message = "Street address must not exceed 200 characters")
    private String street1;

    @Size(max = 200, message = "Street address must not exceed 200 characters")
    private String street2;

    @Size(max = 100, message = "City must not exceed 100 characters")
    private String city;

    @Size(max = 50, message = "State must not exceed 50 characters")
    private String state;

    @Size(max = 20, message = "Zip code must not exceed 20 characters")
    private String zipCode;

    @Size(max = 50, message = "Country must not exceed 50 characters")
    private String country;
}
