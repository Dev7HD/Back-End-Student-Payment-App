package ma.dev7hd.studentspringngapp.security.services;

import org.springframework.scheduling.annotation.Scheduled;

public interface IJwtBlacklistService {
    void blacklistToken(String token);

    boolean isTokenBlacklisted(String token);

    @Scheduled(fixedRate = 86400000)
    void emptyBlacklistTokensAndUserTokens();

}
