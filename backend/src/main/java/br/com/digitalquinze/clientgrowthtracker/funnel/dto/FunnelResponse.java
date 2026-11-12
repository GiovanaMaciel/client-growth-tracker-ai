package br.com.digitalquinze.clientgrowthtracker.funnel.dto;

import java.util.List;

public record FunnelResponse(

    Long channelMetricId,

    Long channelId,
    String channelName,

    List<FunnelStageResponse> stages

) {
}