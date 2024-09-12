package ma.dev7hd.studentspringngapp.security.services;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import ma.dev7hd.studentspringngapp.entities.Admin;
import ma.dev7hd.studentspringngapp.entities.Student;
import ma.dev7hd.studentspringngapp.entities.User;
import ma.dev7hd.studentspringngapp.entities.UserTokens;
import ma.dev7hd.studentspringngapp.repositories.UserRepository;
import ma.dev7hd.studentspringngapp.repositories.UserTokensRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@AllArgsConstructor
@Transactional
public class SecurityService implements ISecurityService {

    private final IJwtBlacklistService jwtBlacklistService;
    private final UserTokensRepository tokensRepository;
    private final IJwtProvider jwtProvider;
    private final UserRepository userRepository;

    @Override
    public ResponseEntity<?> login(String username, String password) {
        try{
            Map<String, String> jwt = jwtProvider.getJWT(username, password);
            Optional<User> optionalUser = userRepository.findById(username);
            Map<String, String> loginInfo = new HashMap<>();
            if (optionalUser.isPresent()) {
                User user = optionalUser.get();

                UserTokens userTokens = new UserTokens();
                userTokens.setToken(jwt.get("access_token"));
                userTokens.setEmail(username);
                userTokens.setLoginTime(new Date());

                loginInfo.put("access_token", jwt.get("access_token"));
                loginInfo.put("email", user.getEmail());
                loginInfo.put("firstName", user.getFirstName());
                loginInfo.put("lastName", user.getLastName());

                if (user instanceof Admin) {
                    loginInfo.put("roles", "ROLE_ADMIN ROLE_STUDENT");
                    loginInfo.put("departmentName", ((Admin) user).getDepartmentName().toString());
                } else if (user instanceof Student) {
                    loginInfo.put("roles", "ROLE_STUDENT");
                    loginInfo.put("programId", ((Student) user).getProgramId().toString());
                }

                tokensRepository.save(userTokens);
                return ResponseEntity.ok(loginInfo);
            } else {
                throw new BadCredentialsException("Invalid username or password");
            }

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