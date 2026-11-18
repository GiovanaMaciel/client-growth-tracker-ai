package br.com.digitalquinze.clientgrowthtracker.channelmetric;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface IfoodFinancialDetailRepository
        extends JpaRepository<IfoodFinancialDetail, Long> {

    Optional<IfoodFinancialDetail> findByChannelMetric_Id(Long channelMetricId);
}
