package com.lms.library.infrastructure.security;

import com.lms.library.domain.entity.Role;
import com.lms.library.domain.entity.User;
import com.lms.library.domain.entity.UserProfile;
import com.lms.library.domain.repository.RoleRepository;
import com.lms.library.domain.repository.UserProfileRepository;
import com.lms.library.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserProfileRepository userProfileRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);
        
        Map<String, Object> attributes = oAuth2User.getAttributes();
        String email = (String) attributes.get("email");
        String name = (String) attributes.get("name");
        String picture = (String) attributes.get("picture");

        log.info("Loading OAuth2 user from Google: email={}, name={}", email, name);

        User user = userRepository.findByEmail(email)
                .map(existingUser -> {
                    log.info("OAuth2 user already exists: email={}", email);
                    existingUser.setFullName(name);
                    if (picture != null) {
                        existingUser.setAvatarUrl(picture);
                    }
                    return userRepository.save(existingUser);
                })
                .orElseGet(() -> createNewUser(email, name, picture));

        return new CustomOAuth2User(user, attributes);
    }

    private User createNewUser(String email, String name, String picture) {
        log.info("Creating new OAuth2 user: email={}", email);
        
        Role userRole = roleRepository.findByName("USER")
                .orElseThrow(() -> new RuntimeException("Role USER không tồn tại"));

        User user = User.builder()
                .email(email)
                .fullName(name)
                .avatarUrl(picture)
                .password(passwordEncoder.encode(UUID.randomUUID().toString()))
                .status(User.UserStatus.ACTIVE)
                .roles(new ArrayList<>(java.util.List.of(userRole)))
                .emailVerified(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        User savedUser = userRepository.save(user);

        // Tạo UserProfile tương thích với hệ thống
        UserProfile userProfile = UserProfile.builder()
                .userId(savedUser.getId())
                .email(savedUser.getEmail())
                .fullName(savedUser.getFullName())
                .cardExpiryDate(java.time.LocalDate.now().plusYears(1))
                .membershipLevel(UserProfile.MembershipLevel.BRONZE)
                .outstandingFines(java.math.BigDecimal.ZERO)
                .currentBooksBorrowed(0)
                .points(0)
                .build();
        userProfileRepository.save(userProfile);

        return savedUser;
    }
}
