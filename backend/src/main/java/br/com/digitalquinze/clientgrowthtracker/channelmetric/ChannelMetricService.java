package br.com.digitalquinze.clientgrowthtracker.channelmetric;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.digitalquinze.clientgrowthtracker.channelmetric.dto.ChannelMetricResponse;
import br.com.digitalquinze.clientgrowthtracker.channelmetric.dto.CreateChannelMetricRequest;
import br.com.digitalquinze.clientgrowthtracker.channelmetric.dto.UpdateChannelMetricRequest;
import br.com.digitalquinze.clientgrowthtracker.reportingperiod.ReportingPeriod;
import br.com.digitalquinze.clientgrowthtracker.reportingperiod.ReportingPeriodNotFoundException;
import br.com.digitalquinze.clientgrowthtracker.reportingperiod.ReportingPeriodRepository;
import br.com.digitalquinze.clientgrowthtracker.restaurantchannel.RestaurantChannel;
import br.com.digitalquinze.clientgrowthtracker.restaurantchannel.RestaurantChannelNotFoundException;
import br.com.digitalquinze.clientgrowthtracker.restaurantchannel.RestaurantChannelRepository;
import br.com.digitalquinze.clientgrowthtracker.security.TenantAccessService;

@Service
public class ChannelMetricService {

    private static final BigDecimal ONE_HUNDRED =
            new BigDecimal("100");

    private final ChannelMetricRepository channelMetricRepository;
    private final ReportingPeriodRepository reportingPeriodRepository;
    private final RestaurantChannelRepository restaurantChannelRepository;
    private final TenantAccessService tenantAccessService;

    public ChannelMetricService(
            ChannelMetricRepository channelMetricRepository,
            ReportingPeriodRepository reportingPeriodRepository,
            RestaurantChannelRepository restaurantChannelRepository,
            TenantAccessService tenantAccessService) {

        this.channelMetricRepository =
                channelMetricRepository;

        this.reportingPeriodRepository =
                reportingPeriodRepository;

        this.restaurantChannelRepository =
                restaurantChannelRepository;

        this.tenantAccessService =
                tenantAccessService;
    }

    @Transactional
    @PreAuthorize("isAuthenticated()")
    public ChannelMetricResponse create(
            Long restaurantId,
            Long periodId,
            CreateChannelMetricRequest request) {

        tenantAccessService
                .validateRestaurantAccess(restaurantId);

        ReportingPeriod period =
                findPeriod(
                    restaurantId,
                    periodId
                );

        RestaurantChannel restaurantChannel =
                restaurantChannelRepository
                    .findByIdAndRestaurant_Id(
                        request.restaurantChannelId(),
                        restaurantId
                    )
                    .orElseThrow(
                        () ->
                            new RestaurantChannelNotFoundException(
                                request.restaurantChannelId()
                            )
                    );

        if (!restaurantChannel.isActive()) {
            throw new IllegalArgumentException(
                "Restaurant channel is inactive"
            );
        }

        if (channelMetricRepository
                .existsByPeriod_IdAndRestaurantChannel_Id(
                    periodId,
                    restaurantChannel.getId()
                )) {

            throw new ChannelMetricAlreadyExistsException();
        }

        validateFinancialValues(
            request.grossRevenue(),
            request.netRevenue()
        );

        ChannelMetric metric =
                new ChannelMetric(
                    period,
                    restaurantChannel,
                    request.grossRevenue(),
                    request.orders(),
                    request.netRevenue(),
                    request.fees(),
                    request.promotions()
                );

        return toResponse(
            channelMetricRepository.save(metric)
        );
    }

