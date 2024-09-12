package ma.dev7hd.studentspringngapp.security.services;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import ma.dev7hd.studentspringngapp.entities.UserTokens;
import ma.dev7hd.studentspringngapp.repositories.UserTokensRepository;
import org.jetbrains.annotations.NotNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@AllArgsConstructor
@Service
public class JwtBlacklistFilter extends OncePerRequestFilter {


    private final IJwtBlacklistService jwtBlacklistService;
    private final JwtDecoder jwtDecoder;

    private static final String[] WHITE_LIST_URL = { "/api/v1/auth/", "/v2/api-docs", "/v3/api-docs",
            "/v3/api-docs/", "/swagger-resources", "/swagger-resources/", "/configuration/ui",
            "/configuration/security", "/swagger-ui/", "/webjars/", "/swagger-ui.html", "/api/auth/",
            "/api/test/", "/authenticate", "/auth/login", "/user/register", "/ws" };
    private final UserTokensRepository userTokensRepository;

    @Override
    protected void doFilterInternal(@NotNull HttpServletRequest request, @NotNull HttpServletResponse response, @NotNull FilterChain chain)
            throws ServletException, IOException {

        String token = getTokenFromRequest(request);

        String path = request.getRequestURI();

        for (String url : WHITE_LIST_URL) {
            if (path.startsWith(url)) {
                chain.doFilter(request, response);
                return;
            }
        }

        if (token == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Token error!");
            System.out.println("Token error!");
            return;
        }

        if (jwtBlacklistService.isTokenBlacklisted(token)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Token is blacklisted");
            return;
        }

        if (userTokensRepository.findByTokenHash(UserTokens.hashToken(token)).isEmpty()) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Try to login again.");
            return;
        }

        // Set authentication in the context if the token is valid and not blacklisted
        Authentication authentication = getAuthentication(token);
        SecurityContextHolder.getContext().setAuthentication(authentication);


        chain.doFilter(request, response);
    }

    private String getTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    private Authentication getAuthentication(String token) {
        Jwt jwt = jwtDecoder.decode(token);
        Collection<GrantedAuthority> authorities = extractAuthorities(jwt);
        return new JwtAuthenticationToken(jwt, authorities);
    }

    private Collection<GrantedAuthority> extractAuthorities(Jwt jwt) {
        List<String> roles = jwt.getClaimAsStringList("scope");

        if (roles == null) {
            return List.of();
        }

        return roles.stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
    }

}
