
package org.ahilmi.schoolmanagement.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record TeachingAssignmentRequest(

        @NotNull
        Long professorId,

        @NotNull
        Long courseId,

        @NotNull
        LocalDate startDate,

        @NotNull
        @Min(0)
        Integer studentCount,

        LocalDate endingDate
) {
}
