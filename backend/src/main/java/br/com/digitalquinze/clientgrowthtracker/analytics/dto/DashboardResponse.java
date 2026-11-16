
package br.com.digitalquinze.clientgrowthtracker.analytics.dto;

import java.util.List;

import br.com.digitalquinze.clientgrowthtracker.restaurant.dto.RestaurantResponse;
import br.com.digitalquinze.clientgrowthtracker.funnel.dto.FunnelResponse;
import br.com.digitalquinze.clientgrowthtracker.paidtraffic.dto.PaidTrafficMetricResponse;

public record DashboardResponse(

    RestaurantResponse restaurant,

    PeriodSummaryResponse summary,

    List<FunnelResponse> funnels,

    PaidTrafficMetricResponse paidTraffic

) {
}
