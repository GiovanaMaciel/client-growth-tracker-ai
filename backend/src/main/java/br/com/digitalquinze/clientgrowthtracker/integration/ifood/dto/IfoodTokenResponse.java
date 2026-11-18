package br.com.digitalquinze.clientgrowthtracker.integration.ifood.dto;

public record IfoodTokenResponse(
        String accessToken,
        String type,
        Long expiresIn
) {
}
