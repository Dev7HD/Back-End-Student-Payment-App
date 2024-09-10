package ma.dev7hd.studentspringngapp.security.services;

import lombok.AllArgsConstructor;
import ma.dev7hd.studentspringngapp.entities.BlacklistedToken;
import ma.dev7hd.studentspringngapp.entities.Token;
import ma.dev7hd.studentspringngapp.repositories.BlacklistedTokenRepository;
import ma.dev7hd.studentspringngapp.repositories.UserTokensRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
@AllArgsConstructor
public class JwtBlacklistService implements IJwtBlacklistService {
    private final BlacklistedTokenRepository blacklistedTokenRepository;
    private final UserTokensRepository userTokensRepository;


    @Override
    public void blacklistToken(String token) {
        if (!blacklistedTokenRepository.existsByTokenHash(Token.hashToken(token))) {
            BlacklistedToken blacklistedToken = new BlacklistedToken();
            blacklistedToken.setToken(token);
            blacklistedToken.setBlacklistedAt(Instant.now());
            blacklistedTokenRepository.save(blacklistedToken);
            userTokensRepository.deleteAllByTokenHash(Token.hashToken(token));
        }
    }

    @Override
    public boolean isTokenBlacklisted(String token) {
        return blacklistedTokenRepository.existsByTokenHash(Token.hashToken(token));
    }

    @Override
    public void removeTokenFromBlacklist(String token) {
        blacklistedTokenRepository.deleteById(token);
    }

    @Scheduled(fixedRate = 86400000)
    @Override
    public void emptyBlacklistTokens(){
        Instant day = Instant.now().minus(24, ChronoUnit.HOURS);
        blacklistedTokenRepository.deleteAllByBlacklistedAtLessThan(day);
        System.out.println("Old blacklisted tokens removed (Older then 24 hours)");
        userTokensRepository.deleteAllByLoginTimeLessThan(day);
        System.out.println("Expired login tokens removed (Older then 24 hours)");
    }
}
