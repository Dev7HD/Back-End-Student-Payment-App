package ma.dev7hd.studentspringngapp.security.services;

import ma.dev7hd.studentspringngapp.entities.tokens.UserTokens;

import java.util.List;
import java.util.Map;

public interface IJwtProvider {
    Map<String, String> getJWT(String username, String password);

    List<UserTokens> getUserTokens();
}
