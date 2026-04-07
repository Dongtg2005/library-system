package com.lms.library.auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import com.lms.library.auth.entity.User;
import com.lms.library.auth.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.time.LocalDateTime;

@SpringBootApplication
public class AuthServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(AuthServiceApplication.class, args);
	}

	// @Bean init disabled - schema incompatibility
	/*
	@Bean
	public CommandLineRunner initializeTestUser(UserRepository userRepository, PasswordEncoder passwordEncoder) {
		return args -> {
			// Create test user if not exists
			if (userRepository.findByEmail("abc123@gmail.com").isEmpty()) {
				User testUser = User.builder()
					.email("abc123@gmail.com")
					.password(passwordEncoder.encode("123456"))
					.fullName("Người Dùng Test")
					.role(User.Role.USER)
					.enabled(true)
					.createdAt(LocalDateTime.now())
					.build();
				userRepository.save(testUser);
				System.out.println("✓ Test user created: abc123@gmail.com (password: 123456)");
			}
		};
	}
	*/

}
