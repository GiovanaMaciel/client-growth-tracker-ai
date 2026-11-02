package br.com.digitalquinze.clientgrowthtracker.restaurant.dto;

import java.time.OffsetDateTime;

public record RestaurantResponse(
    Long id,
    String name,
    OffsetDateTime createdAt,
    OffsetDateTime updatedAt
) {
}