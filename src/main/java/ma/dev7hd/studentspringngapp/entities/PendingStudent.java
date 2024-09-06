package ma.dev7hd.studentspringngapp.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Column;
import lombok.*;
import ma.dev7hd.studentspringngapp.enumirat.ProgramID;

import java.time.Instant;

@Entity
@AllArgsConstructor @NoArgsConstructor
@Getter @Setter
@Builder
public class PendingStudent {
    @Id
    String email;
    @Column(nullable = false, updatable = false)
    
    String firstName;
    @Column(nullable = false, updatable = false)
    String lastName;
    @Column(nullable = false, updatable = false)
    Instant registerDate;
    @Column(nullable = false, updatable = false)
    ProgramID programID;
    @Column(nullable = false, updatable = false, unique = true)
    String code;
}
