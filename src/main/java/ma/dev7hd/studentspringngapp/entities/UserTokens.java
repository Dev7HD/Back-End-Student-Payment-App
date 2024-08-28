package ma.dev7hd.studentspringngapp.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@AllArgsConstructor @NoArgsConstructor
@Getter @Setter @Builder
public class UserTokens {
    @Id @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;
    private String email;
    private String token;
    private Instant loginTime;
}
