package org.example.backend.infrastructure.security;

import org.example.backend.infrastructure.repository.user.JpaUserRepository;
import org.example.backend.infrastructure.security.jwt.JwtAuthenticationProvider;
import org.example.backend.infrastructure.security.jwt.JwtFilter;
import org.example.backend.infrastructure.security.user.CustomUserDetailService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFilter;

import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfiguration {

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http, JwtFilter jwtFilter, UserDetailsService userDetailsService) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(
                        authorizeHttp -> {
                            authorizeHttp.requestMatchers("/api/" + "/auth/login").permitAll();
                            authorizeHttp.anyRequest().authenticated();
                        }
                )
                .addFilterBefore(jwtFilter, AuthenticationFilter.class)
                .authenticationProvider(new JwtAuthenticationProvider(userDetailsService))
                .build();
    }

    @Bean
    UserDetailsService userDetailsService(JpaUserRepository jpaUserRepository) {
        return new CustomUserDetailService(jpaUserRepository);
    }

    @Bean
    public JwtAuthenticationProvider jwtAuthenticationProvider(UserDetailsService userDetailsService) {
        return new JwtAuthenticationProvider(userDetailsService);
    }

    @Bean
    public DaoAuthenticationProvider daoAuthenticationProvider(UserDetailsService userDetailsService) {
        DaoAuthenticationProvider daoProvider = new DaoAuthenticationProvider();
        daoProvider.setUserDetailsService(userDetailsService);
        return daoProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(
            JwtAuthenticationProvider jwtAuthenticationProvider,
            DaoAuthenticationProvider daoAuthenticationProvider
    ) {
        return new ProviderManager(List.of(jwtAuthenticationProvider, daoAuthenticationProvider));
    }
}
