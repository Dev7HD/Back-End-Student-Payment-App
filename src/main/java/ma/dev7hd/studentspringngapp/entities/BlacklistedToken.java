package ma.dev7hd.studentspringngapp.entities;

import jakarta.persistence.*;
import lombok.*;

import java.util.Date;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter @Setter
public class BlacklistedToken extends Token {
    @Column(unique = true, nullable = false, updatable = false)
    private Date blacklistedAt;
}
