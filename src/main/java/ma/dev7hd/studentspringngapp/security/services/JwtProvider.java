package ma.dev7hd.studentspringngapp.security.services;

import lombok.AllArgsConstructor;
import ma.dev7hd.studentspringngapp.entities.users.User;
import ma.dev7hd.studentspringngapp.entities.tokens.UserTokens;
import ma.dev7hd.studentspringngapp.repositories.users.UserRepository;
import ma.dev7hd.studentspringngapp.repositories.tokens.UserTokensRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@AllArgsConstructor
@Service
public class JwtProvider implements IJwtProvider {
    private final AuthenticationManager authenticationManager;
    private final JwtEncoder jwtEncoder;
    private final UserTokensRepository userTokensRepository;
    private final UserRepository userRepository ;

    @Override
    public Map<String, String> getJWT(String username, String password) {
        Authentication authentication = authenticateUser(username, password);
        String jwt = generateToken(authentication, username);
        return Map.of("access_token", jwt);
    }

    @Override
    public List<UserTokens> getUserTokens(){
        String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        Optional<List<UserTokens>> userTokens = userTokensRepository.findByEmail(userEmail);
        return userTokens.orElse(null);
    }

    private String generateToken(Authentication authentication, String username) {
        Instant now = Instant.now();
        String scope = getScopes(authentication);

        Optional<User> optionalUser = userRepository.findById(username);

        if (optionalUser.isPresent()) {
            User user = optionalUser.get();

            boolean isPasswordChanged = user.isPasswordChanged();

            // Build the JWT claims set with additional claims
            JwtClaimsSet claims = JwtClaimsSet.builder()
                    .issuedAt(now)
                    .expiresAt(now.plus(24, ChronoUnit.HOURS))
                    .subject(authentication.getName())
                    .claim("scope", scope)
                    .claim("isPasswordChanged", isPasswordChanged) // Add credentialsNonExpired to claims
                    .build();

            // Create JWT header and encoder parameters
            JwsHeader header = JwsHeader.with(MacAlgorithm.HS512).build();
            JwtEncoderParameters parameters = JwtEncoderParameters.from(header, claims);

            // Encode and return the JWT
            return jwtEncoder.encode(parameters).getTokenValue();
        }

        return null;
    }


    private String getScopes(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(" "));
    }

    private Authentication authenticateUser(String username, String password) {
        return authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
    }
}
