package br.com.digitalquinze.clientgrowthtracker.user;

import java.util.Locale;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(
    name = "app.bootstrap-admin.enabled",
    havingValue = "true"
)
public class AdminBootstrap
        implements ApplicationRunner {

    private final AppUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    private final String name;
    private final String email;
    private final String password;

    public AdminBootstrap(
            AppUserRepository userRepository,
            PasswordEncoder passwordEncoder,
            @Value("${app.bootstrap-admin.name}") String name,
            @Value("${app.bootstrap-admin.email}") String email,
            @Value("${app.bootstrap-admin.password}") String password) {

        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.name = name;
        this.email = email;
        this.password = password;
    }

    @Override
    public void run(ApplicationArguments args) {

        if (name.isBlank()
                || email.isBlank()
                || password.isBlank()) {

            throw new IllegalStateException(
                "Bootstrap admin variables are missing"
            );
        }

        String normalizedEmail =
                email.trim().toLowerCase(Locale.ROOT);

        if (userRepository
                .existsByEmailIgnoreCase(normalizedEmail)) {

            return;
        }

        AppUser admin =
                new AppUser(
                    name.trim(),
                    normalizedEmail,
                    passwordEncoder.encode(password),
                    UserRole.AGENCY_ADMIN,
                    null
                );

        userRepository.save(admin);
    }
}