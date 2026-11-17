package com.salessavvy;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.StringUtils;

import com.salessavvy.entity.User;
import com.salessavvy.repository.UserRepository;

@Configuration
@ConditionalOnProperty(
        name = "app.demo-password-reset.enabled",
        havingValue = "true",
        matchIfMissing = false)
public class AdminPasswordReset {

    @Bean
    CommandLineRunner resetPasswords(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder) {

        return args -> {
            String adminEmail = requiredEnv("ADMIN_EMAIL");
            String adminPassword = requiredEnv("ADMIN_PASSWORD");
            String customerEmail = requiredEnv("CUSTOMER_EMAIL");
            String customerPassword = requiredEnv("CUSTOMER_PASSWORD");

            resetPassword(userRepository, passwordEncoder, adminEmail, adminPassword);
            resetPassword(userRepository, passwordEncoder, customerEmail, customerPassword);
        };
    }

    private void resetPassword(
            UserRepository repository,
            PasswordEncoder encoder,
            String email,
            String rawPassword) {

        User user = repository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException(
                        "Configured demo account was not found: " + email));

        user.setPassword(encoder.encode(rawPassword));
        repository.save(user);
    }

    private String requiredEnv(String name) {
        String value = System.getenv(name);
        if (!StringUtils.hasText(value)) {
            throw new IllegalStateException(
                    "Set " + name + " when demo password reset is enabled.");
        }
        return value;
    }
}
