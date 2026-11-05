package br.com.digitalquinze.clientgrowthtracker.reportingperiod;

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

import br.com.digitalquinze.clientgrowthtracker.reportingperiod.dto.CreateReportingPeriodRequest;
import br.com.digitalquinze.clientgrowthtracker.reportingperiod.dto.ReportingPeriodResponse;
import br.com.digitalquinze.clientgrowthtracker.reportingperiod.dto.UpdateReportingPeriodRequest;
import jakarta.validation.Valid;

@RestController
@RequestMapping(
    "/api/restaurants/{restaurantId}/periods"
)
public class ReportingPeriodController {

    private final ReportingPeriodService reportingPeriodService;

    public ReportingPeriodController(
            ReportingPeriodService reportingPeriodService) {

        this.reportingPeriodService =
                reportingPeriodService;
    }

    @PostMapping
    public ResponseEntity<ReportingPeriodResponse> create(
            @PathVariable Long restaurantId,
            @Valid
            @RequestBody CreateReportingPeriodRequest request) {

        ReportingPeriodResponse period =
                reportingPeriodService.create(
                    restaurantId,
                    request
                );

        URI location =
                ServletUriComponentsBuilder
                    .fromCurrentRequest()
                    .path("/{id}")
                    .buildAndExpand(period.id())
                    .toUri();

        return ResponseEntity
                .created(location)
                .body(period);
    }

    @GetMapping
    public ResponseEntity<List<ReportingPeriodResponse>>
            findAll(
                @PathVariable Long restaurantId) {

        return ResponseEntity.ok(
            reportingPeriodService.findAll(
                restaurantId
            )
        );
    }

    @GetMapping("/{periodId}")
    public ResponseEntity<ReportingPeriodResponse>
            findById(
                @PathVariable Long restaurantId,
                @PathVariable Long periodId) {

        return ResponseEntity.ok(
            reportingPeriodService.findById(
                restaurantId,
                periodId
            )
        );
    }

    @PutMapping("/{periodId}")
    public ResponseEntity<ReportingPeriodResponse>
            update(
                @PathVariable Long restaurantId,
                @PathVariable Long periodId,
                @Valid
                @RequestBody UpdateReportingPeriodRequest request) {

        return ResponseEntity.ok(
            reportingPeriodService.update(
                restaurantId,
                periodId,
                request
            )
        );
    }

    @DeleteMapping("/{periodId}")
    public ResponseEntity<Void> delete(
            @PathVariable Long restaurantId,
            @PathVariable Long periodId) {

        reportingPeriodService.delete(
            restaurantId,
            periodId
        );

        return ResponseEntity
                .noContent()
                .build();
    }
}