package letsplay.java.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import letsplay.java.entity.User;
import letsplay.java.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
public class DatabaseSeeder {
    @Value("${admin.username}")
    private String adminUsername;

    @Value("${admin.password}")
    private String adminPassword;

    @Value("${admin.email}")
    private String adminEmail;

    @Bean
    public CommandLineRunner seedDatabase(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            if (!userRepository.existsByEmail(adminEmail)) {
                User admin = User.builder()
                        .username(adminUsername)
                        .email(adminEmail)
                        .passwordHash(passwordEncoder.encode(adminPassword))
                        .role("ADMIN")
                        .build();

                userRepository.save(admin);
                log.info("✅ Super Admin created successfully with email: {}", adminEmail);
            }
        };
    }
}