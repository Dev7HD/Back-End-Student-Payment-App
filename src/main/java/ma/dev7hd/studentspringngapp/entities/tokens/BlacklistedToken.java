package ma.dev7hd.studentspringngapp.entities.tokens;

import jakarta.persistence.*;
import lombok.*;

import java.util.Date;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter @Setter
public class BlacklistedToken extends Token {
    @Column(nullable = false, updatable = false)
    private Date blacklistedAt;
}
