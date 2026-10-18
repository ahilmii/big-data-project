package org.ahilmi.schoolmanagement.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CourseRequest(

        @NotBlank(message = "Course name cannot be empty")
        @Size(max = 150)
        String name,

        @NotNull(message = "Credit is required")
        @Min(value = 1, message = "Credit must be at least 1")
        @Max(value = 30, message = "Credit must not exceed 30")
        Integer credit
) {
}
