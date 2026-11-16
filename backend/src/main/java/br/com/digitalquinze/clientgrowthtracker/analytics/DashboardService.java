package br.com.digitalquinze.clientgrowthtracker.analytics;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.digitalquinze.clientgrowthtracker.analytics.dto.DashboardResponse;
import br.com.digitalquinze.clientgrowthtracker.analytics.dto.PeriodSummaryResponse;
import br.com.digitalquinze.clientgrowthtracker.channelmetric.ChannelMetricRepository;
import br.com.digitalquinze.clientgrowthtracker.funnel.FunnelService;
import br.com.digitalquinze.clientgrowthtracker.funnel.dto.FunnelResponse;
import br.com.digitalquinze.clientgrowthtracker.paidtraffic.PaidTrafficMetricService;
import br.com.digitalquinze.clientgrowthtracker.paidtraffic.dto.PaidTrafficMetricResponse;
import br.com.digitalquinze.clientgrowthtracker.restaurant.RestaurantService;
import br.com.digitalquinze.clientgrowthtracker.restaurant.dto.RestaurantResponse;

@Service
public class DashboardService {

    private final RestaurantService restaurantService;

    private final PeriodSummaryService periodSummaryService;

    private final ChannelMetricRepository channelMetricRepository;

    private final FunnelService funnelService;

    private final PaidTrafficMetricService paidTrafficMetricService;

    public DashboardService(
            RestaurantService restaurantService,
            PeriodSummaryService periodSummaryService,
            ChannelMetricRepository channelMetricRepository,
            FunnelService funnelService,
            PaidTrafficMetricService paidTrafficMetricService) {

        this.restaurantService = restaurantService;

        this.periodSummaryService = periodSummaryService;

        this.channelMetricRepository = channelMetricRepository;

        this.funnelService = funnelService;

        this.paidTrafficMetricService = paidTrafficMetricService;
    }

    @Transactional(readOnly = true)
    @PreAuthorize("isAuthenticated()")
    public DashboardResponse getDashboard(
            Long restaurantId,
            Long periodId) {

        PeriodSummaryResponse summary =
                periodSummaryService.getSummary(
                    restaurantId,
                    periodId
                );

        RestaurantResponse restaurant =
                restaurantService.findById(
                    restaurantId
                );

        List<FunnelResponse> funnels =
                channelMetricRepository
                    .findAllByPeriod_IdOrderByRestaurantChannel_Channel_NameAsc(
                        periodId
                    )
                    .stream()
                    .map(metric ->
                        funnelService.getFunnel(
                            restaurantId,
                            periodId,
                            metric.getId()
                        )
                    )
                    .filter(funnel ->
                        !funnel.stages().isEmpty()
                    )
                    .toList();

        PaidTrafficMetricResponse paidTraffic =
                paidTrafficMetricService
                    .findOptional(
                        restaurantId,
                        periodId
                    )
                    .orElse(null);

        return new DashboardResponse(
            restaurant,
            summary,
            funnels,
            paidTraffic
        );
    }
}
