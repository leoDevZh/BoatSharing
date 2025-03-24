package org.example.backend.infrastructure.security.jwt;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.UserDetails;

public class JwtAuthenticationToken extends AbstractAuthenticationToken {

    private String username;
    private String token;

    private JwtAuthenticationToken(UserDetails userDetails) {
        super(userDetails.getAuthorities());
        super.setAuthenticated(true);
        this.username = userDetails.getUsername();
    }

    private JwtAuthenticationToken(String token) {
        super(AuthorityUtils.NO_AUTHORITIES);
        super.setAuthenticated(false);
        this.token = token;
    }

    public static JwtAuthenticationToken authenticated(UserDetails userDetails) {
        return new JwtAuthenticationToken(userDetails);
    }

    public static JwtAuthenticationToken unauthenticated(String token) {
        return new JwtAuthenticationToken(token);
    }

    public String getToken() {
        return token;
    }

    @Override
    public Object getCredentials() {
        return null;
    }

    @Override
    public Object getPrincipal() {
        return username;
    }

    @Override
    public void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException {
        throw new RuntimeException("Cannot change authenticated status");
    }
}
