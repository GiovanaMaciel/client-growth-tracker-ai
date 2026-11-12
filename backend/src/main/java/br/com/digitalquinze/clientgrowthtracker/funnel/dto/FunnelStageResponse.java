package br.com.digitalquinze.clientgrowthtracker.funnel.dto;

import java.math.BigDecimal;

import br.com.digitalquinze.clientgrowthtracker.funnel.FunnelValueSource;

public record FunnelStageResponse(

    Long stageId,

    String stageKey,
    String stageName,

    Short stageOrder,

    FunnelValueSource valueSource,

    Integer value,

    BigDecimal conversionFromVisits

) {
}