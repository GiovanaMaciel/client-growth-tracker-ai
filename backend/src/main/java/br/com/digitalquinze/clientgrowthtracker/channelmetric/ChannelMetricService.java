package br.com.digitalquinze.clientgrowthtracker.channelmetric;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.digitalquinze.clientgrowthtracker.channelmetric.dto.ChannelMetricResponse;
import br.com.digitalquinze.clientgrowthtracker.channelmetric.dto.CreateChannelMetricRequest;
import br.com.digitalquinze.clientgrowthtracker.channelmetric.dto.IfoodFinancialDetailsRequest;
import br.com.digitalquinze.clientgrowthtracker.channelmetric.dto.IfoodFinancialDetailsResponse;
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
    private final IfoodFinancialDetailRepository ifoodFinancialDetailRepository;
    private final TenantAccessService tenantAccessService;

    public ChannelMetricService(
            ChannelMetricRepository channelMetricRepository,
            ReportingPeriodRepository reportingPeriodRepository,
            RestaurantChannelRepository restaurantChannelRepository,
            IfoodFinancialDetailRepository ifoodFinancialDetailRepository,
            TenantAccessService tenantAccessService) {

        this.channelMetricRepository = channelMetricRepository;
        this.reportingPeriodRepository = reportingPeriodRepository;
        this.restaurantChannelRepository = restaurantChannelRepository;
        this.ifoodFinancialDetailRepository = ifoodFinancialDetailRepository;
        this.tenantAccessService = tenantAccessService;
    }

    @Transactional
    @PreAuthorize("isAuthenticated()")
    public ChannelMetricResponse create(
            Long restaurantId,
            Long periodId,
            CreateChannelMetricRequest request) {

        tenantAccessService.validateRestaurantAccess(restaurantId);

        ReportingPeriod period = findPeriod(
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
                                () -> new RestaurantChannelNotFoundException(
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

        BigDecimal resolvedNetRevenue = request.netRevenue();

        IfoodFinancialDetailsRequest resolvedDetails =
                request.ifoodDetails();

        if (isIfood(restaurantChannel)) {

            IfoodFinancialCalculator.Result calculated =
                    IfoodFinancialCalculator.calculate(
                            request.grossRevenue(),
                            request.fees(),
                            request.ifoodDetails()
                    );

            resolvedNetRevenue = calculated.netRevenue();

            resolvedDetails =
                    IfoodFinancialCalculator.withCalculatedTransfers(
                            request.ifoodDetails(),
                            calculated
                    );

        } else {

            validateFinancialValues(
                    request.grossRevenue(),
                    resolvedNetRevenue
            );
        }

        ChannelMetric metric = new ChannelMetric(
                period,
                restaurantChannel,
                request.grossRevenue(),
                request.orders(),
                resolvedNetRevenue,
                request.fees(),
                request.promotions()
        );

        ChannelMetric saved =
                channelMetricRepository.save(metric);

        saveIfoodDetails(saved, resolvedDetails);

        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    @PreAuthorize("isAuthenticated()")
    public List<ChannelMetricResponse> findAll(
            Long restaurantId,
            Long periodId) {

        tenantAccessService.validateRestaurantAccess(restaurantId);

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

        tenantAccessService.validateRestaurantAccess(restaurantId);

        findPeriod(
                restaurantId,
                periodId
        );

        ChannelMetric metric = findMetric(
                periodId,
                metricId
        );

        return toResponse(metric);
    }

    @Transactional
    @PreAuthorize("isAuthenticated()")
    public ChannelMetricResponse update(
            Long restaurantId,
            Long periodId,
            Long metricId,
            UpdateChannelMetricRequest request) {

        tenantAccessService.validateRestaurantAccess(restaurantId);

        findPeriod(
                restaurantId,
                periodId
        );

        ChannelMetric metric = findMetric(
                periodId,
                metricId
        );

        BigDecimal resolvedNetRevenue = request.netRevenue();

        IfoodFinancialDetailsRequest resolvedDetails =
                request.ifoodDetails();

        if (isIfood(metric.getRestaurantChannel())) {

            IfoodFinancialCalculator.Result calculated =
                    IfoodFinancialCalculator.calculate(
                            request.grossRevenue(),
                            request.fees(),
                            request.ifoodDetails()
                    );

            resolvedNetRevenue = calculated.netRevenue();

            resolvedDetails =
                    IfoodFinancialCalculator.withCalculatedTransfers(
                            request.ifoodDetails(),
                            calculated
                    );

        } else {

            validateFinancialValues(
                    request.grossRevenue(),
                    resolvedNetRevenue
            );
        }

        metric.setGrossRevenue(
                request.grossRevenue()
        );

        metric.setOrders(
                request.orders()
        );

        metric.setNetRevenue(
                resolvedNetRevenue
        );

        metric.setFees(
                request.fees()
        );

        metric.setPromotions(
                request.promotions()
        );

        ChannelMetric saved =
                channelMetricRepository.save(metric);

        saveIfoodDetails(saved, resolvedDetails);

        return toResponse(saved);
    }

    @Transactional
    @PreAuthorize("isAuthenticated()")
    public void delete(
            Long restaurantId,
            Long periodId,
            Long metricId) {

        tenantAccessService.validateRestaurantAccess(restaurantId);

        findPeriod(
                restaurantId,
                periodId
        );

        ChannelMetric metric = findMetric(
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
                        () -> new ReportingPeriodNotFoundException(
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
                        () -> new ChannelMetricNotFoundException(
                                metricId
                        )
                );
    }

    private boolean isIfood(
            RestaurantChannel restaurantChannel) {

        return "ifood".equals(
                restaurantChannel
                        .getChannel()
                        .getSlug()
        );
    }

    private void saveIfoodDetails(
            ChannelMetric metric,
            IfoodFinancialDetailsRequest detailsRequest) {

        if (detailsRequest == null) {
            return;
        }

        if (!isIfood(metric.getRestaurantChannel())) {
            throw new IllegalArgumentException(
                    "iFood financial details are only available for the iFood channel"
            );
        }

        IfoodFinancialDetail detail =
                ifoodFinancialDetailRepository
                        .findByChannelMetric_Id(metric.getId())
                        .orElseGet(
                                () -> new IfoodFinancialDetail(metric)
                        );

        detail.update(detailsRequest);

        ifoodFinancialDetailRepository.save(detail);
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

        BigDecimal gross = metric.getGrossRevenue();
        BigDecimal net = metric.getNetRevenue();

        BigDecimal discount = gross.subtract(net);

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

        IfoodFinancialDetailsResponse ifoodDetails =
                ifoodFinancialDetailRepository
                        .findByChannelMetric_Id(metric.getId())
                        .map(IfoodFinancialDetailsResponse::from)
                        .orElse(null);

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

                ifoodDetails,

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
            return BigDecimal.ZERO.setScale(2);
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
            return BigDecimal.ZERO.setScale(2);
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
