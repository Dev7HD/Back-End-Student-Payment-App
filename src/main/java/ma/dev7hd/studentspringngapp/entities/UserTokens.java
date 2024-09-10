package ma.dev7hd.studentspringngapp.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@AllArgsConstructor @NoArgsConstructor
@Getter @Setter
public class UserTokens extends Token {

    @Column(nullable = false, updatable = false)
    private String email;

    @Column(nullable = false, updatable = false)
    private Instant loginTime;

}
