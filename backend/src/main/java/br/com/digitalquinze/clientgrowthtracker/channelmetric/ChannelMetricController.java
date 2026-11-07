package br.com.digitalquinze.clientgrowthtracker.channelmetric;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.digitalquinze.clientgrowthtracker.channelmetric.dto.ChannelMetricResponse;
import br.com.digitalquinze.clientgrowthtracker.channelmetric.dto.CreateChannelMetricRequest;
import br.com.digitalquinze.clientgrowthtracker.channelmetric.dto.UpdateChannelMetricRequest;
import jakarta.validation.Valid;

@RestController
@RequestMapping(
    "/api/restaurants/{restaurantId}/periods/{periodId}/channel-metrics"
)
public class ChannelMetricController {

    private final ChannelMetricService service;

    public ChannelMetricController(
            ChannelMetricService service) {

        this.service = service;
    }

    @PostMapping
    public ResponseEntity<ChannelMetricResponse> create(
            @PathVariable Long restaurantId,
            @PathVariable Long periodId,
            @Valid
            @RequestBody CreateChannelMetricRequest request) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                    service.create(
                        restaurantId,
                        periodId,
                        request
                    )
                );
    }

    @GetMapping
    public ResponseEntity<List<ChannelMetricResponse>>
            findAll(
                @PathVariable Long restaurantId,
                @PathVariable Long periodId) {

        return ResponseEntity.ok(
            service.findAll(
                restaurantId,
                periodId
            )
        );
    }

    @GetMapping("/{metricId}")
    public ResponseEntity<ChannelMetricResponse> findById(
            @PathVariable Long restaurantId,
            @PathVariable Long periodId,
            @PathVariable Long metricId) {

        return ResponseEntity.ok(
            service.findById(
                restaurantId,
                periodId,
                metricId
            )
        );
    }

    @PutMapping("/{metricId}")
    public ResponseEntity<ChannelMetricResponse> update(
            @PathVariable Long restaurantId,
            @PathVariable Long periodId,
            @PathVariable Long metricId,
            @Valid
            @RequestBody UpdateChannelMetricRequest request) {

        return ResponseEntity.ok(
            service.update(
                restaurantId,
                periodId,
                metricId,
                request
            )
        );
    }

    @DeleteMapping("/{metricId}")
    public ResponseEntity<Void> delete(
            @PathVariable Long restaurantId,
            @PathVariable Long periodId,
            @PathVariable Long metricId) {

        service.delete(
            restaurantId,
            periodId,
            metricId
        );

        return ResponseEntity
                .noContent()
                .build();
    }
}