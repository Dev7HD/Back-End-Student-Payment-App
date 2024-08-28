package ma.dev7hd.studentspringngapp.security.services;

import lombok.AllArgsConstructor;
import ma.dev7hd.studentspringngapp.entities.BlacklistedToken;
import ma.dev7hd.studentspringngapp.repositories.BlacklistedTokenRepository;
import ma.dev7hd.studentspringngapp.repositories.UserTokensRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@AllArgsConstructor
public class JwtBlacklistService implements IJwtBlacklistService {
    private final BlacklistedTokenRepository blacklistedTokenRepository;
    private final UserTokensRepository userTokensRepository;


    @Override
    public void blacklistToken(String token) {
        if (!blacklistedTokenRepository.existsByToken(token)) {
            BlacklistedToken blacklistedToken = BlacklistedToken.builder()
                    .token(token)
                    .blacklistedAt(Instant.now())
                    .build();
            blacklistedTokenRepository.save(blacklistedToken);
            userTokensRepository.deleteAllByToken(token);
        }
    }

    @Override
    public boolean isTokenBlacklisted(String token) {
        return blacklistedTokenRepository.existsByToken(token);
    }

    @Override
    public void removeTokenFromBlacklist(String token) {
        blacklistedTokenRepository.deleteById(token);
    }

}
