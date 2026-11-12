package br.com.digitalquinze.clientgrowthtracker.funnel;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.digitalquinze.clientgrowthtracker.channelmetric.ChannelMetric;
import br.com.digitalquinze.clientgrowthtracker.channelmetric.ChannelMetricNotFoundException;
import br.com.digitalquinze.clientgrowthtracker.channelmetric.ChannelMetricRepository;
import br.com.digitalquinze.clientgrowthtracker.funnel.dto.FunnelResponse;
import br.com.digitalquinze.clientgrowthtracker.funnel.dto.FunnelStageResponse;
import br.com.digitalquinze.clientgrowthtracker.funnel.dto.FunnelStageValueRequest;
import br.com.digitalquinze.clientgrowthtracker.funnel.dto.UpdateFunnelRequest;
import br.com.digitalquinze.clientgrowthtracker.reportingperiod.ReportingPeriodNotFoundException;
import br.com.digitalquinze.clientgrowthtracker.reportingperiod.ReportingPeriodRepository;
import br.com.digitalquinze.clientgrowthtracker.security.TenantAccessService;

@Service
public class FunnelService {

    private static final BigDecimal ONE_HUNDRED =
            new BigDecimal("100");

    private final FunnelStageDefinitionRepository
            definitionRepository;

    private final FunnelStageValueRepository
            valueRepository;

    private final ChannelMetricRepository
            channelMetricRepository;

    private final ReportingPeriodRepository
            reportingPeriodRepository;

    private final TenantAccessService
            tenantAccessService;

    public FunnelService(
            FunnelStageDefinitionRepository definitionRepository,
            FunnelStageValueRepository valueRepository,
            ChannelMetricRepository channelMetricRepository,
            ReportingPeriodRepository reportingPeriodRepository,
            TenantAccessService tenantAccessService) {

        this.definitionRepository =
                definitionRepository;

        this.valueRepository =
                valueRepository;

        this.channelMetricRepository =
                channelMetricRepository;

        this.reportingPeriodRepository =
                reportingPeriodRepository;

        this.tenantAccessService =
                tenantAccessService;
    }

    @Transactional(readOnly = true)
    @PreAuthorize("isAuthenticated()")
    public FunnelResponse getFunnel(
            Long restaurantId,
            Long periodId,
            Long metricId) {

        ChannelMetric metric =
                validateAndFindMetric(
                    restaurantId,
                    periodId,
                    metricId
                );

        return buildResponse(metric);
    }

    @Transactional
    @PreAuthorize("isAuthenticated()")
    public FunnelResponse updateFunnel(
            Long restaurantId,
            Long periodId,
            Long metricId,
            UpdateFunnelRequest request) {

        ChannelMetric metric =
                validateAndFindMetric(
                    restaurantId,
                    periodId,
                    metricId
                );

        Long channelId =
                metric
                    .getRestaurantChannel()
                    .getChannel()
                    .getId();

        List<FunnelStageDefinition> definitions =
                definitionRepository
                    .findAllByChannel_IdOrderByStageOrderAsc(
                        channelId
                    );

        Map<Long, FunnelStageDefinition> definitionsById =
                new HashMap<>();

        for (FunnelStageDefinition definition : definitions) {
            definitionsById.put(
                definition.getId(),
                definition
            );
        }

        Set<Long> receivedStageIds =
                new HashSet<>();

        for (FunnelStageValueRequest stageRequest
                : request.stages()) {

            if (!receivedStageIds.add(
                    stageRequest.stageId())) {

                throw new IllegalArgumentException(
                    "Duplicate funnel stage in request"
                );
            }

            FunnelStageDefinition definition =
                    definitionsById.get(
                        stageRequest.stageId()
                    );

            if (definition == null) {
                throw new IllegalArgumentException(
                    "Funnel stage does not belong to this channel"
                );
            }

            if (definition.getValueSource()
                    != FunnelValueSource.MANUAL) {

                throw new IllegalArgumentException(
                    "This funnel stage cannot be manually updated"
                );
            }

            FunnelStageValue value =
                    valueRepository
                        .findByChannelMetric_IdAndFunnelStage_Id(
                            metricId,
                            definition.getId()
                        )
                        .orElseGet(
                            () -> new FunnelStageValue(
                                metric,
                                definition,
                                stageRequest.value()
                            )
                        );

            value.setValue(
                stageRequest.value()
            );

            valueRepository.save(value);
        }

        return buildResponse(metric);
    }

    private ChannelMetric validateAndFindMetric(
            Long restaurantId,
            Long periodId,
            Long metricId) {

        tenantAccessService
                .validateRestaurantAccess(
                    restaurantId
                );

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

    private FunnelResponse buildResponse(
            ChannelMetric metric) {

        Long channelId =
                metric
                    .getRestaurantChannel()
                    .getChannel()
                    .getId();

        List<FunnelStageDefinition> definitions =
                definitionRepository
                    .findAllByChannel_IdOrderByStageOrderAsc(
                        channelId
                    );

        Map<Long, Integer> storedValues =
                new HashMap<>();

        valueRepository
            .findAllByChannelMetric_Id(
                metric.getId()
            )
            .forEach(
                value ->
                    storedValues.put(
                        value
                            .getFunnelStage()
                            .getId(),

                        value.getValue()
                    )
            );

        Integer visits =
                findVisits(
                    definitions,
                    storedValues,
                    metric
                );

        List<FunnelStageResponse> stages =
                definitions
                    .stream()
                    .map(
                        definition -> {

                            Integer value =
                                    resolveValue(
                                        definition,
                                        storedValues,
                                        metric
                                    );

                            return new FunnelStageResponse(
                                definition.getId(),
                                definition.getStageKey(),
                                definition.getStageName(),
                                definition.getStageOrder(),
                                definition.getValueSource(),
                                value,
                                conversion(
                                    value,
                                    visits
                                )
                            );
                        }
                    )
                    .toList();

        return new FunnelResponse(

            metric.getId(),

            channelId,

            metric
                .getRestaurantChannel()
                .getChannel()
                .getName(),

            stages
        );
    }

    private Integer resolveValue(
            FunnelStageDefinition definition,
            Map<Long, Integer> storedValues,
            ChannelMetric metric) {

        if (definition.getValueSource()
                == FunnelValueSource.ORDERS) {

            return metric.getOrders();
        }

        return storedValues.get(
            definition.getId()
        );
    }

    private Integer findVisits(
            List<FunnelStageDefinition> definitions,
            Map<Long, Integer> storedValues,
            ChannelMetric metric) {

        return definitions
                .stream()
                .filter(
                    definition ->
                        definition
                            .getStageKey()
                            .equals("visits")
                )
                .findFirst()
                .map(
                    definition ->
                        resolveValue(
                            definition,
                            storedValues,
                            metric
                        )
                )
                .orElse(null);
    }

    private BigDecimal conversion(
            Integer value,
            Integer visits) {

        if (value == null
                || visits == null
                || visits == 0) {

            return null;
        }

        return BigDecimal
                .valueOf(value)
                .multiply(ONE_HUNDRED)
                .divide(
                    BigDecimal.valueOf(visits),
                    2,
                    RoundingMode.HALF_UP
                );
    }
}