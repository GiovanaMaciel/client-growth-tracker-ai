package br.com.digitalquinze.clientgrowthtracker.paidtraffic.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record UpdatePaidTrafficMetricRequest(

    @NotNull
    @DecimalMin("0.00")
    BigDecimal investment,

    @NotNull
    @Min(0)
    Integer reach,

    @NotNull
    @Min(0)
    Integer impressions,

    @NotNull
    @Min(0)
    Integer visits,

    @NotNull
    @Min(0)
    Integer carts,

    @NotNull
    @Min(0)
    Integer purchases,

    @NotNull
    @DecimalMin("0.00")
    BigDecimal attributedRevenue

) {
}