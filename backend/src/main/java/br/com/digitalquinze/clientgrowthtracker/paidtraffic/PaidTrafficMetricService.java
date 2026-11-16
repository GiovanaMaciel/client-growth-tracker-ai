package br.com.digitalquinze.clientgrowthtracker.paidtraffic;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.digitalquinze.clientgrowthtracker.paidtraffic.dto.PaidTrafficMetricResponse;
import br.com.digitalquinze.clientgrowthtracker.paidtraffic.dto.UpdatePaidTrafficMetricRequest;
import br.com.digitalquinze.clientgrowthtracker.reportingperiod.ReportingPeriod;
import br.com.digitalquinze.clientgrowthtracker.reportingperiod.ReportingPeriodNotFoundException;
import br.com.digitalquinze.clientgrowthtracker.reportingperiod.ReportingPeriodRepository;
import br.com.digitalquinze.clientgrowthtracker.security.TenantAccessService;
import java.util.Optional;

@Service
public class PaidTrafficMetricService {

    private static final BigDecimal ONE_HUNDRED =
            new BigDecimal("100");

    private final PaidTrafficMetricRepository
            paidTrafficMetricRepository;

    private final ReportingPeriodRepository
            reportingPeriodRepository;

    private final TenantAccessService
            tenantAccessService;

    public PaidTrafficMetricService(
            PaidTrafficMetricRepository paidTrafficMetricRepository,
            ReportingPeriodRepository reportingPeriodRepository,
            TenantAccessService tenantAccessService) {

        this.paidTrafficMetricRepository =
                paidTrafficMetricRepository;

        this.reportingPeriodRepository =
                reportingPeriodRepository;

        this.tenantAccessService =
                tenantAccessService;
    }
    

	@Transactional(readOnly = true)
	@PreAuthorize("isAuthenticated()")
	public Optional<PaidTrafficMetricResponse> findOptional(Long restaurantId, Long periodId) {

		validateAndFindPeriod(restaurantId, periodId);

		return paidTrafficMetricRepository.findByPeriod_Id(periodId).map(this::toResponse);
	}


    @Transactional(readOnly = true)
    @PreAuthorize("isAuthenticated()")
    public PaidTrafficMetricResponse find(
            Long restaurantId,
            Long periodId) {

        validateAndFindPeriod(
            restaurantId,
            periodId
        );

        PaidTrafficMetric metric =
                paidTrafficMetricRepository
                    .findByPeriod_Id(periodId)
                    .orElseThrow(
                        () ->
                            new PaidTrafficMetricNotFoundException(
                                periodId
                            )
                    );

        return toResponse(metric);
    }

    @Transactional
    @PreAuthorize("isAuthenticated()")
    public PaidTrafficMetricResponse save(
            Long restaurantId,
            Long periodId,
            UpdatePaidTrafficMetricRequest request) {

        ReportingPeriod period =
                validateAndFindPeriod(
                    restaurantId,
                    periodId
                );

        PaidTrafficMetric metric =
                paidTrafficMetricRepository
                    .findByPeriod_Id(periodId)
                    .orElseGet(
                        () -> new PaidTrafficMetric(
                            period,
                            request.investment(),
                            request.reach(),
                            request.impressions(),
                            request.visits(),
                            request.carts(),
                            request.purchases(),
                            request.attributedRevenue()
                        )
                    );

        metric.setInvestment(
            request.investment()
        );

        metric.setReach(
            request.reach()
        );

        metric.setImpressions(
            request.impressions()
        );

        metric.setVisits(
            request.visits()
        );

        metric.setCarts(
            request.carts()
        );

        metric.setPurchases(
            request.purchases()
        );

        metric.setAttributedRevenue(
            request.attributedRevenue()
        );

        PaidTrafficMetric saved =
                paidTrafficMetricRepository
                    .save(metric);

        return toResponse(saved);
    }

    @Transactional
    @PreAuthorize("isAuthenticated()")
    public void delete(
            Long restaurantId,
            Long periodId) {

        validateAndFindPeriod(
            restaurantId,
            periodId
        );

        PaidTrafficMetric metric =
                paidTrafficMetricRepository
                    .findByPeriod_Id(periodId)
                    .orElseThrow(
                        () ->
                            new PaidTrafficMetricNotFoundException(
                                periodId
                            )
                    );

        paidTrafficMetricRepository
                .delete(metric);
    }

    private ReportingPeriod validateAndFindPeriod(
            Long restaurantId,
            Long periodId) {

        tenantAccessService
                .validateRestaurantAccess(
                    restaurantId
                );

        return reportingPeriodRepository
                .findByIdAndRestaurant_Id(
                    periodId,
                    restaurantId
                )
                .orElseThrow(
                    () ->
                        new ReportingPeriodNotFoundException(
                            periodId
                        )
                );
    }

    private PaidTrafficMetricResponse toResponse(
            PaidTrafficMetric metric) {

        BigDecimal investment =
                metric.getInvestment();

        BigDecimal revenue =
                metric.getAttributedRevenue();

        return new PaidTrafficMetricResponse(

            metric.getId(),

            metric.getPeriod().getId(),

            money(investment),

            metric.getReach(),
            metric.getImpressions(),
            metric.getVisits(),
            metric.getCarts(),
            metric.getPurchases(),

            money(revenue),

            metric.getSource(),

            divide(
                revenue,
                investment
            ),

            moneyDivide(
                investment,
                metric.getPurchases()
            ),

            moneyDivide(
                investment,
                metric.getVisits()
            ),

            moneyDivide(
                revenue,
                metric.getPurchases()
            ),

            divide(
                BigDecimal.valueOf(
                    metric.getImpressions()
                ),
                BigDecimal.valueOf(
                    metric.getReach()
                )
            ),

            percentage(
                metric.getCarts(),
                metric.getVisits()
            ),

            percentage(
                metric.getPurchases(),
                metric.getVisits()
            ),

            percentage(
                metric.getPurchases(),
                metric.getCarts()
            )
        );
    }

    private BigDecimal percentage(
            int value,
            int total) {

        if (total == 0) {
            return zero();
        }

        return BigDecimal
                .valueOf(value)
                .multiply(ONE_HUNDRED)
                .divide(
                    BigDecimal.valueOf(total),
                    2,
                    RoundingMode.HALF_UP
                );
    }

    private BigDecimal divide(
            BigDecimal value,
            BigDecimal divisor) {

        if (divisor.compareTo(
                BigDecimal.ZERO) == 0) {

            return zero();
        }

        return value.divide(
            divisor,
            2,
            RoundingMode.HALF_UP
        );
    }

    private BigDecimal moneyDivide(
            BigDecimal value,
            int divisor) {

        if (divisor == 0) {
            return zero();
        }

        return value.divide(
            BigDecimal.valueOf(divisor),
            2,
            RoundingMode.HALF_UP
        );
    }

    private BigDecimal money(
            BigDecimal value) {

        return value.setScale(
            2,
            RoundingMode.HALF_UP
        );
    }

    private BigDecimal zero() {

        return BigDecimal.ZERO
                .setScale(2);
    }
}