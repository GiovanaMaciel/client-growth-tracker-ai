package br.com.digitalquinze.clientgrowthtracker.auth.dto;

import br.com.digitalquinze.clientgrowthtracker.user.UserRole;

public record LoginResponse(

    String accessToken,
    String tokenType,
    long expiresIn,

    Long userId,
    String name,
    String email,
    UserRole role,
    Long restaurantId

) {
}