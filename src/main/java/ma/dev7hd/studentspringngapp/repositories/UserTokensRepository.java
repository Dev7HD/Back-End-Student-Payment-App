package ma.dev7hd.studentspringngapp.repositories;

import ma.dev7hd.studentspringngapp.entities.UserTokens;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserTokensRepository extends JpaRepository<UserTokens, UUID> {
    Optional<List<UserTokens>> findByEmail(String email);
    void deleteAllByToken(String token);
    void deleteAllByLoginTimeLessThan(Instant day);
    Optional<UserTokens> findByToken(String token);
}
