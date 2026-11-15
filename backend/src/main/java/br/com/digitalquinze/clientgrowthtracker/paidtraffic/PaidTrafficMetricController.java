package br.com.digitalquinze.clientgrowthtracker.paidtraffic;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.digitalquinze.clientgrowthtracker.paidtraffic.dto.PaidTrafficMetricResponse;
import br.com.digitalquinze.clientgrowthtracker.paidtraffic.dto.UpdatePaidTrafficMetricRequest;
import jakarta.validation.Valid;

@RestController
@RequestMapping(
    "/api/restaurants/{restaurantId}/periods/{periodId}/paid-traffic"
)
public class PaidTrafficMetricController {

    private final PaidTrafficMetricService service;

    public PaidTrafficMetricController(
            PaidTrafficMetricService service) {

        this.service = service;
    }

    @GetMapping
    public ResponseEntity<PaidTrafficMetricResponse> find(
            @PathVariable Long restaurantId,
            @PathVariable Long periodId) {

        return ResponseEntity.ok(
            service.find(
                restaurantId,
                periodId
            )
        );
    }

    @PutMapping
    public ResponseEntity<PaidTrafficMetricResponse> save(
            @PathVariable Long restaurantId,
            @PathVariable Long periodId,
            @Valid
            @RequestBody UpdatePaidTrafficMetricRequest request) {

        return ResponseEntity.ok(
            service.save(
                restaurantId,
                periodId,
                request
            )
        );
    }

    @DeleteMapping
    public ResponseEntity<Void> delete(
            @PathVariable Long restaurantId,
            @PathVariable Long periodId) {

        service.delete(
            restaurantId,
            periodId
        );

        return ResponseEntity
                .noContent()
                .build();
    }
}