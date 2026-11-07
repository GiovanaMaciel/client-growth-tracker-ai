package br.com.digitalquinze.clientgrowthtracker.channel;

public record ChannelResponse(
    Long id,
    String name,
    String slug
) {
}