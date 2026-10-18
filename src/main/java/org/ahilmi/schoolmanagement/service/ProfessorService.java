package org.ahilmi.schoolmanagement.service;

import lombok.RequiredArgsConstructor;
import org.ahilmi.schoolmanagement.dto.ProfessorRequest;
import org.ahilmi.schoolmanagement.dto.ProfessorResponse;
import org.ahilmi.schoolmanagement.model.Professor;
import org.ahilmi.schoolmanagement.repository.ProfessorRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProfessorService {

    private final ProfessorRepository professorRepository;

    @Transactional(readOnly = true)
    public List<ProfessorResponse> getAllProfessors() {
        return professorRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Cacheable(value = "professors", key = "#id")
    @Transactional(readOnly = true)
    public ProfessorResponse getProfessorById(Long id) {
        return toResponse(findProfessor(id));
    }

    @Transactional
    public ProfessorResponse createProfessor(ProfessorRequest request) {
        Professor professor = new Professor();

        professor.setName(request.name());
        professor.setDepartment(request.department());

        Professor saved = professorRepository.save(professor);
        return toResponse(saved);
    }

    @CacheEvict(value = "professors", key = "#id")
    @Transactional
    public ProfessorResponse updateProfessor(Long id, ProfessorRequest request) {
        Professor professor = findProfessor(id);

        professor.setName(request.name());
        professor.setDepartment(request.department());

        return toResponse(professorRepository.save(professor));
    }

    @CacheEvict(value = "professors", key = "#id")
    @Transactional
    public void deleteProfessor(Long id) {
        Professor professor = findProfessor(id);

        try {
            professorRepository.delete(professor);
            professorRepository.flush();
        } catch (DataIntegrityViolationException exception) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Professor has teaching assignments"
            );
        }
    }

    private Professor findProfessor(Long id) {
        return professorRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Professor not found: " + id
                ));
    }

    private ProfessorResponse toResponse(Professor professor) {
        return new ProfessorResponse(
                professor.getId(),
                professor.getName(),
                professor.getDepartment(),
                professor.getPhotoPath()
        );
    }
}
