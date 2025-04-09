package org.example.backend.infrastructure.security.jwt;

import org.example.backend.infrastructure.security.user.CustomUserDetail;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

@Component
public class JwtAuthenticationProvider implements AuthenticationProvider {

    private final UserDetailsService userDetailsService;

    public JwtAuthenticationProvider(UserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String token = ((JwtAuthenticationToken) authentication).getToken();
        if (token != null) {
            String username = JwtUtil.extractUsername(token);
            if (JwtUtil.validateToken(token, username)) {
                CustomUserDetail userDetails = (CustomUserDetail) userDetailsService.loadUserByUsername(username);
                return JwtAuthenticationToken.authenticated(userDetails);
            }
        }
        throw new InvalidJwtTokenException("Invalid JWT token");
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return JwtAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
