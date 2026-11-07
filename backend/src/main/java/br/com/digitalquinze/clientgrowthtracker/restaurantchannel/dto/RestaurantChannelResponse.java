package br.com.digitalquinze.clientgrowthtracker.restaurantchannel.dto;

import java.time.OffsetDateTime;

public record RestaurantChannelResponse(

    Long id,
    Long restaurantId,

    Long channelId,
    String channelName,
    String channelSlug,

    String externalStoreId,
    boolean active,

    OffsetDateTime createdAt,
    OffsetDateTime updatedAt

) {
}