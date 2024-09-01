package ma.dev7hd.studentspringngapp.security.controller;

import lombok.AllArgsConstructor;
import ma.dev7hd.studentspringngapp.security.services.ISecurityService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@AllArgsConstructor
@RequestMapping("/auth")
public class securityController {

    private final ISecurityService securityService;

    @GetMapping("/profile")
    public Authentication authenticate(Authentication authentication) {
        return authentication;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(String username, String password) {
        return securityService.login(username, password);
    }

    @PostMapping("/logout")
    public void logout() {
        securityService.logout();
    }
}
