package ma.dev7hd.studentspringngapp.security.services;

import org.springframework.http.ResponseEntity;

import java.util.Map;

public interface ISecurityService {
    ResponseEntity<?> login(String username, String password);

    void logout(String token);
}
