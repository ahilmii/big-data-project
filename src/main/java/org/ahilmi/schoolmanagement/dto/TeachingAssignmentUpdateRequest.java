
package org.ahilmi.schoolmanagement.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record TeachingAssignmentUpdateRequest(
        @NotNull
        @Min(0)
        Integer studentCount,
        LocalDate endingDate
) {
}
