package br.com.digitalquinze.clientgrowthtracker.security;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

@Service
public class TenantAccessService {

    private final CurrentUserService currentUserService;

    public TenantAccessService(
            CurrentUserService currentUserService) {

        this.currentUserService = currentUserService;
    }

    public void validateRestaurantAccess(Long restaurantId) {

        if (currentUserService.isAgencyAdmin()) {
            return;
        }

        Long currentRestaurantId =
                currentUserService.getRestaurantId();

        if (currentRestaurantId == null
                || !currentRestaurantId.equals(restaurantId)) {

            throw new AccessDeniedException(
                "You do not have access to this restaurant"
            );
        }
    }
}