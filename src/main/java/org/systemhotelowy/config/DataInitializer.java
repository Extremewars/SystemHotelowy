package org.systemhotelowy.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.systemhotelowy.model.Role;
import org.systemhotelowy.model.User;
import org.systemhotelowy.repository.UserRepository;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner initAdmin(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            if (userRepository.findByEmail("admin@local.com").isEmpty()) {
                User admin = new User("System", "Administrator", "admin@local.com",
                        passwordEncoder.encode("admin123!"), Role.ADMIN);
                userRepository.save(admin);
                System.out.println("Admin user created: admin@local.com / admin123!");
            }
        };
    }
}
