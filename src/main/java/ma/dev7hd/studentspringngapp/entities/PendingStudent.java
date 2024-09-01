package ma.dev7hd.studentspringngapp.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
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
    String firstName;
    String lastName;
    Instant registerDate;
    ProgramID programID;
    String code;
}
