package br.com.digitalquinze.clientgrowthtracker.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {

        return PasswordEncoderFactories
                .createDelegatingPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(
            UserDetailsService userDetailsService,
            PasswordEncoder passwordEncoder) {

        DaoAuthenticationProvider provider =
                new DaoAuthenticationProvider(
                        userDetailsService
                );

        provider.setPasswordEncoder(passwordEncoder);

        return new ProviderManager(provider);
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {

        JwtGrantedAuthoritiesConverter authorities =
                new JwtGrantedAuthoritiesConverter();

        authorities.setAuthoritiesClaimName("roles");
        authorities.setAuthorityPrefix("ROLE_");

        JwtAuthenticationConverter converter =
                new JwtAuthenticationConverter();

        converter.setJwtGrantedAuthoritiesConverter(
                authorities
        );

        return converter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            JwtAuthenticationConverter jwtAuthenticationConverter)
            throws Exception {

        http

            // Stateless API using JWT
            .csrf(csrf -> csrf.disable())

            .sessionManagement(session ->
                session.sessionCreationPolicy(
                    SessionCreationPolicy.STATELESS
                )
            )

            .authorizeHttpRequests(auth -> auth

                // =====================================================
                // AUTH
                // =====================================================
                .requestMatchers(
                    HttpMethod.POST,
                    "/api/auth/login"
                )
                .permitAll()

                .requestMatchers(
                    HttpMethod.GET,
                    "/api/auth/me"
                )
                .authenticated()


                // =====================================================
                // CHANNELS
                // =====================================================

                // iFood, 99 Food, Anota Ai...
                .requestMatchers(
                    HttpMethod.GET,
                    "/api/channels/**"
                )
                .authenticated()


                // =====================================================
                // CHANNEL METRICS
                //
                // IMPORTANT:
                // these rules must come BEFORE the generic rules
                // for /api/restaurants/**
                //
                // A restaurant can provide its own data. 
                // TenantAccessService verifies that it is the correct restaurant.
                // =====================================================

                .requestMatchers(
                    HttpMethod.POST,
                    "/api/restaurants/*/periods/*/channel-metrics"
                )
                .authenticated()

                .requestMatchers(
                    HttpMethod.PUT,
                    "/api/restaurants/*/periods/*/channel-metrics/*"
                )
                .authenticated()

                .requestMatchers(
                    HttpMethod.DELETE,
                    "/api/restaurants/*/periods/*/channel-metrics/*"
                )
                .authenticated()

                .requestMatchers(
                    HttpMethod.GET,
                    "/api/restaurants/*/periods/*/channel-metrics/**"
                )
                .authenticated()


                // =====================================================
                // REPORTING PERIODS
                //
                // Restaurants can create and edit their own time periods. 
                // TenantAccessService ensures isolation.
                // =====================================================

                .requestMatchers(
                    HttpMethod.POST,
                    "/api/restaurants/*/periods"
                )
                .authenticated()

                .requestMatchers(
                    HttpMethod.PUT,
                    "/api/restaurants/*/periods/*"
                )
                .authenticated()

                .requestMatchers(
                    HttpMethod.DELETE,
                    "/api/restaurants/*/periods/*"
                )
                .authenticated()

                .requestMatchers(
                    HttpMethod.GET,
                    "/api/restaurants/*/periods/**"
                )
                .authenticated()


                // =====================================================
                // USERS
                //
                // Only the agency creates/manages accounts.
                // =====================================================

                .requestMatchers(
                    "/api/users/**"
                )
                .hasRole("AGENCY_ADMIN")


                // =====================================================
                // RESTAURANTS
                //
                // Create/modify/delete restaurants:
                // agency only.
                // =====================================================

                .requestMatchers(
                    HttpMethod.POST,
                    "/api/restaurants/**"
                )
                .hasRole("AGENCY_ADMIN")
                
                .requestMatchers(
                	HttpMethod.PUT,
                	"/api/restaurants/*/periods/*/channel-metrics/*/funnel"
                )
                .authenticated()

                .requestMatchers(
                    HttpMethod.PUT,
                    "/api/restaurants/**"
                )
                .hasRole("AGENCY_ADMIN")

                .requestMatchers(
                    HttpMethod.DELETE,
                    "/api/restaurants/**"
                )
                .hasRole("AGENCY_ADMIN")


                // View restaurants:
                // any authenticated user. 
                //
                // RestaurantService/TenantAccessService determines
                // which restaurants that user is permitted to see
                .requestMatchers(
                    HttpMethod.GET,
                    "/api/restaurants/**"
                )
                .authenticated()


                // =====================================================
                // ANY OTHER ROUTE
                // =====================================================

                .anyRequest()
                .authenticated()
            )

            // Makes Spring Security validate the JWT sent in:
            //
            // Authorization: Bearer <token>
            //
            .oauth2ResourceServer(oauth ->
                oauth.jwt(jwt ->
                    jwt.jwtAuthenticationConverter(
                        jwtAuthenticationConverter
                    )
                )
            );

        return http.build();
    }
}