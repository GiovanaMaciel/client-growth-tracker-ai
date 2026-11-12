package br.com.digitalquinze.clientgrowthtracker.funnel;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.digitalquinze.clientgrowthtracker.funnel.dto.FunnelResponse;
import br.com.digitalquinze.clientgrowthtracker.funnel.dto.UpdateFunnelRequest;
import jakarta.validation.Valid;

@RestController
@RequestMapping(
    "/api/restaurants/{restaurantId}/periods/{periodId}/channel-metrics/{metricId}/funnel"
)
public class FunnelController {

    private final FunnelService funnelService;

    public FunnelController(
            FunnelService funnelService) {

        this.funnelService =
                funnelService;
    }

    @GetMapping
    public ResponseEntity<FunnelResponse> getFunnel(
            @PathVariable Long restaurantId,
            @PathVariable Long periodId,
            @PathVariable Long metricId) {

        return ResponseEntity.ok(
            funnelService.getFunnel(
                restaurantId,
                periodId,
                metricId
            )
        );
    }

    @PutMapping
    public ResponseEntity<FunnelResponse> updateFunnel(
            @PathVariable Long restaurantId,
            @PathVariable Long periodId,
            @PathVariable Long metricId,
            @Valid
            @RequestBody UpdateFunnelRequest request) {

        return ResponseEntity.ok(
            funnelService.updateFunnel(
                restaurantId,
                periodId,
                metricId,
                request
            )
        );
    }
}