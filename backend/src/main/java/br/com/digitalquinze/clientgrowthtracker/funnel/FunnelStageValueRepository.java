package br.com.digitalquinze.clientgrowthtracker.funnel;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface FunnelStageValueRepository
        extends JpaRepository<FunnelStageValue, Long> {

    List<FunnelStageValue>
        findAllByChannelMetric_Id(
            Long channelMetricId
        );

    Optional<FunnelStageValue>
        findByChannelMetric_IdAndFunnelStage_Id(
            Long channelMetricId,
            Long funnelStageId
        );
}