package br.com.digitalquinze.clientgrowthtracker.restaurantchannel;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface RestaurantChannelRepository
        extends JpaRepository<RestaurantChannel, Long> {

    List<RestaurantChannel>
        findAllByRestaurant_IdOrderByChannel_NameAsc(
            Long restaurantId
        );

    Optional<RestaurantChannel>
        findByIdAndRestaurant_Id(
            Long id,
            Long restaurantId
        );

    boolean existsByRestaurant_IdAndChannel_Id(
        Long restaurantId,
        Long channelId
    );
}