package br.com.digitalquinze.clientgrowthtracker.funnel;

import br.com.digitalquinze.clientgrowthtracker.channel.Channel;
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
import jakarta.persistence.Table;

@Entity
@Table(name = "funnel_stage_definitions")
public class FunnelStageDefinition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "channel_id", nullable = false)
    private Channel channel;

    @Column(name = "stage_key", nullable = false, length = 50)
    private String stageKey;

    @Column(name = "stage_name", nullable = false, length = 100)
    private String stageName;

    @Column(name = "stage_order", nullable = false)
    private Short stageOrder;

    @Enumerated(EnumType.STRING)
    @Column(name = "value_source", nullable = false, length = 20)
    private FunnelValueSource valueSource;

    public FunnelStageDefinition() {
    }

    public Long getId() {
        return id;
    }

    public Channel getChannel() {
        return channel;
    }

    public String getStageKey() {
        return stageKey;
    }

    public String getStageName() {
        return stageName;
    }

    public Short getStageOrder() {
        return stageOrder;
    }

    public FunnelValueSource getValueSource() {
        return valueSource;
    }
}