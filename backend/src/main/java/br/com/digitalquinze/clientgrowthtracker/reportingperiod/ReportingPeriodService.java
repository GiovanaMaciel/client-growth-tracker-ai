package br.com.digitalquinze.clientgrowthtracker.reportingperiod;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.digitalquinze.clientgrowthtracker.reportingperiod.dto.CreateReportingPeriodRequest;
import br.com.digitalquinze.clientgrowthtracker.reportingperiod.dto.ReportingPeriodResponse;
import br.com.digitalquinze.clientgrowthtracker.reportingperiod.dto.UpdateReportingPeriodRequest;
import br.com.digitalquinze.clientgrowthtracker.restaurant.Restaurant;
import br.com.digitalquinze.clientgrowthtracker.restaurant.RestaurantNotFoundException;
import br.com.digitalquinze.clientgrowthtracker.restaurant.RestaurantRepository;
import br.com.digitalquinze.clientgrowthtracker.security.TenantAccessService;

@Service
public class ReportingPeriodService {

    private final ReportingPeriodRepository reportingPeriodRepository;
    private final RestaurantRepository restaurantRepository;
    private final TenantAccessService tenantAccessService;

    public ReportingPeriodService(
            ReportingPeriodRepository reportingPeriodRepository,
            RestaurantRepository restaurantRepository,
            TenantAccessService tenantAccessService) {

        this.reportingPeriodRepository =
                reportingPeriodRepository;

        this.restaurantRepository =
                restaurantRepository;

        this.tenantAccessService =
                tenantAccessService;
    }

    @Transactional
    public ReportingPeriodResponse create(
            Long restaurantId,
            CreateReportingPeriodRequest request) {

        tenantAccessService
                .validateRestaurantAccess(restaurantId);

        validateReferenceMonth(
                request.referenceMonth()
        );

        Restaurant restaurant =
                restaurantRepository
                    .findById(restaurantId)
                    .orElseThrow(
                        () -> new RestaurantNotFoundException(
                            restaurantId
                        )
                    );

        boolean exists =
                reportingPeriodRepository
                    .existsByRestaurant_IdAndReferenceMonth(
                        restaurantId,
                        request.referenceMonth()
                    );

        if (exists) {
            throw new ReportingPeriodAlreadyExistsException(
                restaurantId,
                request.referenceMonth()
            );
        }

        ReportingPeriod period =
                new ReportingPeriod(
                    restaurant,
                    request.referenceMonth(),
                    request.openDays(),
                    request.notes()
                );

        ReportingPeriod saved =
                reportingPeriodRepository.save(period);

        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<ReportingPeriodResponse> findAll(
            Long restaurantId) {

        tenantAccessService
                .validateRestaurantAccess(restaurantId);

        return reportingPeriodRepository
                .findAllByRestaurant_IdOrderByReferenceMonthDesc(
                    restaurantId
                )
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public ReportingPeriodResponse findById(
            Long restaurantId,
            Long periodId) {

        tenantAccessService
                .validateRestaurantAccess(restaurantId);

        ReportingPeriod period =
                findPeriod(
                    restaurantId,
                    periodId
                );

        return toResponse(period);
    }

    @Transactional
    public ReportingPeriodResponse update(
            Long restaurantId,
            Long periodId,
            UpdateReportingPeriodRequest request) {

        tenantAccessService
                .validateRestaurantAccess(restaurantId);

        validateReferenceMonth(
                request.referenceMonth()
        );

        ReportingPeriod period =
                findPeriod(
                    restaurantId,
                    periodId
                );

        if (!period
                .getReferenceMonth()
                .equals(request.referenceMonth())) {

            boolean exists =
                    reportingPeriodRepository
                        .existsByRestaurant_IdAndReferenceMonth(
                            restaurantId,
                            request.referenceMonth()
                        );

            if (exists) {
                throw new ReportingPeriodAlreadyExistsException(
                    restaurantId,
                    request.referenceMonth()
                );
            }
        }

        period.setReferenceMonth(
            request.referenceMonth()
        );

        period.setOpenDays(
            request.openDays()
        );

        period.setNotes(
            request.notes()
        );

        ReportingPeriod updated =
                reportingPeriodRepository.save(period);

        return toResponse(updated);
    }

    @Transactional
    public void delete(
            Long restaurantId,
            Long periodId) {

        tenantAccessService
                .validateRestaurantAccess(restaurantId);

        ReportingPeriod period =
                findPeriod(
                    restaurantId,
                    periodId
                );

        reportingPeriodRepository.delete(period);
    }

    private ReportingPeriod findPeriod(
            Long restaurantId,
            Long periodId) {

        return reportingPeriodRepository
                .findByIdAndRestaurant_Id(
                    periodId,
                    restaurantId
                )
                .orElseThrow(
                    () -> new ReportingPeriodNotFoundException(
                        periodId
                    )
                );
    }

    private void validateReferenceMonth(
            LocalDate referenceMonth) {

        if (referenceMonth.getDayOfMonth() != 1) {

            throw new IllegalArgumentException(
                "Reference month must use the first day of the month"
            );
        }
    }

    private ReportingPeriodResponse toResponse(
            ReportingPeriod period) {

        return new ReportingPeriodResponse(
            period.getId(),
            period.getRestaurant().getId(),
            period.getReferenceMonth(),
            period.getOpenDays(),
            period.getNotes(),
            period.getCreatedAt(),
            period.getUpdatedAt()
        );
    }
}