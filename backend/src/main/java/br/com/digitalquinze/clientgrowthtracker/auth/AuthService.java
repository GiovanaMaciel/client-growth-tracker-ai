package br.com.digitalquinze.clientgrowthtracker.auth;

import java.util.Locale;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.digitalquinze.clientgrowthtracker.auth.dto.LoginRequest;
import br.com.digitalquinze.clientgrowthtracker.auth.dto.LoginResponse;
import br.com.digitalquinze.clientgrowthtracker.user.AppUser;
import br.com.digitalquinze.clientgrowthtracker.user.AppUserRepository;

@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final AppUserRepository userRepository;
    private final JwtService jwtService;

    public AuthService(
            AuthenticationManager authenticationManager,
            AppUserRepository userRepository,
            JwtService jwtService) {

        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.jwtService = jwtService;
    }

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {

        String email = request
                .email()
                .trim()
                .toLowerCase(Locale.ROOT);

        authenticationManager.authenticate(
            UsernamePasswordAuthenticationToken
                .unauthenticated(
                    email,
                    request.password()
                )
        );

        AppUser user = userRepository
                .findByEmailIgnoreCase(email)
                .orElseThrow();

        String token =
                jwtService.generateToken(user);

        Long restaurantId =
                user.getRestaurant() == null
                    ? null
                    : user.getRestaurant().getId();

        return new LoginResponse(
                token,
                "Bearer",
                jwtService.getExpirationSeconds(),
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole(),
                restaurantId
        );
    }
}