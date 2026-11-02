package br.com.digitalquinze.clientgrowthtracker.restaurant;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import br.com.digitalquinze.clientgrowthtracker.restaurant.dto.CreateRestaurantRequest;
import br.com.digitalquinze.clientgrowthtracker.restaurant.dto.RestaurantResponse;
import br.com.digitalquinze.clientgrowthtracker.restaurant.dto.UpdateRestaurantRequest;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/restaurants")
public class RestaurantController {

    private final RestaurantService restaurantService;

    public RestaurantController(
            RestaurantService restaurantService) {

        this.restaurantService = restaurantService;
    }

    @PostMapping
    public ResponseEntity<RestaurantResponse> create(
            @Valid @RequestBody CreateRestaurantRequest request) {

        RestaurantResponse restaurant =
                restaurantService.create(request);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(restaurant.id())
                .toUri();

        return ResponseEntity
                .created(location)
                .body(restaurant);
    }

    @GetMapping
    public ResponseEntity<List<RestaurantResponse>> findAll() {

        return ResponseEntity.ok(
                restaurantService.findAll()
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<RestaurantResponse> findById(
            @PathVariable Long id) {

        return ResponseEntity.ok(
        		restaurantService.findById(id)
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<RestaurantResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateRestaurantRequest request) {

        return ResponseEntity.ok(
                restaurantService.update(id, request)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Long id) {

        restaurantService.delete(id);

        return ResponseEntity.noContent().build();
    }
}