package br.com.digitalquinze.clientgrowthtracker.analytics.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record PeriodSummaryResponse(

    Long periodId,
    Long restaurantId,

    LocalDate referenceMonth,
    Short openDays,

    Integer channelCount,

    BigDecimal totalGrossRevenue,
    Integer totalOrders,

    BigDecimal averageOrdersPerDay,
    BigDecimal consolidatedAverageTicket,

    BigDecimal totalNetRevenue,
    BigDecimal receivedPercentage,

    BigDecimal totalDiscountValue,
    BigDecimal discountPercentage,

    List<ChannelPerformanceResponse> channels

) {
}