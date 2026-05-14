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
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfiguration {

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http, JwtFilter jwtFilter, UserDetailsService userDetailsService) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .authorizeHttpRequests(
                        authorizeHttp -> {
                            authorizeHttp.requestMatchers("/api/auth/login").permitAll();
                            authorizeHttp.requestMatchers("/api-docs").permitAll();
                            authorizeHttp.anyRequest().authenticated();
                        }
                )
                .addFilterBefore(jwtFilter, AuthenticationFilter.class)
                .authenticationProvider(jwtAuthenticationProvider(userDetailsService))
                .build();
    }

    @Bean
    PasswordEncoder getPasswordEncoder() {
        return new BCryptPasswordEncoder();
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
        daoProvider.setPasswordEncoder(getPasswordEncoder());
        return daoProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(
            JwtAuthenticationProvider jwtAuthenticationProvider,
            DaoAuthenticationProvider daoAuthenticationProvider
    ) {
        return new ProviderManager(List.of(jwtAuthenticationProvider, daoAuthenticationProvider));
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(List.of("*"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
