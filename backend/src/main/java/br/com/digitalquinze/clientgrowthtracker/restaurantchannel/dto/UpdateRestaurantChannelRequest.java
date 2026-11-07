package br.com.digitalquinze.clientgrowthtracker.restaurantchannel.dto;

import jakarta.validation.constraints.NotNull;

public record UpdateRestaurantChannelRequest(

    String externalStoreId,

    @NotNull
    Boolean active

) {
}