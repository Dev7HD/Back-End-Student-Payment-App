package ma.dev7hd.studentspringngapp.repositories;

import ma.dev7hd.studentspringngapp.entities.BlacklistedToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public interface BlacklistedTokenRepository extends JpaRepository<BlacklistedToken, String> {
    boolean existsByToken(String token);
    void deleteAllByBlacklistedAtLessThan(Instant day);
}
