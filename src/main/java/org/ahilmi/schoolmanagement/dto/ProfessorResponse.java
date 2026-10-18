package org.ahilmi.schoolmanagement.dto;

import java.io.Serializable;

public record ProfessorResponse(
        Long id,
        String name,
        String department,
        String photoPath
) implements Serializable {

    private static final long serialVersionUID = 1L;
}
// burada Serializable ve yukarıadki uid ne işe yaradı öğren