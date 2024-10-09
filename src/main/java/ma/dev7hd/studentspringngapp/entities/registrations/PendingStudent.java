package ma.dev7hd.studentspringngapp.entities.registrations;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import ma.dev7hd.studentspringngapp.enumirat.ProgramID;

import java.util.Date;

@Entity
@AllArgsConstructor @NoArgsConstructor
@Getter @Setter
@Builder
public class PendingStudent {
    @Id
    @Email(message = "Invalid email format")
    @NotBlank(message = "Email is mandatory")
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

    private String photo;

}
