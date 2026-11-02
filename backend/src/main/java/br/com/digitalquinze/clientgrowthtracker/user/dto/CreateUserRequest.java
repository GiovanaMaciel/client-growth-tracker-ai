package br.com.digitalquinze.clientgrowthtracker.user.dto;

import br.com.digitalquinze.clientgrowthtracker.user.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateUserRequest(

    @NotBlank
    @Size(max = 120)
    String name,

    @NotBlank
    @Email
    String email,

    @NotBlank
    @Size(min = 8)
    String password,

    @NotNull
    UserRole role,

    Long restaurantId

) {
}