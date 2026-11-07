package br.com.digitalquinze.clientgrowthtracker.restaurantchannel;

public class RestaurantChannelNotFoundException
        extends RuntimeException {

    public RestaurantChannelNotFoundException(Long id) {
        super(
            "Restaurant channel not found with id: " + id
        );
    }
}