package br.com.digitalquinze.clientgrowthtracker.funnel.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record FunnelStageValueRequest(

    @NotNull
    Long stageId,

    @NotNull
    @Min(0)
    Integer value

) {
}