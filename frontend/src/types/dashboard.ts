export interface ChannelPerformance {
  restaurantChannelId: number;
  channelId: number;
  channelName: string;

  grossRevenue: number;
  orders: number;
  averageTicket: number;

  netRevenue: number;
  receivedPercentage: number;

  discountValue: number;
  discountPercentage: number;

  salesSharePercentage: number;
  ordersSharePercentage: number;
}

export interface PeriodSummary {
  periodId: number;
  restaurantId: number;

  referenceMonth: string;
  openDays: number;

  channelCount: number;

  totalGrossRevenue: number;
  totalOrders: number;

  averageOrdersPerDay: number;
  consolidatedAverageTicket: number;

  totalNetRevenue: number;
  receivedPercentage: number;

  totalDiscountValue: number;
  discountPercentage: number;

  channels: ChannelPerformance[];
}

export interface FunnelStage {
  stageId: number;
  stageKey: string;
  stageName: string;
  stageOrder: number;

  valueSource: "MANUAL" | "ORDERS";

  value: number | null;
  conversionFromVisits: number | null;
}

export interface Funnel {
  channelMetricId: number;
  channelId: number;
  channelName: string;

  stages: FunnelStage[];
}

export interface PaidTraffic {
  id: number;
  periodId: number;

  investment: number;
  reach: number;
  impressions: number;
  visits: number;
  carts: number;
  purchases: number;

  attributedRevenue: number;

  source: "MANUAL" | "INTEGRATION";

  roas: number;
  cpa: number;
  costPerVisit: number | null;
  averageTicket: number;
  frequency: number | null;

  visitToCartConversion: number;
  visitToPurchaseConversion: number;
  cartToPurchaseConversion: number;
}

export interface DashboardResponse {
  restaurant: {
    id: number;
    name: string;
    createdAt: string;
    updatedAt: string;
  };

  summary: PeriodSummary;

  funnels: Funnel[];

  paidTraffic: PaidTraffic | null;
}
