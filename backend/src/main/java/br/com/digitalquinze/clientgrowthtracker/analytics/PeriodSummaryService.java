package br.com.digitalquinze.clientgrowthtracker.analytics;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.digitalquinze.clientgrowthtracker.analytics.dto.ChannelPerformanceResponse;
import br.com.digitalquinze.clientgrowthtracker.analytics.dto.PeriodSummaryResponse;
import br.com.digitalquinze.clientgrowthtracker.channelmetric.ChannelMetric;
import br.com.digitalquinze.clientgrowthtracker.channelmetric.ChannelMetricRepository;
import br.com.digitalquinze.clientgrowthtracker.reportingperiod.ReportingPeriod;
import br.com.digitalquinze.clientgrowthtracker.reportingperiod.ReportingPeriodNotFoundException;
import br.com.digitalquinze.clientgrowthtracker.reportingperiod.ReportingPeriodRepository;
import br.com.digitalquinze.clientgrowthtracker.security.TenantAccessService;

@Service
public class PeriodSummaryService {

    private static final BigDecimal ONE_HUNDRED =
            new BigDecimal("100");

    private final ReportingPeriodRepository reportingPeriodRepository;
    private final ChannelMetricRepository channelMetricRepository;
    private final TenantAccessService tenantAccessService;

    public PeriodSummaryService(
            ReportingPeriodRepository reportingPeriodRepository,
            ChannelMetricRepository channelMetricRepository,
            TenantAccessService tenantAccessService) {

        this.reportingPeriodRepository =
                reportingPeriodRepository;

        this.channelMetricRepository =
                channelMetricRepository;

        this.tenantAccessService =
                tenantAccessService;
    }

    @Transactional(readOnly = true)
    @PreAuthorize("isAuthenticated()")
    public PeriodSummaryResponse getSummary(
            Long restaurantId,
            Long periodId) {

    	// 1. Ensures that the user can access this restaurant
        tenantAccessService
                .validateRestaurantAccess(restaurantId);

        // 2. Retrieves the period and ensures that it actually
        // belongs to the specified restaurant
        ReportingPeriod period =
                reportingPeriodRepository
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

        // 3. Retrieves metrics for all channels for the period
        List<ChannelMetric> metrics =
                channelMetricRepository
                    .findAllByPeriod_IdOrderByRestaurantChannel_Channel_NameAsc(
                        periodId
                    );

        // 4. Calculate the totals
        BigDecimal totalGrossRevenue =
                metrics.stream()
                    .map(ChannelMetric::getGrossRevenue)
                    .reduce(
                        BigDecimal.ZERO,
                        BigDecimal::add
                    );

        BigDecimal totalNetRevenue =
                metrics.stream()
                    .map(ChannelMetric::getNetRevenue)
                    .reduce(
                        BigDecimal.ZERO,
                        BigDecimal::add
                    );

        int totalOrders =
                metrics.stream()
                    .mapToInt(ChannelMetric::getOrders)
                    .sum();

        BigDecimal totalDiscountValue =
                totalGrossRevenue.subtract(
                    totalNetRevenue
                );

        // 5. Consolidated KPIs
        BigDecimal consolidatedAverageTicket =
                calculateAverageTicket(
                    totalGrossRevenue,
                    totalOrders
                );

        BigDecimal receivedPercentage =
                percentage(
                    totalNetRevenue,
                    totalGrossRevenue
                );

        BigDecimal discountPercentage =
                percentage(
                    totalDiscountValue,
                    totalGrossRevenue
                );

        BigDecimal averageOrdersPerDay =
                calculateOrdersPerDay(
                    totalOrders,
                    period.getOpenDays()
                );

        // 6. Individual performance of each channel
        List<ChannelPerformanceResponse> channels =
                metrics.stream()
                    .map(metric ->
                        toChannelPerformance(
                            metric,
                            totalGrossRevenue,
                            totalOrders
                        )
                    )
                    .toList();

        return new PeriodSummaryResponse(
            period.getId(),
            restaurantId,

            period.getReferenceMonth(),
            period.getOpenDays(),

            metrics.size(),

            money(totalGrossRevenue),
            totalOrders,

            averageOrdersPerDay,
            consolidatedAverageTicket,

            money(totalNetRevenue),
            receivedPercentage,

            money(totalDiscountValue),
            discountPercentage,

            channels
        );
    }

    private ChannelPerformanceResponse toChannelPerformance(
            ChannelMetric metric,
            BigDecimal totalGrossRevenue,
            int totalOrders) {

        BigDecimal gross =
                metric.getGrossRevenue();

        BigDecimal net =
                metric.getNetRevenue();

        BigDecimal discount =
                gross.subtract(net);

        return new ChannelPerformanceResponse(

            metric.getRestaurantChannel().getId(),

            metric.getRestaurantChannel()
                .getChannel()
                .getId(),

            metric.getRestaurantChannel()
                .getChannel()
                .getName(),

            money(gross),

            metric.getOrders(),

            calculateAverageTicket(
                gross,
                metric.getOrders()
            ),

            money(net),

            percentage(
                net,
                gross
            ),

            money(discount),

            percentage(
                discount,
                gross
            ),

            percentage(
                gross,
                totalGrossRevenue
            ),

            percentage(
                BigDecimal.valueOf(
                    metric.getOrders()
                ),
                BigDecimal.valueOf(
                    totalOrders
                )
            )
        );
    }

    private BigDecimal calculateAverageTicket(
            BigDecimal grossRevenue,
            int orders) {

        if (orders == 0) {
            return zero();
        }

        return grossRevenue.divide(
            BigDecimal.valueOf(orders),
            2,
            RoundingMode.HALF_UP
        );
    }

    private BigDecimal calculateOrdersPerDay(
            int orders,
            Short openDays) {

        if (openDays == null || openDays == 0) {
            return zero();
        }

        return BigDecimal
                .valueOf(orders)
                .divide(
                    BigDecimal.valueOf(
                        openDays.longValue()
                    ),
                    2,
                    RoundingMode.HALF_UP
                );
    }

    private BigDecimal percentage(
            BigDecimal value,
            BigDecimal total) {

        if (total == null
                || total.compareTo(
                    BigDecimal.ZERO
                ) == 0) {

            return zero();
        }

        return value
                .multiply(ONE_HUNDRED)
                .divide(
                    total,
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