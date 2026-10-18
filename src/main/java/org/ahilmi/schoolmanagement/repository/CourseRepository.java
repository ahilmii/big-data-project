
package org.ahilmi.schoolmanagement.repository;

import org.ahilmi.schoolmanagement.model.Course;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CourseRepository
        extends JpaRepository<Course, Long> {
}
