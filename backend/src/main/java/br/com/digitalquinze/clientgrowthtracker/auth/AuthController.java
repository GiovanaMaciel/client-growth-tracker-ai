package br.com.digitalquinze.clientgrowthtracker.auth;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.digitalquinze.clientgrowthtracker.auth.dto.LoginRequest;
import br.com.digitalquinze.clientgrowthtracker.auth.dto.LoginResponse;
import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.GetMapping;

import br.com.digitalquinze.clientgrowthtracker.auth.dto.CurrentUserResponse;
import br.com.digitalquinze.clientgrowthtracker.security.CurrentUserService;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final CurrentUserService currentUserService;

    public AuthController(
    		AuthService authService, 
    		CurrentUserService currentUserService) {
    	
    	
        this.authService = authService;
        this.currentUserService = currentUserService;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody LoginRequest request) {

        return ResponseEntity.ok(
            authService.login(request)
        );
    }
    
    @GetMapping("/me")
    public ResponseEntity<CurrentUserResponse> me() {

        return ResponseEntity.ok(
            new CurrentUserResponse(
                currentUserService.getUserId(),
                currentUserService.getEmail(),
                currentUserService.getRestaurantId(),
                currentUserService.isAgencyAdmin()
            )
        );
    }
}