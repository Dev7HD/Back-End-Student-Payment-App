package ma.dev7hd.studentspringngapp.repositories;

import ma.dev7hd.studentspringngapp.entities.BlacklistedToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Date;

import org.springframework.transaction.annotation.Transactional;

@Transactional
public interface BlacklistedTokenRepository extends JpaRepository<BlacklistedToken, String> {
    boolean existsByTokenHash(String tokenHash);
    void deleteAllByBlacklistedAtLessThan(Date day);
}
