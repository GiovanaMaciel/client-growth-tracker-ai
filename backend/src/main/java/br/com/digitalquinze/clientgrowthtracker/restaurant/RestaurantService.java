package br.com.digitalquinze.clientgrowthtracker.restaurant;

import java.util.List;

import org.springframework.stereotype.Service;

import br.com.digitalquinze.clientgrowthtracker.restaurant.dto.CreateRestaurantRequest;
import br.com.digitalquinze.clientgrowthtracker.restaurant.dto.RestaurantResponse;
import br.com.digitalquinze.clientgrowthtracker.restaurant.dto.UpdateRestaurantRequest;

@Service
public class RestaurantService {

    private final RestaurantRepository restaurantRepository;

    public RestaurantService(RestaurantRepository restaurantRepository) {
        this.restaurantRepository = restaurantRepository;
    }

    public RestaurantResponse create(CreateRestaurantRequest request) {

        Restaurant restaurant = new Restaurant(request.name());

        Restaurant savedRestaurant =
                restaurantRepository.save(restaurant);

        return toResponse(savedRestaurant);
    }

    public List<RestaurantResponse> findAll() {

        return restaurantRepository
                .findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public RestaurantResponse findById(Long id) {

        Restaurant restaurant = restaurantRepository
                .findById(id)
                .orElseThrow(
                    () -> new RestaurantNotFoundException(id)
                );

        return toResponse(restaurant);
    }

    public RestaurantResponse update(
            Long id,
            UpdateRestaurantRequest request) {

        Restaurant restaurant = restaurantRepository
                .findById(id)
                .orElseThrow(
                    () -> new RestaurantNotFoundException(id)
                );

        restaurant.setName(request.name());

        Restaurant updatedRestaurant =
                restaurantRepository.save(restaurant);

        return toResponse(updatedRestaurant);
    }

    public void delete(Long id) {

        Restaurant restaurant = restaurantRepository
                .findById(id)
                .orElseThrow(
                    () -> new RestaurantNotFoundException(id)
                );

        restaurantRepository.delete(restaurant);
    }

    private RestaurantResponse toResponse(Restaurant restaurant) {

        return new RestaurantResponse(
                restaurant.getId(),
                restaurant.getName(),
                restaurant.getCreatedAt(),
                restaurant.getUpdatedAt()
        );
    }
}