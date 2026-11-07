package br.com.digitalquinze.clientgrowthtracker.channelmetric;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import br.com.digitalquinze.clientgrowthtracker.reportingperiod.ReportingPeriod;
import br.com.digitalquinze.clientgrowthtracker.restaurantchannel.RestaurantChannel;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

@Entity
@Table(name = "channel_metrics")
public class ChannelMetric {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "period_id", nullable = false)
    private ReportingPeriod period;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "restaurant_channel_id",
        nullable = false
    )
    private RestaurantChannel restaurantChannel;

    @Column(
        name = "gross_revenue",
        nullable = false,
        precision = 12,
        scale = 2
    )
    private BigDecimal grossRevenue;

    @Column(nullable = false)
    private Integer orders;

    @Column(
        name = "net_revenue",
        nullable = false,
        precision = 12,
        scale = 2
    )
    private BigDecimal netRevenue;

    @Column(precision = 12, scale = 2)
    private BigDecimal fees;

    @Column(precision = 12, scale = 2)
    private BigDecimal promotions;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ChannelMetricSource source;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    public ChannelMetric() {
    }

    public ChannelMetric(
            ReportingPeriod period,
            RestaurantChannel restaurantChannel,
            BigDecimal grossRevenue,
            Integer orders,
            BigDecimal netRevenue,
            BigDecimal fees,
            BigDecimal promotions) {

        this.period = period;
        this.restaurantChannel = restaurantChannel;
        this.grossRevenue = grossRevenue;
        this.orders = orders;
        this.netRevenue = netRevenue;
        this.fees = fees;
        this.promotions = promotions;

        this.source = ChannelMetricSource.MANUAL;
    }

    @PrePersist
    public void prePersist() {
        OffsetDateTime now = OffsetDateTime.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = OffsetDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public ReportingPeriod getPeriod() {
        return period;
    }

    public RestaurantChannel getRestaurantChannel() {
        return restaurantChannel;
    }

    public BigDecimal getGrossRevenue() {
        return grossRevenue;
    }

    public Integer getOrders() {
        return orders;
    }

    public BigDecimal getNetRevenue() {
        return netRevenue;
    }

    public BigDecimal getFees() {
        return fees;
    }

    public BigDecimal getPromotions() {
        return promotions;
    }

    public ChannelMetricSource getSource() {
        return source;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setGrossRevenue(BigDecimal grossRevenue) {
        this.grossRevenue = grossRevenue;
    }

    public void setOrders(Integer orders) {
        this.orders = orders;
    }

    public void setNetRevenue(BigDecimal netRevenue) {
        this.netRevenue = netRevenue;
    }

    public void setFees(BigDecimal fees) {
        this.fees = fees;
    }

    public void setPromotions(BigDecimal promotions) {
        this.promotions = promotions;
    }
}