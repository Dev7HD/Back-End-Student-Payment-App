package ma.dev7hd.studentspringngapp.security.services;

import org.springframework.security.core.Authentication;

import java.util.Map;

public interface IJwtProvider {
    Map<String, String> getJWT(String username, String password);

}
