package ma.dev7hd.studentspringngapp.repositories.tokens;

import jakarta.transaction.Transactional;
import ma.dev7hd.studentspringngapp.entities.tokens.UserTokens;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Transactional
public interface UserTokensRepository extends JpaRepository<UserTokens, UUID> {
    Optional<List<UserTokens>> findByEmail(String email);
    void deleteByTokenHash(String token);
    void deleteAllByLoginTimeLessThan(Date day);
    Optional<UserTokens> findByTokenHash(String tokenHash);
}
