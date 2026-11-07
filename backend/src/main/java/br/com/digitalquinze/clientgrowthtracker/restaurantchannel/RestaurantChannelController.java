package br.com.digitalquinze.clientgrowthtracker.restaurantchannel;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.digitalquinze.clientgrowthtracker.restaurantchannel.dto.CreateRestaurantChannelRequest;
import br.com.digitalquinze.clientgrowthtracker.restaurantchannel.dto.RestaurantChannelResponse;
import br.com.digitalquinze.clientgrowthtracker.restaurantchannel.dto.UpdateRestaurantChannelRequest;
import jakarta.validation.Valid;

@RestController
@RequestMapping(
    "/api/restaurants/{restaurantId}/channels"
)
public class RestaurantChannelController {

    private final RestaurantChannelService service;

    public RestaurantChannelController(
            RestaurantChannelService service) {

        this.service = service;
    }

    @PostMapping
    public ResponseEntity<RestaurantChannelResponse> create(
            @PathVariable Long restaurantId,
            @Valid
            @RequestBody CreateRestaurantChannelRequest request) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                    service.create(
                        restaurantId,
                        request
                    )
                );
    }

    @GetMapping
    public ResponseEntity<List<RestaurantChannelResponse>>
            findAll(
                @PathVariable Long restaurantId) {

        return ResponseEntity.ok(
            service.findAll(restaurantId)
        );
    }

    @PutMapping("/{restaurantChannelId}")
    public ResponseEntity<RestaurantChannelResponse> update(
            @PathVariable Long restaurantId,
            @PathVariable Long restaurantChannelId,
            @Valid
            @RequestBody UpdateRestaurantChannelRequest request) {

        return ResponseEntity.ok(
            service.update(
                restaurantId,
                restaurantChannelId,
                request
            )
        );
    }
}