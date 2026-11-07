package br.com.digitalquinze.clientgrowthtracker.analytics;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.digitalquinze.clientgrowthtracker.analytics.dto.PeriodSummaryResponse;

@RestController
@RequestMapping(
    "/api/restaurants/{restaurantId}/periods/{periodId}/summary"
)
public class PeriodSummaryController {

    private final PeriodSummaryService periodSummaryService;

    public PeriodSummaryController(
            PeriodSummaryService periodSummaryService) {

        this.periodSummaryService =
                periodSummaryService;
    }

    @GetMapping
    public ResponseEntity<PeriodSummaryResponse> getSummary(
            @PathVariable Long restaurantId,
            @PathVariable Long periodId) {

        return ResponseEntity.ok(
            periodSummaryService.getSummary(
                restaurantId,
                periodId
            )
        );
    }
}