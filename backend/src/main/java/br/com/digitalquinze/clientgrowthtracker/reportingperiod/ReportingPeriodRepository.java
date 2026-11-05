package br.com.digitalquinze.clientgrowthtracker.reportingperiod;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ReportingPeriodRepository
        extends JpaRepository<ReportingPeriod, Long> {

    List<ReportingPeriod>
        findAllByRestaurant_IdOrderByReferenceMonthDesc(
            Long restaurantId
        );

    Optional<ReportingPeriod>
        findByIdAndRestaurant_Id(
            Long id,
            Long restaurantId
        );

    boolean existsByRestaurant_IdAndReferenceMonth(
        Long restaurantId,
        LocalDate referenceMonth
    );
}