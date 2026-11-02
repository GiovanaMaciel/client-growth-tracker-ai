package br.com.digitalquinze.clientgrowthtracker.user.dto;

import java.time.OffsetDateTime;

import br.com.digitalquinze.clientgrowthtracker.user.UserRole;

public record UserResponse(

    Long id,
    String name,
    String email,
    UserRole role,
    Long restaurantId,
    boolean active,
    OffsetDateTime createdAt

) {
}