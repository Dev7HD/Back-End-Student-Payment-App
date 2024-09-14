package ma.dev7hd.studentspringngapp.entities;

import jakarta.persistence.*;
import lombok.*;
import ma.dev7hd.studentspringngapp.enumirat.ProgramID;

import java.util.Date;

@Entity
@AllArgsConstructor @NoArgsConstructor
@Getter @Setter
@Builder
public class PendingStudent {
    @Id
    private String email;
    @Column(nullable = false, updatable = false)
    private String firstName;

    @Column(nullable = false, updatable = false)
    private String lastName;

    @Column(nullable = false, updatable = false)
    private Date registerDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, updatable = false)
    private ProgramID programID;

    @Column(nullable = false, updatable = false, unique = true)
    private String code;

    @Column(nullable = false)
    private boolean seen;
}
