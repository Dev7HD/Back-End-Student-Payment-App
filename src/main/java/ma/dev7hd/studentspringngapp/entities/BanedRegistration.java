package ma.dev7hd.studentspringngapp.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.*;
import ma.dev7hd.studentspringngapp.enumirat.ProgramID;

import java.time.Instant;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class BanedRegistration {
    @Id
    String email;
    String firstName;
    String lastName;
    Instant registerDate;
    Instant banDate;
    ProgramID programID;
    String code;
    @ManyToOne
    @JoinColumn(name = "admin_id")
    Admin adminBanner;
}
