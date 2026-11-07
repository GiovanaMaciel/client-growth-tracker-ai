package br.com.digitalquinze.clientgrowthtracker.restaurantchannel.dto;

import jakarta.validation.constraints.NotNull;

public record CreateRestaurantChannelRequest(

    @NotNull(message = "Channel is required")
    Long channelId,

    String externalStoreId

) {
}