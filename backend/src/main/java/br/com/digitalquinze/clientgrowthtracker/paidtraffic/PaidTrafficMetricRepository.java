package br.com.digitalquinze.clientgrowthtracker.paidtraffic;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PaidTrafficMetricRepository
        extends JpaRepository<PaidTrafficMetric, Long> {

    Optional<PaidTrafficMetric>
        findByPeriod_Id(Long periodId);

}