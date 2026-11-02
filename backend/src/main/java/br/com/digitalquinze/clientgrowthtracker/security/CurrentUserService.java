package br.com.digitalquinze.clientgrowthtracker.security;

import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;

import br.com.digitalquinze.clientgrowthtracker.user.UserRole;

@Service
public class CurrentUserService {

    private JwtAuthenticationToken getAuthentication() {

        Authentication authentication =
                SecurityContextHolder
                    .getContext()
                    .getAuthentication();

        if (!(authentication instanceof JwtAuthenticationToken jwtAuthentication)) {
            throw new InsufficientAuthenticationException(
                "Authenticated JWT user is required"
            );
        }

        return jwtAuthentication;
    }

    public String getEmail() {
        return getAuthentication()
                .getToken()
                .getSubject();
    }

    public Long getUserId() {
        return getLongClaim("userId");
    }

    public Long getRestaurantId() {

        Object claim = getAuthentication()
                .getToken()
                .getClaim("restaurantId");

        if (claim == null) {
            return null;
        }

        if (claim instanceof Number number) {
            return number.longValue();
        }

        return Long.valueOf(claim.toString());
    }

    public boolean hasRole(UserRole role) {

        String authority =
                "ROLE_" + role.name();

        return getAuthentication()
                .getAuthorities()
                .stream()
                .anyMatch(grantedAuthority ->
                    grantedAuthority
                        .getAuthority()
                        .equals(authority)
                );
    }

    public boolean isAgencyAdmin() {
        return hasRole(UserRole.AGENCY_ADMIN);
    }

    public boolean isRestaurantUser() {
        return hasRole(UserRole.RESTAURANT_USER);
    }

    private Long getLongClaim(String name) {

        Object claim = getAuthentication()
                .getToken()
                .getClaim(name);

        if (claim instanceof Number number) {
            return number.longValue();
        }

        return Long.valueOf(claim.toString());
    }
}