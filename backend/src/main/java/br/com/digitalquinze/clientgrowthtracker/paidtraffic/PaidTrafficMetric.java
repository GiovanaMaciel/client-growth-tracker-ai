package br.com.digitalquinze.clientgrowthtracker.paidtraffic;

import java.math.BigDecimal;

import br.com.digitalquinze.clientgrowthtracker.reportingperiod.ReportingPeriod;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "paid_traffic_metrics")
public class PaidTrafficMetric {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "period_id",
        nullable = false,
        unique = true
    )
    private ReportingPeriod period;

    @Column(
        nullable = false,
        precision = 12,
        scale = 2
    )
    private BigDecimal investment;

    @Column(nullable = false)
    private Integer reach;

    @Column(nullable = false)
    private Integer impressions;

    @Column(nullable = false)
    private Integer visits;

    @Column(nullable = false)
    private Integer carts;

    @Column(nullable = false)
    private Integer purchases;

    @Column(
        name = "attributed_revenue",
        nullable = false,
        precision = 12,
        scale = 2
    )
    private BigDecimal attributedRevenue;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PaidTrafficSource source;

    public PaidTrafficMetric() {
    }

    public PaidTrafficMetric(
            ReportingPeriod period,
            BigDecimal investment,
            Integer reach,
            Integer impressions,
            Integer visits,
            Integer carts,
            Integer purchases,
            BigDecimal attributedRevenue) {

        this.period = period;
        this.investment = investment;
        this.reach = reach;
        this.impressions = impressions;
        this.visits = visits;
        this.carts = carts;
        this.purchases = purchases;
        this.attributedRevenue = attributedRevenue;
        this.source = PaidTrafficSource.MANUAL;
    }

    public Long getId() {
        return id;
    }

    public ReportingPeriod getPeriod() {
        return period;
    }

    public BigDecimal getInvestment() {
        return investment;
    }

    public Integer getReach() {
        return reach;
    }

    public Integer getImpressions() {
        return impressions;
    }

    public Integer getVisits() {
        return visits;
    }

    public Integer getCarts() {
        return carts;
    }

    public Integer getPurchases() {
        return purchases;
    }

    public BigDecimal getAttributedRevenue() {
        return attributedRevenue;
    }

    public PaidTrafficSource getSource() {
        return source;
    }

    public void setInvestment(BigDecimal investment) {
        this.investment = investment;
    }

    public void setReach(Integer reach) {
        this.reach = reach;
    }

    public void setImpressions(Integer impressions) {
        this.impressions = impressions;
    }

    public void setVisits(Integer visits) {
        this.visits = visits;
    }

    public void setCarts(Integer carts) {
        this.carts = carts;
    }

    public void setPurchases(Integer purchases) {
        this.purchases = purchases;
    }

    public void setAttributedRevenue(
            BigDecimal attributedRevenue) {

        this.attributedRevenue =
                attributedRevenue;
    }
}