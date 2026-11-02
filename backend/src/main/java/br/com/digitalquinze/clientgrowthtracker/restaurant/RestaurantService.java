package br.com.digitalquinze.clientgrowthtracker.restaurant;

import java.util.List;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import br.com.digitalquinze.clientgrowthtracker.restaurant.dto.CreateRestaurantRequest;
import br.com.digitalquinze.clientgrowthtracker.restaurant.dto.RestaurantResponse;
import br.com.digitalquinze.clientgrowthtracker.restaurant.dto.UpdateRestaurantRequest;
import br.com.digitalquinze.clientgrowthtracker.security.CurrentUserService;

@Service
public class RestaurantService {

    private final RestaurantRepository restaurantRepository;
    private final CurrentUserService currentUserService;

    public RestaurantService(
            RestaurantRepository restaurantRepository,
            CurrentUserService currentUserService) {

        this.restaurantRepository = restaurantRepository;
        this.currentUserService = currentUserService;
    }

    @PreAuthorize("hasRole('AGENCY_ADMIN')")
    public RestaurantResponse create(
            CreateRestaurantRequest request) {

        Restaurant restaurant =
                new Restaurant(request.name());

        Restaurant savedRestaurant =
                restaurantRepository.save(restaurant);

        return toResponse(savedRestaurant);
    }

    public List<RestaurantResponse> findAll() {

        if (currentUserService.isAgencyAdmin()) {

            return restaurantRepository
                    .findAll()
                    .stream()
                    .map(this::toResponse)
                    .toList();
        }

        Long restaurantId =
                currentUserService.getRestaurantId();

        if (restaurantId == null) {

            throw new AccessDeniedException(
                    "Restaurant user is not associated with a restaurant"
            );
        }

        Restaurant restaurant =
                restaurantRepository
                    .findById(restaurantId)
                    .orElseThrow(
                        () -> new RestaurantNotFoundException(
                                restaurantId
                        )
                    );

        return List.of(
                toResponse(restaurant)
        );
    }

    public RestaurantResponse findById(Long id) {

        validateRestaurantAccess(id);

        Restaurant restaurant =
                restaurantRepository
                    .findById(id)
                    .orElseThrow(
                        () -> new RestaurantNotFoundException(id)
                    );

        return toResponse(restaurant);
    }

    @PreAuthorize("hasRole('AGENCY_ADMIN')")
    public RestaurantResponse update(
            Long id,
            UpdateRestaurantRequest request) {

        Restaurant restaurant =
                restaurantRepository
                    .findById(id)
                    .orElseThrow(
                        () -> new RestaurantNotFoundException(id)
                    );

        restaurant.setName(request.name());

        Restaurant updatedRestaurant =
                restaurantRepository.save(restaurant);

        return toResponse(updatedRestaurant);
    }

    @PreAuthorize("hasRole('AGENCY_ADMIN')")
    public void delete(Long id) {

        Restaurant restaurant =
                restaurantRepository
                    .findById(id)
                    .orElseThrow(
                        () -> new RestaurantNotFoundException(id)
                    );

        restaurantRepository.delete(restaurant);
    }

    private void validateRestaurantAccess(
            Long restaurantId) {

        if (currentUserService.isAgencyAdmin()) {
            return;
        }

        Long currentRestaurantId =
                currentUserService.getRestaurantId();

        if (currentRestaurantId == null
                || !currentRestaurantId.equals(
                        restaurantId)) {

            throw new AccessDeniedException(
                    "You do not have access to this restaurant"
            );
        }
    }

    private RestaurantResponse toResponse(
            Restaurant restaurant) {

        return new RestaurantResponse(
                restaurant.getId(),
                restaurant.getName(),
                restaurant.getCreatedAt(),
                restaurant.getUpdatedAt()
        );
    }
}