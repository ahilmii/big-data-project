package org.ahilmi.schoolmanagement.service;

import lombok.RequiredArgsConstructor;
import org.ahilmi.schoolmanagement.dto.CourseRequest;
import org.ahilmi.schoolmanagement.dto.CourseResponse;
import org.ahilmi.schoolmanagement.model.Course;
import org.ahilmi.schoolmanagement.repository.CourseRepository;
import org.ahilmi.schoolmanagement.repository.TeachesRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CourseService {

    private final CourseRepository courseRepository;
    private final TeachesRepository teachesRepository;

    @Transactional(readOnly = true)
    public List<CourseResponse> getAllCourses() {
        return courseRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public CourseResponse getCourseById(Long id) {
        return toResponse(findCourse(id));
    }

    @Transactional
    public CourseResponse createCourse(CourseRequest request) {
        Course course = new Course();

        course.setName(request.name());
        course.setCredit(request.credit());

        return toResponse(courseRepository.save(course));
    }

    @Transactional
    public CourseResponse updateCourse(Long id, CourseRequest request) {
        Course course = findCourse(id);

        course.setName(request.name());
        course.setCredit(request.credit());

        return toResponse(courseRepository.save(course));
    }

    @Transactional
    public void deleteCourse(Long id) {
        Course course = findCourse(id);

        if (teachesRepository.existsByCourse_Id(id)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Course has teaching assignments"
            );
        }

        courseRepository.delete(course);
    }

    private Course findCourse(Long id) {
        return courseRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Course not found: " + id
                ));
    }

    private CourseResponse toResponse(Course course) {
        return new CourseResponse(
                course.getId(),
                course.getName(),
                course.getCredit()
        );
    }
}
