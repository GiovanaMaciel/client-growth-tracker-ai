package br.com.digitalquinze.clientgrowthtracker.funnel;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface FunnelStageDefinitionRepository
        extends JpaRepository<FunnelStageDefinition, Long> {

    List<FunnelStageDefinition>
        findAllByChannel_IdOrderByStageOrderAsc(
            Long channelId
        );
}