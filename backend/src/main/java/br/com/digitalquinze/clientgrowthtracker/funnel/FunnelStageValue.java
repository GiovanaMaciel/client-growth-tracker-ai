package br.com.digitalquinze.clientgrowthtracker.funnel;

import br.com.digitalquinze.clientgrowthtracker.channelmetric.ChannelMetric;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "funnel_stage_values")
public class FunnelStageValue {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "channel_metric_id", nullable = false)
    private ChannelMetric channelMetric;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "funnel_stage_id", nullable = false)
    private FunnelStageDefinition funnelStage;

    @Column(nullable = false)
    private Integer value;

    public FunnelStageValue() {
    }

    public FunnelStageValue(
            ChannelMetric channelMetric,
            FunnelStageDefinition funnelStage,
            Integer value) {

        this.channelMetric = channelMetric;
        this.funnelStage = funnelStage;
        this.value = value;
    }

    public Long getId() {
        return id;
    }

    public ChannelMetric getChannelMetric() {
        return channelMetric;
    }

    public FunnelStageDefinition getFunnelStage() {
        return funnelStage;
    }

    public Integer getValue() {
        return value;
    }

    public void setValue(Integer value) {
        this.value = value;
    }
}