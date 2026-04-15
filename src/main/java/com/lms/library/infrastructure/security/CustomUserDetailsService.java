package com.lms.library.infrastructure.security;

import com.lms.library.domain.entity.User;
import com.lms.library.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {
    
    private final UserRepository userRepository;
    
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

                String roleName = "USER";
                if (user.getRoles() != null && !user.getRoles().isEmpty()) {
                        if (user.getRoles().stream().anyMatch(r -> "ADMIN".equalsIgnoreCase(r.getName()))) {
                                roleName = "ADMIN";
                        } else if (user.getRoles().stream().anyMatch(r -> "LIBRARIAN".equalsIgnoreCase(r.getName()))) {
                                roleName = "LIBRARIAN";
                        }
                }

        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPassword(),
                user.isActive(),
                true, // account non expired
                true, // credentials non expired
                !user.isLocked(), // account non locked
                java.util.List.of(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_" + roleName))
        );
    }
}
