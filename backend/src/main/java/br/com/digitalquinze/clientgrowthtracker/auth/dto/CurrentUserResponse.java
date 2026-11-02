package br.com.digitalquinze.clientgrowthtracker.auth.dto;

public record CurrentUserResponse(
    Long userId,
    String email,
    Long restaurantId,
    boolean agencyAdmin
) {
}