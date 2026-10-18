
package org.ahilmi.schoolmanagement.repository;

import org.ahilmi.schoolmanagement.dto.TeachingAssignmentResponse;
import org.ahilmi.schoolmanagement.model.Teaches;
import org.ahilmi.schoolmanagement.model.TeachesId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface TeachesRepository
        extends JpaRepository<Teaches, TeachesId> {

    @Query("""
        SELECT new org.ahilmi.schoolmanagement.dto.TeachingAssignmentResponse(
            p.id,
            p.name,
            p.department,
            p.photoPath,
            c.id,
            c.name,
            c.credit,
            t.studentCount,
            t.id.startDate,
            t.endingDate
        )
        FROM Teaches t
        JOIN t.professor p
        JOIN t.course c
        ORDER BY p.id, c.name
        """)
    List<TeachingAssignmentResponse> findAllAssignments();

    boolean existsByCourse_Id(Long courseId);
}
