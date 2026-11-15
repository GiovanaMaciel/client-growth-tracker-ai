package br.com.digitalquinze.clientgrowthtracker.paidtraffic.dto;

import java.math.BigDecimal;

import br.com.digitalquinze.clientgrowthtracker.paidtraffic.PaidTrafficSource;

public record PaidTrafficMetricResponse(

    Long id,
    Long periodId,

    BigDecimal investment,

    Integer reach,
    Integer impressions,
    Integer visits,
    Integer carts,
    Integer purchases,

    BigDecimal attributedRevenue,

    PaidTrafficSource source,

    BigDecimal roas,
    BigDecimal cpa,
    BigDecimal costPerVisit,
    BigDecimal averageTicket,
    BigDecimal frequency,

    BigDecimal visitToCartConversion,
    BigDecimal visitToPurchaseConversion,
    BigDecimal cartToPurchaseConversion

) {
}