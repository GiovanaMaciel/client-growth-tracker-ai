package br.com.digitalquinze.clientgrowthtracker.analytics.dto;

import java.math.BigDecimal;

public record ChannelPerformanceResponse(

    Long restaurantChannelId,
    Long channelId,
    String channelName,

    BigDecimal grossRevenue,
    Integer orders,
    BigDecimal averageTicket,

    BigDecimal netRevenue,
    BigDecimal receivedPercentage,

    BigDecimal discountValue,
    BigDecimal discountPercentage,

    BigDecimal salesSharePercentage,
    BigDecimal ordersSharePercentage

) {
}