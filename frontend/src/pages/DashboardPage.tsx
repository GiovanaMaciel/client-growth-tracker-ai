import { Link, useParams } from "react-router";

import { useQuery } from "@tanstack/react-query";

import {
  BarChart,
  Bar,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  ResponsiveContainer,
} from "recharts";

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

  return (
    <div className="min-h-screen bg-slate-50">
      <main className="max-w-7xl mx-auto px-6 py-10">
        <Link
          to={`/restaurants/${restaurantId}/periods`}
          className="text-sm text-teal-700 hover:underline"
        >
          ← Voltar aos períodos
        </Link>

        <div className="mt-7 mb-9">
          <h1 className="text-3xl font-bold text-slate-900">
            {restaurant.name}
          </h1>

          <p className="text-slate-500 mt-2 capitalize">
            Dashboard executivo · {formatMonth(summary.referenceMonth)}
          </p>

          <Link
            to={`/restaurants/${restaurantId}/periods/${periodId}/data`}
            className="inline-flex mt-5 rounded-lg bg-teal-600 px-5 py-3 text-sm font-semibold text-white hover:bg-teal-700 transition-colors"
          >
            Inserir / editar dados
          </Link>
        </div>

        <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-4">
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
            label="Líquido recebido"
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

        <section className="mt-8 bg-white border border-slate-200 rounded-xl p-6">
          <h2 className="text-xl font-semibold text-slate-900 mb-6">
            Faturamento por canal
          </h2>

          {chartData.length > 0 ? (
            <div className="h-80">
              <ResponsiveContainer width="100%" height="100%">
                <BarChart data={chartData}>
                  <CartesianGrid strokeDasharray="3 3" stroke="#e2e8f0" />

                  <XAxis dataKey="name" />

                  <YAxis />

                  <Tooltip
                    formatter={(value) => formatCurrency(Number(value))}
                  />

                  <Bar
                    dataKey="faturamento"
                    fill="#0d9488"
                    radius={[6, 6, 0, 0]}
                  />
                </BarChart>
              </ResponsiveContainer>
            </div>
          ) : (
            <p className="text-slate-500">
              Ainda não existem métricas financeiras para este período.
            </p>
          )}
        </section>

        <section className="mt-8 bg-white border border-slate-200 rounded-xl p-6">
          <h2 className="text-xl font-semibold text-slate-900 mb-6">
            Performance por canal
          </h2>

          <div className="overflow-x-auto">
            <table className="w-full text-sm text-left">
              <thead className="text-slate-500 border-b border-slate-200">
                <tr>
                  <th className="py-3">Canal</th>
                  <th>Faturamento</th>
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

                    <td>{formatNumber(channel.orders)}</td>

                    <td>{formatCurrency(channel.averageTicket)}</td>

                    <td>{formatPercentage(channel.salesSharePercentage)}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </section>

        <section className="mt-8">
          <h2 className="text-xl font-semibold text-slate-900 mb-5">
            Funis de conversão
          </h2>

          {funnels.length === 0 && (
            <div className="bg-white border border-slate-200 rounded-xl p-6 text-slate-500">
              Nenhum funil disponível para este período.
            </div>
          )}

          <div className="grid gap-5 lg:grid-cols-2">
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

        <section className="mt-8 mb-10">
          <h2 className="text-xl font-semibold text-slate-900 mb-5">
            Tráfego pago
          </h2>

          {paidTraffic ? (
            <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-4">
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
      </main>
    </div>
  );
}
