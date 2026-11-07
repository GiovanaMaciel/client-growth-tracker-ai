package br.com.digitalquinze.clientgrowthtracker.channelmetric.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record UpdateChannelMetricRequest(

    @NotNull
    @DecimalMin("0.00")
    BigDecimal grossRevenue,

    @NotNull
    @Min(0)
    Integer orders,

    @NotNull
    @DecimalMin("0.00")
    BigDecimal netRevenue,

    @DecimalMin("0.00")
    BigDecimal fees,

    @DecimalMin("0.00")
    BigDecimal promotions

) {
}