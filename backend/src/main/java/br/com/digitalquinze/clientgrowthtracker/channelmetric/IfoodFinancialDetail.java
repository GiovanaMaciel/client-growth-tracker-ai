package br.com.digitalquinze.clientgrowthtracker.channelmetric;

import java.math.BigDecimal;

import br.com.digitalquinze.clientgrowthtracker.channelmetric.dto.IfoodFinancialDetailsRequest;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "ifood_financial_details")
public class IfoodFinancialDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "channel_metric_id", nullable = false, unique = true)
    private ChannelMetric channelMetric;

    @Column(name = "services_and_promotions", precision = 12, scale = 2)
    private BigDecimal servicesAndPromotions;

    @Column(precision = 12, scale = 2)
    private BigDecimal adjustments;

    @Column(name = "direct_receipts", precision = 12, scale = 2)
    private BigDecimal directReceipts;

    @Column(name = "marketplace_transfers", precision = 12, scale = 2)
    private BigDecimal marketplaceTransfers;

    protected IfoodFinancialDetail() {
    }

    public IfoodFinancialDetail(ChannelMetric channelMetric) {
        this.channelMetric = channelMetric;
    }

    public void update(IfoodFinancialDetailsRequest request) {
        servicesAndPromotions = request.servicesAndPromotions();
        adjustments = request.adjustments();
        directReceipts = request.directReceipts();
        marketplaceTransfers = request.marketplaceTransfers();
    }

    public BigDecimal getServicesAndPromotions() {
        return servicesAndPromotions;
    }

    public BigDecimal getAdjustments() {
        return adjustments;
    }

    public BigDecimal getDirectReceipts() {
        return directReceipts;
    }

    public BigDecimal getMarketplaceTransfers() {
        return marketplaceTransfers;
    }
}
