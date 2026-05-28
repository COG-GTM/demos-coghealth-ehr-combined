package com.medchart.ehr.dto;

import lombok.*;

import javax.validation.constraints.Size;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AddressDTO {
    @Size(max = 200, message = "Street line 1 must not exceed 200 characters")
    private String street1;

    @Size(max = 200, message = "Street line 2 must not exceed 200 characters")
    private String street2;

    @Size(max = 100, message = "City must not exceed 100 characters")
    private String city;

    @Size(max = 2, message = "State must be a 2-letter code")
    private String state;

    @Size(max = 10, message = "Zip code must not exceed 10 characters")
    private String zipCode;

    @Size(max = 100, message = "Country must not exceed 100 characters")
    private String country;
}
