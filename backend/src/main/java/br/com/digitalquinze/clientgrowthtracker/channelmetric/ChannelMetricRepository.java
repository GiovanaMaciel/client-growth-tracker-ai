package br.com.digitalquinze.clientgrowthtracker.channelmetric;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ChannelMetricRepository
        extends JpaRepository<ChannelMetric, Long> {

    List<ChannelMetric>
        findAllByPeriod_IdOrderByRestaurantChannel_Channel_NameAsc(
            Long periodId
        );

    Optional<ChannelMetric>
        findByIdAndPeriod_Id(
            Long id,
            Long periodId
        );

    boolean existsByPeriod_IdAndRestaurantChannel_Id(
        Long periodId,
        Long restaurantChannelId
    );
}