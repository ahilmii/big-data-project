package org.ahilmi.schoolmanagement.model;

import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDate;

@Embeddable // @Embeddable, bu sınıfın başka bir entity içerisinde birleşik anahtar olarak kullanılabileceğini belirtir.
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class TeachesId implements Serializable {

    private Long professorId;

    private Long courseId;

    private LocalDate startDate;
}
