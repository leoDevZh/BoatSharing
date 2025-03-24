package org.example.backend.infrastructure.security.user;

import org.example.backend.infrastructure.repository.user.JpaUserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

@Component
public class CustomUserDetailService implements UserDetailsService {

    private final JpaUserRepository userRepository;

    public CustomUserDetailService(JpaUserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        org.example.backend.infrastructure.repository.user.User user = userRepository.findByUsername(username).orElseThrow(() -> new UsernameNotFoundException(username));
        return CustomUserDetail.builder()
                .id(user.getId())
                .username(user.getUsername())
                .password(user.getPassword())
                .build();
    }
}
