package br.com.digitalquinze.clientgrowthtracker.reportingperiod.dto;

import java.time.LocalDate;
import java.time.OffsetDateTime;

public record ReportingPeriodResponse(

    Long id,
    Long restaurantId,
    LocalDate referenceMonth,
    Short openDays,
    String notes,
    OffsetDateTime createdAt,
    OffsetDateTime updatedAt

) {
}