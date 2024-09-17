package ma.dev7hd.studentspringngapp.security.services;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import ma.dev7hd.studentspringngapp.entities.BlacklistedToken;
import ma.dev7hd.studentspringngapp.entities.Token;
import ma.dev7hd.studentspringngapp.repositories.BlacklistedTokenRepository;
import ma.dev7hd.studentspringngapp.repositories.UserTokensRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

@Service
@Transactional
@AllArgsConstructor
public class JwtBlacklistService implements IJwtBlacklistService {
    private final BlacklistedTokenRepository blacklistedTokenRepository;
    private final UserTokensRepository userTokensRepository;


    @Override
    public void blacklistToken(String token) {
        if (!blacklistedTokenRepository.existsByTokenHash(Token.hashToken(token))) {
            BlacklistedToken blacklistedToken = new BlacklistedToken();
            blacklistedToken.setToken(token);
            blacklistedToken.setBlacklistedAt(new Date());
            blacklistedTokenRepository.save(blacklistedToken);
            userTokensRepository.deleteByTokenHash(Token.hashToken(token));
        }
    }

    @Override
    public boolean isTokenBlacklisted(String token) {
        return blacklistedTokenRepository.existsByTokenHash(Token.hashToken(token));
    }

    @Scheduled(fixedRate = 3600000) // Run every one hour
    @Override
    public void emptyBlacklistTokensAndUserTokens(){
        Date day = new Date(Instant.now().minus(24, ChronoUnit.HOURS).toEpochMilli());
        System.out.println("Removing Old blacklisted tokens (Older then 24 hours)....");
        blacklistedTokenRepository.deleteAllByBlacklistedAtLessThan(day);
        System.out.println("Removing Old user tokens (Older then 24 hours)....");
        userTokensRepository.deleteAllByLoginTimeLessThan(day);
    }

}
