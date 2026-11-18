package br.com.digitalquinze.clientgrowthtracker.channelmetric.dto;

import java.math.BigDecimal;

import br.com.digitalquinze.clientgrowthtracker.channelmetric.IfoodFinancialDetail;

public record IfoodFinancialDetailsResponse(
        BigDecimal servicesAndPromotions,
        BigDecimal adjustments,
        BigDecimal directReceipts,
        BigDecimal marketplaceTransfers
) {
    public static IfoodFinancialDetailsResponse from(IfoodFinancialDetail detail) {
        return new IfoodFinancialDetailsResponse(
                detail.getServicesAndPromotions(),
                detail.getAdjustments(),
                detail.getDirectReceipts(),
                detail.getMarketplaceTransfers()
        );
    }
}
