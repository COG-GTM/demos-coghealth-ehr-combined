package com.medchart.ehr.dto;

import lombok.*;
import org.springframework.data.domain.Sort;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.Pattern;
import java.util.Arrays;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PatientSearchRequestDTO {

    private static final List<String> SORTABLE_FIELDS =
            Arrays.asList("lastName", "firstName", "mrn", "dateOfBirth", "id");

    private static final Sort DEFAULT_SORT =
            Sort.by("lastName").ascending().and(Sort.by("firstName").ascending()).and(Sort.by("id").ascending());

    private String query;

    @Min(value = 0, message = "Page must not be negative")
    private Integer page;

    @Min(value = 1, message = "Size must be at least 1")
    @Max(value = 200, message = "Size must not exceed 200")
    private Integer size;

    private String sortBy;

    @Pattern(regexp = "(?i)asc|desc", message = "Sort direction must be asc or desc")
    private String sortDirection;

    public int getPageOrDefault() {
        return page == null ? 0 : page;
    }

    public int getSizeOrDefault() {
        return size == null ? 20 : size;
    }

    public String getQueryOrEmpty() {
        return query == null ? "" : query;
    }

    /**
     * Resolves the requested sort, falling back to a deterministic order so paging stays stable.
     */
    public Sort getSortOrDefault() {
        if (sortBy == null || !SORTABLE_FIELDS.contains(sortBy)) {
            return DEFAULT_SORT;
        }
        Sort.Direction direction = "desc".equalsIgnoreCase(sortDirection)
                ? Sort.Direction.DESC
                : Sort.Direction.ASC;
        return Sort.by(direction, sortBy).and(Sort.by(Sort.Direction.ASC, "id"));
    }
}
