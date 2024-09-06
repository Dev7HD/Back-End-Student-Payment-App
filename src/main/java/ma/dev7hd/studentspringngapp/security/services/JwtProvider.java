package ma.dev7hd.studentspringngapp.security.services;

import lombok.AllArgsConstructor;
import ma.dev7hd.studentspringngapp.entities.UserTokens;
import ma.dev7hd.studentspringngapp.repositories.UserTokensRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
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

    @Override
    public Map<String, String> getJWT(String username, String password) {
        Authentication authentication = authenticateUser(username, password);
        String jwt = generateToken(authentication);
        return Map.of("access_token", jwt);
    }

    @Override
    public List<UserTokens> getUserTokens(){
        String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        Optional<List<UserTokens>> userTokens = userTokensRepository.findByEmail(userEmail);
        return userTokens.orElse(null);
    }

    private String generateToken(Authentication authentication) {
        Instant now = Instant.now();
        String scope = getScopes(authentication);

        // Cast principal to UserDetails or your custom User entity
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();

        // Retrieve account properties
        boolean isEnabled = userDetails.isEnabled();
        boolean accountNonLocked = userDetails.isAccountNonLocked();
        boolean credentialsNonExpired = userDetails.isCredentialsNonExpired();

        // Build the JWT claims set with additional claims
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuedAt(now)
                .expiresAt(now.plus(24, ChronoUnit.HOURS))
                .subject(authentication.getName())
                .claim("scope", scope)
                .claim("isEnabled", isEnabled) // Add isEnabled to claims
                .claim("accountNonLocked", accountNonLocked) // Add accountNonLocked to claims
                .claim("credentialsNonExpired", credentialsNonExpired) // Add credentialsNonExpired to claims
                .build();

        // Create JWT header and encoder parameters
        JwsHeader header = JwsHeader.with(MacAlgorithm.HS512).build();
        JwtEncoderParameters parameters = JwtEncoderParameters.from(header, claims);

        // Encode and return the JWT
        return jwtEncoder.encode(parameters).getTokenValue();
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
