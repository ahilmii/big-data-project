
package org.ahilmi.schoolmanagement.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "teaches")
@Getter
@Setter
@NoArgsConstructor
public class Teaches {

    @EmbeddedId
    private TeachesId id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("professorId")
    @JoinColumn(name = "professor_id", nullable = false)
    private Professor professor;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("courseId")
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @Column(name = "student_count", nullable = false)
    private Integer studentCount;

    @Column(name = "ending_date")
    private LocalDate endingDate;
}
