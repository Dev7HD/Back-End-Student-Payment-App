package ma.dev7hd.studentspringngapp.security.services;

public interface IJwtBlacklistService {
    void blacklistToken(String token);

    boolean isTokenBlacklisted(String token);

    void removeTokenFromBlacklist(String token);
}
