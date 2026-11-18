package br.com.digitalquinze.clientgrowthtracker.channelmetric;

import java.math.BigDecimal;
import java.math.RoundingMode;

import br.com.digitalquinze.clientgrowthtracker.channelmetric.dto.IfoodFinancialDetailsRequest;

public final class IfoodFinancialCalculator {

    private IfoodFinancialCalculator() {
    }

    public record Result(
            BigDecimal netRevenue,
            BigDecimal marketplaceTransfers
    ) {
    }

    public static Result calculate(
            BigDecimal grossRevenue,
            BigDecimal fees,
            IfoodFinancialDetailsRequest details) {

        if (details == null) {
            throw new IllegalArgumentException(
                    "iFood financial details are required"
            );
        }

        BigDecimal services = details.servicesAndPromotions();
        BigDecimal adjustments = details.adjustments();
        BigDecimal directReceipts = details.directReceipts();

        requireNonNegative("Gross revenue", grossRevenue);
        requireNonNegative("Fees", fees);
        requireNonNegative("Services and promotions", services);
        requireNonNegative("Direct receipts", directReceipts);

        if (adjustments == null) {
            throw new IllegalArgumentException(
                    "Adjustments are required"
            );
        }

        BigDecimal netRevenue = grossRevenue
                .subtract(fees)
                .subtract(services)
                .add(adjustments)
                .setScale(2, RoundingMode.HALF_UP);

        if (netRevenue.signum() < 0) {
            throw new IllegalArgumentException(
                    "Calculated net revenue cannot be negative"
            );
        }

        BigDecimal transfers = netRevenue
                .subtract(directReceipts)
                .setScale(2, RoundingMode.HALF_UP);

        if (transfers.signum() < 0) {
            throw new IllegalArgumentException(
                    "Direct receipts cannot exceed net revenue"
            );
        }

        return new Result(netRevenue, transfers);
    }

    public static IfoodFinancialDetailsRequest withCalculatedTransfers(
            IfoodFinancialDetailsRequest original,
            Result calculated) {

        return new IfoodFinancialDetailsRequest(
                original.servicesAndPromotions(),
                original.adjustments(),
                original.directReceipts(),
                calculated.marketplaceTransfers()
        );
    }

    private static void requireNonNegative(
            String field,
            BigDecimal value) {

        if (value == null || value.signum() < 0) {
            throw new IllegalArgumentException(
                    field + " must be provided and non-negative"
            );
        }
    }
}
