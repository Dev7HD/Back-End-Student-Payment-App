package ma.dev7hd.studentspringngapp.security.services;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import ma.dev7hd.studentspringngapp.entities.UserTokens;
import ma.dev7hd.studentspringngapp.repositories.UserTokensRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Service
@AllArgsConstructor
@Transactional
public class SecurityService implements ISecurityService {

    private final IJwtBlacklistService jwtBlacklistService;
    private final UserTokensRepository tokensRepository;
    private final IJwtProvider jwtProvider;

    @Override
    public ResponseEntity<?> login(String username, String password) {
        try{
            Map<String, String> jwt = jwtProvider.getJWT(username, password);
            UserTokens userTokens = UserTokens.builder()
                    .token(jwt.get("access_token"))
                    .email(username)
                    .loginTime(Instant.now())
                    .build();
            tokensRepository.save(userTokens);
            return ResponseEntity.ok(jwt);
        } catch (BadCredentialsException e){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials");
        }
    }

    @Override
    public void logoutByToken(String token) {
        jwtBlacklistService.blacklistToken(token);
    }

    @Override
    public void logout() {
        List<UserTokens> userTokens = jwtProvider.getUserTokens();
        if(userTokens != null) {
            userTokens.forEach(userToken -> jwtBlacklistService.blacklistToken(userToken.getToken()));
        }
    }
}