import { Link, useParams } from "react-router";

import { useQuery } from "@tanstack/react-query";

import axios from "axios";

import { api } from "../api/client";

import KpiCard from "../components/KpiCard";

import type { DashboardResponse } from "../types/dashboard";

import {
  formatCurrency,
  formatNumber,
  formatPercentage,
  formatMonth,
} from "../utils/formatters";

import DownloadReportButton from "../components/DownloadReportButton";

export default function DashboardPage() {
  const { restaurantId, periodId } = useParams();

  const validIds =
    Number.isSafeInteger(Number(restaurantId)) &&
    Number(restaurantId) > 0 &&
    Number.isSafeInteger(Number(periodId)) &&
    Number(periodId) > 0;

  const { data, isPending, isError, error } = useQuery<DashboardResponse>({
    queryKey: ["dashboard", restaurantId, periodId],

    queryFn: async () => {
      const response = await api.get<DashboardResponse>(
        `/restaurants/${restaurantId}/periods/${periodId}/dashboard`,
      );

      return response.data;
    },

    enabled: validIds,
  });

  if (!validIds) {
    return <p>Restaurante ou período inválido.</p>;
  }

  if (isPending) {
    return (
      <div className="min-h-screen bg-slate-50 p-10">
        Carregando dashboard...
      </div>
    );
  }

  if (isError) {
    const status = axios.isAxiosError(error)
      ? error.response?.status
      : undefined;

    const message =
      status === 403
        ? "Você não tem permissão para acessar este restaurante."
        : status === 404
          ? "Restaurante ou período não encontrado."
          : status === 401
            ? "Sua sessão expirou. Faça login novamente."
            : "Não foi possível carregar o dashboard.";

    return (
      <div className="p-10">
        <p className="text-red-600">{message}</p>

        <Link
          to="/restaurants"
          className="text-teal-700 underline mt-4 inline-block"
        >
          Voltar
        </Link>
      </div>
    );
  }

  const { restaurant, summary, funnels, paidTraffic } = data;

  const chartData = summary.channels.map((channel) => ({
    name: channel.channelName,
    faturamento: channel.grossRevenue,
  }));

  const ifoodFinancial = summary.channels.find(
    (channel) =>
      channel.ifoodDetails &&
      Object.values(channel.ifoodDetails).some((value) => value !== null),
  );
  const ifoodDetails = ifoodFinancial?.ifoodDetails;

  const maximumRevenue = Math.max(
    0,
    ...chartData.map((channel) => channel.faturamento),
  );

  const chartScale = Math.max(1000, Math.ceil(maximumRevenue / 1000) * 1000);

  const chartTicks = Array.from(
    { length: 5 },
    (_, index) => (chartScale * index) / 4,
  );

  return (
    <div className="min-h-screen bg-slate-50">
      <main id="dashboard-print" className="max-w-7xl mx-auto px-6 py-10">
        <Link
          to={`/restaurants/${restaurantId}/periods`}
          className="cgt-no-print text-sm text-teal-700 hover:underline"
        >
          ← Voltar aos períodos
        </Link>

        <div className="cgt-report-header mt-7 mb-9">
          <h1 className="text-3xl font-bold text-slate-900">
            {restaurant.name}
          </h1>

          <p className="text-slate-500 mt-2 capitalize">
            Dashboard executivo · {formatMonth(summary.referenceMonth)}
          </p>

          <div className="cgt-no-print mt-5 flex flex-wrap items-center gap-3">
            <DownloadReportButton />

            <Link
              to={`/restaurants/${restaurantId}/periods/${periodId}/data`}
              className="inline-flex items-center justify-center rounded-lg border border-teal-600 bg-white px-5 py-3 text-sm font-semibold text-teal-700 transition-colors hover:bg-teal-50"
            >
              Inserir / editar dados
            </Link>
          </div>
        </div>

        <div className="cgt-kpi-grid grid gap-4 md:grid-cols-2 lg:grid-cols-4">
          <KpiCard
            label="Faturamento bruto"
            value={formatCurrency(summary.totalGrossRevenue)}
          />

          <KpiCard label="Pedidos" value={formatNumber(summary.totalOrders)} />

          <KpiCard
            label="Ticket médio"
            value={formatCurrency(summary.consolidatedAverageTicket)}
          />

          <KpiCard
            label="Valor líquido informado"
            value={formatCurrency(summary.totalNetRevenue)}
          />

          <KpiCard
            label="Descontos"
            value={formatCurrency(summary.totalDiscountValue)}
          />

          <KpiCard
            label="% recebido"
            value={formatPercentage(summary.receivedPercentage)}
          />

          <KpiCard
            label="Pedidos por dia"
            value={formatNumber(summary.averageOrdersPerDay)}
          />

          <KpiCard
            label="Dias abertos"
            value={formatNumber(summary.openDays)}
          />
        </div>

        <section className="cgt-report-card mt-8 bg-white border border-slate-200 rounded-xl p-6">
          <h2 className="text-xl font-semibold text-slate-900 mb-6">
            Faturamento por canal
          </h2>

          {chartData.length > 0 ? (
            <div
              className="cgt-chart w-full"
              role="img"
              aria-label={`Faturamento por canal: ${chartData
                .map(
                  (channel) =>
                    `${channel.name}, ${formatCurrency(channel.faturamento)}`,
                )
                .join("; ")}`}
            >
              <div className="space-y-4">
                {chartData.map((channel) => (
                  <div
                    key={channel.name}
                    className="grid grid-cols-[7rem_minmax(0,1fr)] items-center gap-3"
                  >
                    <span
                      className="truncate text-right text-sm text-slate-500"
                      title={channel.name}
                    >
                      {channel.name}
                    </span>

                    <div className="relative h-8 border-l border-slate-300">
                      <div
                        aria-hidden="true"
                        className="pointer-events-none absolute inset-0 flex justify-between"
                      >
                        {chartTicks.map((tick) => (
                          <span
                            key={tick}
                            className="h-full border-l border-dashed border-slate-200"
                          />
                        ))}
                      </div>

                      <div
                        className="relative h-full rounded-r-md bg-teal-600"
                        style={{
                          width: `${Math.min(100, Math.max(0, (channel.faturamento / chartScale) * 100))}%`,
                        }}
                        title={`${channel.name}: ${formatCurrency(channel.faturamento)}`}
                      />
                    </div>
                  </div>
                ))}
              </div>

              <div className="mt-3 grid grid-cols-[7rem_minmax(0,1fr)] items-center gap-3">
                <span />
                <div className="flex justify-between border-t border-slate-400 pt-2 text-xs text-slate-500">
                  {chartTicks.map((tick) => (
                    <span key={tick}>{formatNumber(tick)}</span>
                  ))}
                </div>
              </div>
            </div>
          ) : (
            <p className="text-slate-500">
              Ainda não existem métricas financeiras para este período.
            </p>
          )}
        </section>

        <section className="cgt-report-card mt-8 bg-white border border-slate-200 rounded-xl p-6">
          <h2 className="text-xl font-semibold text-slate-900 mb-6">
            Performance por canal
          </h2>

          <div className="overflow-x-auto">
            <table className="cgt-channel-table w-full text-sm text-left">
              <thead className="text-slate-500 border-b border-slate-200">
                <tr>
                  <th className="py-3">Canal</th>
                  <th>Faturamento</th>
                  <th>Valor líquido</th>
                  <th>Pedidos</th>
                  <th>Ticket médio</th>
                  <th>Participação</th>
                </tr>
              </thead>

              <tbody>
                {summary.channels.map((channel) => (
                  <tr
                    key={channel.restaurantChannelId}
                    className="border-b border-slate-100"
                  >
                    <td className="py-4 font-semibold">
                      {channel.channelName}
                    </td>

                    <td>{formatCurrency(channel.grossRevenue)}</td>

                    <td>{formatCurrency(channel.netRevenue)}</td>

                    <td>{formatNumber(channel.orders)}</td>

                    <td>{formatCurrency(channel.averageTicket)}</td>

                    <td>{formatPercentage(channel.salesSharePercentage)}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </section>

        {ifoodFinancial && ifoodDetails && (
          <section className="cgt-report-card mt-8 rounded-xl border border-slate-200 bg-white p-6">
            <h2 className="mb-2 text-xl font-semibold text-slate-900">
              Detalhamento financeiro — iFood
            </h2>
            <p className="mb-5 text-sm text-slate-500">
              Valores informados no demonstrativo financeiro do iFood.
            </p>
            <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
              {(
                [
                  ["Valor das vendas", ifoodFinancial.grossRevenue],
                  ["Taxas e comissões", ifoodFinancial.fees],
                ] as const
              ).map(([label, amount]) => (
                <div key={label} className="rounded-lg bg-slate-50 p-4">
                  <p className="text-xs text-slate-500">{label}</p>
                  <p className="mt-1 font-semibold text-slate-900">
                    {amount === null ? "—" : formatCurrency(amount)}
                  </p>
                </div>
              ))}
              {(
                [
                  ["Serviços e promoções", ifoodDetails.servicesAndPromotions],
                  ["Ajustes", ifoodDetails.adjustments],
                  ["Faturamento após descontos", ifoodFinancial.netRevenue],
                  ["Recebido direto pela loja", ifoodDetails.directReceipts],
                  ["Valor dos repasses", ifoodDetails.marketplaceTransfers],
                ] as const
              ).map(([label, amount]) => (
                <div key={label} className="rounded-lg bg-slate-50 p-4">
                  <p className="text-xs text-slate-500">{label}</p>
                  <p className="mt-1 font-semibold text-slate-900">
                    {amount === null ? "—" : formatCurrency(amount)}
                  </p>
                </div>
              ))}
            </div>
          </section>
        )}

        <section className="cgt-funnels mt-8">
          <h2 className="text-xl font-semibold text-slate-900 mb-5">
            Funis de conversão
          </h2>

          {funnels.length === 0 && (
            <div className="bg-white border border-slate-200 rounded-xl p-6 text-slate-500">
              Nenhum funil disponível para este período.
            </div>
          )}

          <div className="cgt-funnel-grid grid gap-5 lg:grid-cols-2">
            {funnels.map((funnel) => (
              <div
                key={funnel.channelMetricId}
                className="bg-white border border-slate-200 rounded-xl p-6"
              >
                <h3 className="font-semibold text-lg mb-6">
                  {funnel.channelName}
                </h3>

                <div className="space-y-5">
                  {funnel.stages.map((stage) => (
                    <div key={stage.stageId}>
                      <div className="flex justify-between text-sm mb-2">
                        <span className="text-slate-600">
                          {stage.stageName}
                        </span>

                        <span className="font-medium">
                          {formatNumber(stage.value)}
                          {" · "}
                          {formatPercentage(stage.conversionFromVisits)}
                        </span>
                      </div>

                      <div className="h-3 bg-slate-100 rounded-full overflow-hidden">
                        <div
                          className="h-full bg-teal-500 rounded-full"
                          style={{
                            width: `${Math.min(
                              Math.max(stage.conversionFromVisits ?? 0, 0),
                              100,
                            )}%`,
                          }}
                        />
                      </div>
                    </div>
                  ))}
                </div>
              </div>
            ))}
          </div>
        </section>

        <section className="cgt-paid-traffic mt-8 mb-10">
          <h2 className="text-xl font-semibold text-slate-900 mb-5">
            Tráfego pago
          </h2>

          {paidTraffic ? (
            <div className="cgt-paid-grid grid gap-4 md:grid-cols-2 lg:grid-cols-4">
              <KpiCard
                label="Investimento"
                value={formatCurrency(paidTraffic.investment)}
              />

              <KpiCard
                label="Receita atribuída"
                value={formatCurrency(paidTraffic.attributedRevenue)}
              />

              <KpiCard
                label="ROAS"
                value={`${formatNumber(paidTraffic.roas)}x`}
              />

              <KpiCard label="CPA" value={formatCurrency(paidTraffic.cpa)} />

              <KpiCard
                label="Alcance"
                value={formatNumber(paidTraffic.reach)}
              />

              <KpiCard
                label="Impressões"
                value={formatNumber(paidTraffic.impressions)}
              />

              <KpiCard
                label="Visitas"
                value={formatNumber(paidTraffic.visits)}
              />

              <KpiCard
                label="Compras"
                value={formatNumber(paidTraffic.purchases)}
              />

              <KpiCard
                label="Custo por visita"
                value={
                  paidTraffic.visits === 0 || paidTraffic.costPerVisit == null
                    ? "—"
                    : formatCurrency(paidTraffic.costPerVisit)
                }
              />

              <KpiCard
                label="Frequência"
                value={
                  paidTraffic.reach === 0 || paidTraffic.frequency == null
                    ? "—"
                    : paidTraffic.frequency.toLocaleString("pt-BR", {
                        minimumFractionDigits: 2,
                        maximumFractionDigits: 2,
                      })
                }
              />
            </div>
          ) : (
            <div className="bg-white border border-slate-200 rounded-xl p-6 text-slate-500">
              Dados de tráfego pago ainda não preenchidos.
            </div>
          )}
        </section>

        <footer className="cgt-report-footer mb-6 text-xs text-slate-500">
          O valor líquido informado não representa necessariamente o lucro. No
          iFood, o faturamento após descontos inclui recebimentos diretos e
          repasses da plataforma.
        </footer>
      </main>
    </div>
  );
}
