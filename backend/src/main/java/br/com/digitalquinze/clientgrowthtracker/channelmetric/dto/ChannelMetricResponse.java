package br.com.digitalquinze.clientgrowthtracker.channelmetric.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import br.com.digitalquinze.clientgrowthtracker.channelmetric.ChannelMetricSource;

public record ChannelMetricResponse(

    Long id,
    Long periodId,

    Long restaurantChannelId,
    Long channelId,
    String channelName,

    BigDecimal grossRevenue,
    Integer orders,
    BigDecimal netRevenue,
    BigDecimal fees,
    BigDecimal promotions,

    ChannelMetricSource source,

    BigDecimal averageTicket,
    BigDecimal receivedPercentage,
    BigDecimal discountValue,
    BigDecimal discountPercentage,

    OffsetDateTime createdAt,
    OffsetDateTime updatedAt

) {
}