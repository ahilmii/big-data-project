package org.ahilmi.schoolmanagement.repository;

import org.ahilmi.schoolmanagement.model.Professor;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProfessorRepository extends JpaRepository<Professor, Long> {
}
