package org.ahilmi.schoolmanagement.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ProfessorRequest(

        @NotBlank(message = "Professor name cannot be empty")
        @Size(max = 150)
        String name,

        @NotBlank(message = "Department cannot be empty")
        @Size(max = 150)
        String department
) {
}
