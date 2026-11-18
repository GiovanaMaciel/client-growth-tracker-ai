package br.com.digitalquinze.clientgrowthtracker.analytics.dto;

import java.math.BigDecimal;
import br.com.digitalquinze.clientgrowthtracker.channelmetric.dto.IfoodFinancialDetailsResponse;

public record ChannelPerformanceResponse(

    Long restaurantChannelId,
    Long channelId,
    String channelName,

    BigDecimal grossRevenue,
    Integer orders,
    BigDecimal averageTicket,

    BigDecimal netRevenue,
    BigDecimal receivedPercentage,
    BigDecimal fees,

    BigDecimal discountValue,
    BigDecimal discountPercentage,

    BigDecimal salesSharePercentage,
    BigDecimal ordersSharePercentage,
    IfoodFinancialDetailsResponse ifoodDetails

) {
}
