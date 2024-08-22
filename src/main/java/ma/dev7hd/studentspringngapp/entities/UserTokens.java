package ma.dev7hd.studentspringngapp.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.*;

import java.time.Instant;

@Entity
@AllArgsConstructor @NoArgsConstructor
@Getter @Setter @Builder
public class UserTokens {
    @Id
    private String email;
    private String token;
    private Instant loginTime;
}
