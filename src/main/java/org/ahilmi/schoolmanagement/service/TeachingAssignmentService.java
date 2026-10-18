package org.ahilmi.schoolmanagement.service;

import lombok.RequiredArgsConstructor;
import org.ahilmi.schoolmanagement.dto.*;
import org.ahilmi.schoolmanagement.model.*;
import org.ahilmi.schoolmanagement.repository.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TeachingAssignmentService {

    private final TeachesRepository teachesRepository;
    private final ProfessorRepository professorRepository;
    private final CourseRepository courseRepository;

    @Transactional(readOnly = true)
    public List<TeachingAssignmentResponse> getAllAssignments() {
        return teachesRepository.findAllAssignments();
    }

    @Transactional
    public TeachingAssignmentResponse createAssignment(TeachingAssignmentRequest request) {
        validateDates(request.startDate(), request.endingDate());

        Professor professor = professorRepository.findById(request.professorId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Professor not found"
                ));

        Course course = courseRepository.findById(request.courseId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Course not found"
                ));

        TeachesId id = new TeachesId(request.professorId(), request.courseId(), request.startDate());

        if (teachesRepository.existsById(id)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Teaching assignment already exists"
            );
        }

        Teaches teaches = new Teaches();

        teaches.setId(id);
        teaches.setProfessor(professor);
        teaches.setCourse(course);
        teaches.setStudentCount(request.studentCount());
        teaches.setEndingDate(request.endingDate());

        teachesRepository.saveAndFlush(teaches);

        return toResponse(teaches);
    }

    @Transactional
    public TeachingAssignmentResponse updateAssignment(Long professorId, Long courseId, LocalDate startDate, TeachingAssignmentUpdateRequest request) {
        validateDates(startDate, request.endingDate());

        Teaches teaches = findAssignment(professorId, courseId, startDate);

        teaches.setStudentCount(request.studentCount());
        teaches.setEndingDate(request.endingDate());

        teachesRepository.save(teaches);

        return toResponse(teaches);
    }

    @Transactional
    public void deleteAssignment(Long professorId, Long courseId, LocalDate startDate) {
        Teaches teaches = findAssignment(professorId, courseId, startDate);
        teachesRepository.delete(teaches);
    }

    private Teaches findAssignment(Long professorId, Long courseId, LocalDate startDate) {
        TeachesId id = new TeachesId(professorId, courseId, startDate);

        return teachesRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Teaching assignment not found"
                ));
    }

    private void validateDates(LocalDate startDate, LocalDate endingDate) {
        if (endingDate != null && endingDate.isBefore(startDate)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Ending date cannot be before start date"
            );
        }
    }

    private TeachingAssignmentResponse toResponse(Teaches teaches) {
        Professor professor = teaches.getProfessor();
        Course course = teaches.getCourse();

        return new TeachingAssignmentResponse(
                professor.getId(),
                professor.getName(),
                professor.getDepartment(),
                professor.getPhotoPath(),
                course.getId(),
                course.getName(),
                course.getCredit(),
                teaches.getStudentCount(),
                teaches.getId().getStartDate(),
                teaches.getEndingDate()
        );
    }
}
