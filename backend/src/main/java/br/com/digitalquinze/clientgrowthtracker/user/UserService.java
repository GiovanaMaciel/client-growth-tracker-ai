package br.com.digitalquinze.clientgrowthtracker.user;

import java.util.Locale;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.digitalquinze.clientgrowthtracker.restaurant.Restaurant;
import br.com.digitalquinze.clientgrowthtracker.restaurant.RestaurantNotFoundException;
import br.com.digitalquinze.clientgrowthtracker.restaurant.RestaurantRepository;
import br.com.digitalquinze.clientgrowthtracker.user.dto.CreateUserRequest;
import br.com.digitalquinze.clientgrowthtracker.user.dto.UserResponse;

@Service
public class UserService {

    private final AppUserRepository userRepository;
    private final RestaurantRepository restaurantRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(
            AppUserRepository userRepository,
            RestaurantRepository restaurantRepository,
            PasswordEncoder passwordEncoder) {

        this.userRepository = userRepository;
        this.restaurantRepository = restaurantRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public UserResponse create(
            CreateUserRequest request) {

        String email =
                request.email()
                    .trim()
                    .toLowerCase(Locale.ROOT);

        if (userRepository
                .existsByEmailIgnoreCase(email)) {

            throw new IllegalArgumentException(
                "Email is already registered"
            );
        }

        Restaurant restaurant = null;

        if (request.role()
                == UserRole.RESTAURANT_USER) {

            if (request.restaurantId() == null) {

                throw new IllegalArgumentException(
                    "Restaurant user must have a restaurant"
                );
            }

            restaurant = restaurantRepository
                    .findById(request.restaurantId())
                    .orElseThrow(
                        () -> new RestaurantNotFoundException(
                            request.restaurantId()
                        )
                    );
        }

        if (request.role()
                == UserRole.AGENCY_ADMIN
                && request.restaurantId() != null) {

            throw new IllegalArgumentException(
                "Agency admin cannot be associated with a restaurant"
            );
        }

        AppUser user =
                new AppUser(
                    request.name().trim(),
                    email,
                    passwordEncoder.encode(
                        request.password()
                    ),
                    request.role(),
                    restaurant
                );

        AppUser saved =
                userRepository.save(user);

        return toResponse(saved);
    }

    private UserResponse toResponse(AppUser user) {

        Long restaurantId =
                user.getRestaurant() == null
                    ? null
                    : user.getRestaurant().getId();

        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole(),
                restaurantId,
                user.isActive(),
                user.getCreatedAt()
        );
    }
}