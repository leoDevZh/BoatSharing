package org.example.backend.infrastructure.security.jwt;

import org.example.backend.infrastructure.security.user.CustomUserDetail;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;

public class JwtAuthenticationToken extends AbstractAuthenticationToken {

    private CustomUserDetail customUserDetail;
    private String token;

    private JwtAuthenticationToken(CustomUserDetail userDetails) {
        super(userDetails.getAuthorities());
        super.setAuthenticated(true);
        this.customUserDetail = clearPasswordFromCustomUserDetails(userDetails);
    }

    private JwtAuthenticationToken(String token) {
        super(AuthorityUtils.NO_AUTHORITIES);
        super.setAuthenticated(false);
        this.token = token;
    }

    public static JwtAuthenticationToken authenticated(CustomUserDetail userDetails) {
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
        return customUserDetail;
    }

    @Override
    public void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException {
        throw new RuntimeException("Cannot change authenticated status");
    }

    private static CustomUserDetail clearPasswordFromCustomUserDetails(CustomUserDetail customUserDetail) {
        return CustomUserDetail.builder()
                .id(customUserDetail.getId())
                .username(customUserDetail.getUsername())
                .build();
    }
}
