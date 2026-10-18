package org.ahilmi.schoolmanagement.service;

import lombok.RequiredArgsConstructor;
import org.ahilmi.schoolmanagement.dto.PendingProfessorUpdate;
import org.ahilmi.schoolmanagement.model.Professor;
import org.ahilmi.schoolmanagement.repository.ProfessorRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProfessorSyncService {

    private final ProfessorRepository professorRepository;

    @Transactional
    public void persistUpdate(PendingProfessorUpdate pending) {

        Professor professor = professorRepository.findById(pending.professorId())
                .orElseThrow(() -> new IllegalStateException(
                        "Professor no longer exists: "
                                + pending.professorId()
                ));

        professor.setName(pending.name());
        professor.setDepartment(pending.department());

        professorRepository.saveAndFlush(professor);
    }
}
