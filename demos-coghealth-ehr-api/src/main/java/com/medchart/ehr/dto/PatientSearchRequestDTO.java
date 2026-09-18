package com.medchart.ehr.dto;

import lombok.*;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PatientSearchRequestDTO {

    private String query;

    @Min(value = 0, message = "Page must not be negative")
    private Integer page;

    @Min(value = 1, message = "Size must be at least 1")
    @Max(value = 200, message = "Size must not exceed 200")
    private Integer size;

    public int getPageOrDefault() {
        return page == null ? 0 : page;
    }

    public int getSizeOrDefault() {
        return size == null ? 20 : size;
    }

    public String getQueryOrEmpty() {
        return query == null ? "" : query;
    }
}
