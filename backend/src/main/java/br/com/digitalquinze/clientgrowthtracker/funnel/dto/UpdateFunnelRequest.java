package br.com.digitalquinze.clientgrowthtracker.funnel.dto;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public record UpdateFunnelRequest(

    @NotNull
    List<@Valid FunnelStageValueRequest> stages

) {
}