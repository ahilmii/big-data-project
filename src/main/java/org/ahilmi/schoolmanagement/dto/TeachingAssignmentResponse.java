package org.ahilmi.schoolmanagement.dto;

import java.time.LocalDate;

public record TeachingAssignmentResponse(
        Long professorId,
        String professorName,
        String department,
        String photoPath,
        Long courseId,
        String courseName,
        Integer credit,
        Integer studentCount,
        LocalDate startDate,
        LocalDate endingDate
) {
}
