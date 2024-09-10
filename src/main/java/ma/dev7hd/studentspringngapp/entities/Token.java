package ma.dev7hd.studentspringngapp.entities;

import jakarta.persistence.*;
import lombok.*;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public class Token {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Lob
    @Column(nullable = false, updatable = false, columnDefinition = "TEXT")
    private String token;

    @Column(unique = true, nullable = false, updatable = false, columnDefinition = "CHAR(64)")
    private String tokenHash;

    @PrePersist
    public void generateTokenHash() {
        this.tokenHash = hashToken(this.token);
    }

    static public String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder(2 * hash.length);
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}
