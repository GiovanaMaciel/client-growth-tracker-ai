package br.com.digitalquinze.clientgrowthtracker.restaurantchannel;

public class RestaurantChannelAlreadyExistsException
        extends RuntimeException {

    public RestaurantChannelAlreadyExistsException() {
        super(
            "This channel is already associated with the restaurant"
        );
    }
}