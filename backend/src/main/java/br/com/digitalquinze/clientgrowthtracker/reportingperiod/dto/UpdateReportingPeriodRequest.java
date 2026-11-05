package br.com.digitalquinze.clientgrowthtracker.reportingperiod.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record UpdateReportingPeriodRequest(

    @NotNull(message = "Reference month is required")
    LocalDate referenceMonth,

    @NotNull(message = "Open days is required")
    @Min(value = 0, message = "Open days cannot be negative")
    @Max(value = 31, message = "Open days cannot exceed 31")
    Short openDays,

    String notes

) {
}