    @Transactional(readOnly = true)
    @PreAuthorize("isAuthenticated()")
    public List<ChannelMetricResponse> findAll(
            Long restaurantId,
            Long periodId) {

        tenantAccessService
                .validateRestaurantAccess(restaurantId);

        findPeriod(
            restaurantId,
            periodId
        );

        return channelMetricRepository
                .findAllByPeriod_IdOrderByRestaurantChannel_Channel_NameAsc(
                    periodId
                )
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    @PreAuthorize("isAuthenticated()")
    public ChannelMetricResponse findById(
            Long restaurantId,
            Long periodId,
            Long metricId) {

        tenantAccessService
                .validateRestaurantAccess(restaurantId);

        findPeriod(
            restaurantId,
            periodId
        );

        return toResponse(
            findMetric(
                periodId,
                metricId
            )
        );
    }

    @Transactional
    @PreAuthorize("isAuthenticated()")
    public ChannelMetricResponse update(
            Long restaurantId,
            Long periodId,
            Long metricId,
            UpdateChannelMetricRequest request) {

        tenantAccessService
                .validateRestaurantAccess(restaurantId);

        findPeriod(
            restaurantId,
            periodId
        );

        ChannelMetric metric =
                findMetric(
                    periodId,
                    metricId
                );

        validateFinancialValues(
            request.grossRevenue(),
            request.netRevenue()
        );

        metric.setGrossRevenue(
            request.grossRevenue()
        );

        metric.setOrders(
            request.orders()
        );

        metric.setNetRevenue(
            request.netRevenue()
        );

        metric.setFees(
            request.fees()
        );

        metric.setPromotions(
            request.promotions()
        );

        return toResponse(
            channelMetricRepository.save(metric)
        );
    }

    @Transactional
    @PreAuthorize("isAuthenticated()")
    public void delete(
            Long restaurantId,
            Long periodId,
            Long metricId) {

        tenantAccessService
                .validateRestaurantAccess(restaurantId);

        findPeriod(
            restaurantId,
            periodId
        );

        ChannelMetric metric =
                findMetric(
                    periodId,
                    metricId
                );

        channelMetricRepository.delete(metric);
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
                    () ->
                        new ReportingPeriodNotFoundException(
                            periodId
                        )
                );
    }

    private ChannelMetric findMetric(
            Long periodId,
            Long metricId) {

        return channelMetricRepository
                .findByIdAndPeriod_Id(
                    metricId,
                    periodId
                )
                .orElseThrow(
                    () ->
                        new ChannelMetricNotFoundException(
                            metricId
                        )
                );
    }

    private void validateFinancialValues(
            BigDecimal grossRevenue,
            BigDecimal netRevenue) {

        if (netRevenue.compareTo(grossRevenue) > 0) {

            throw new IllegalArgumentException(
                "Net revenue cannot exceed gross revenue"
            );
        }
    }

    private ChannelMetricResponse toResponse(
            ChannelMetric metric) {

        BigDecimal gross =
                metric.getGrossRevenue();

        BigDecimal net =
                metric.getNetRevenue();

        BigDecimal discount =
                gross.subtract(net);

        BigDecimal averageTicket =
                calculateAverageTicket(
                    gross,
                    metric.getOrders()
                );

        BigDecimal receivedPercentage =
                percentage(
                    net,
                    gross
                );

        BigDecimal discountPercentage =
                percentage(
                    discount,
                    gross
                );

        return new ChannelMetricResponse(
            metric.getId(),
            metric.getPeriod().getId(),

            metric.getRestaurantChannel().getId(),
            metric.getRestaurantChannel()
                  .getChannel()
                  .getId(),

            metric.getRestaurantChannel()
                  .getChannel()
                  .getName(),

            gross,
            metric.getOrders(),
            net,
            metric.getFees(),
            metric.getPromotions(),

            metric.getSource(),

            averageTicket,
            receivedPercentage,
            discount,
            discountPercentage,

            metric.getCreatedAt(),
            metric.getUpdatedAt()
        );
    }

    private BigDecimal calculateAverageTicket(
            BigDecimal grossRevenue,
            Integer orders) {

        if (orders == 0) {
            return BigDecimal.ZERO
                    .setScale(2);
        }

        return grossRevenue.divide(
            BigDecimal.valueOf(orders),
            2,
            RoundingMode.HALF_UP
        );
    }

    private BigDecimal percentage(
            BigDecimal value,
            BigDecimal total) {

        if (total.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO
                    .setScale(2);
        }

        return value
                .multiply(ONE_HUNDRED)
                .divide(
                    total,
                    2,
                    RoundingMode.HALF_UP
                );
    }
}