package br.com.digitalquinze.clientgrowthtracker.analytics;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.digitalquinze.clientgrowthtracker.analytics.dto.DashboardResponse;

@RestController
@RequestMapping(
    "/api/restaurants/{restaurantId}/periods/{periodId}/dashboard"
)
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(
            DashboardService dashboardService) {

        this.dashboardService = dashboardService;
    }

    @GetMapping
    public ResponseEntity<DashboardResponse> getDashboard(
            @PathVariable Long restaurantId,
            @PathVariable Long periodId) {

        return ResponseEntity.ok(
            dashboardService.getDashboard(
                restaurantId,
                periodId
            )
        );
    }
}
