package br.com.digitalquinze.clientgrowthtracker.restaurant.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateRestaurantRequest(

    @NotBlank(message = "Restaurant name is required")
    @Size(max = 160, message = "Restaurant name must have at most 160 characters")
    String name

) {
}