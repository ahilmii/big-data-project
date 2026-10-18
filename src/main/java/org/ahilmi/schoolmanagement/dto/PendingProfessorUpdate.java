package org.ahilmi.schoolmanagement.dto;

public record PendingProfessorUpdate(
        Long professorId,
        String name,
        String department
) {
}
