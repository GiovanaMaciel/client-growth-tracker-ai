package br.com.digitalquinze.clientgrowthtracker.channelmetric.dto;

import java.math.BigDecimal;
import jakarta.validation.constraints.DecimalMin;

public record IfoodFinancialDetailsRequest(
        @DecimalMin("0.00") BigDecimal servicesAndPromotions,
        BigDecimal adjustments,
        @DecimalMin("0.00") BigDecimal directReceipts,
        @DecimalMin("0.00") BigDecimal marketplaceTransfers
) {
}