package ma.dev7hd.studentspringngapp.security.services;

import org.springframework.http.ResponseEntity;


public interface ISecurityService {
    ResponseEntity<?> login(String username, String password);

    void logoutByToken(String token);

    void logout();
}
