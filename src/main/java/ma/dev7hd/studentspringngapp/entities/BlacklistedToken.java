package ma.dev7hd.studentspringngapp.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter @Setter
@Builder
public class BlacklistedToken {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;
    @Lob
    @Column(unique = true, nullable = false, updatable = false, columnDefinition = "TEXT")
    private String token;
    @Column(unique = true, nullable = false, updatable = false)
    private Instant blacklistedAt;
}
