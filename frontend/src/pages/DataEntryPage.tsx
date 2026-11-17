import { useState, type SubmitEvent } from "react";
import { Link, useParams } from "react-router";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";

import { api } from "../api/client";
import DataEntryNavigation from "../components/DataEntryNavigation";

interface RestaurantChannel {
  id: number;
  restaurantId: number;
  channelId: number;
  channelName: string;
  channelSlug: string;
  active: boolean;
}

interface ChannelMetric {
  id: number;
  restaurantChannelId: number;
  grossRevenue: number;
  orders: number;
  netRevenue: number;
  fees: number | null;
  promotions: number | null;
}

interface FinancialPayload {
  grossRevenue: number;
  orders: number;
  netRevenue: number;
  fees: number | null;
  promotions: number | null;
}

interface ChannelMetricsFormProps {
  restaurantId: string;
  periodId: string;
  channel: RestaurantChannel;
  existingMetric?: ChannelMetric;
}

function ChannelMetricsForm({
  restaurantId,
  periodId,
  channel,
  existingMetric,
}: ChannelMetricsFormProps) {
  const queryClient = useQueryClient();

  const [grossRevenue, setGrossRevenue] = useState(
    existingMetric?.grossRevenue.toString() ?? "",
  );

  const [orders, setOrders] = useState(existingMetric?.orders.toString() ?? "");

  const [netRevenue, setNetRevenue] = useState(
    existingMetric?.netRevenue.toString() ?? "",
  );

  const [fees, setFees] = useState(existingMetric?.fees?.toString() ?? "");

  const [promotions, setPromotions] = useState(
    existingMetric?.promotions?.toString() ?? "",
  );

  const [validationError, setValidationError] = useState("");
  const [saved, setSaved] = useState(false);

  const endpoint = `/restaurants/${restaurantId}/periods/${periodId}/channel-metrics`;

  const mutation = useMutation({
    mutationFn: async (payload: FinancialPayload) => {
      if (existingMetric) {
        return api.put(`${endpoint}/${existingMetric.id}`, payload);
      }

      return api.post(endpoint, {
        restaurantChannelId: channel.id,
        ...payload,
      });
    },

    onSuccess: async () => {
      await Promise.all([
        queryClient.invalidateQueries({
          queryKey: ["channel-metrics", restaurantId, periodId],
        }),

        queryClient.invalidateQueries({
          queryKey: ["dashboard", restaurantId, periodId],
        }),
      ]);

      setSaved(true);
    },
  });

  function handleSubmit(event: SubmitEvent<HTMLFormElement>) {
    event.preventDefault();

    setSaved(false);
    setValidationError("");

    const gross = Number(grossRevenue);
    const net = Number(netRevenue);
    const totalOrders = Number(orders);

    if (
      !Number.isFinite(gross) ||
      !Number.isFinite(net) ||
      !Number.isSafeInteger(totalOrders) ||
      gross < 0 ||
      net < 0 ||
      totalOrders < 0
    ) {
      setValidationError("Verifique os valores informados.");
      return;
    }

    if (net > gross) {
      setValidationError(
        "O líquido recebido não pode exceder o faturamento bruto.",
      );
      return;
    }

    const payload: FinancialPayload = {
      grossRevenue: gross,
      orders: totalOrders,
      netRevenue: net,
      fees: fees.trim() === "" ? null : Number(fees),
      promotions: promotions.trim() === "" ? null : Number(promotions),
    };

    if (
      (payload.fees !== null &&
        (!Number.isFinite(payload.fees) || payload.fees < 0)) ||
      (payload.promotions !== null &&
        (!Number.isFinite(payload.promotions) || payload.promotions < 0))
    ) {
      setValidationError("Taxas e promoções devem ser valores válidos.");
      return;
    }

    mutation.mutate(payload);
  }

  return (
    <form
      onSubmit={handleSubmit}
      className="bg-white border border-slate-200 rounded-xl p-6 shadow-sm"
    >
      <div className="flex items-center justify-between mb-6">
        <h2 className="text-xl font-semibold text-slate-900">
          {channel.channelName}
        </h2>

        <span className="text-xs rounded-full bg-slate-100 text-slate-600 px-3 py-1">
          {existingMetric ? "Cadastrado" : "Não preenchido"}
        </span>
      </div>

      <div className="grid gap-5 md:grid-cols-2">
        <div>
          <label
            htmlFor={`gross-${channel.id}`}
            className="block text-sm font-medium mb-2"
          >
            Faturamento bruto (R$)
          </label>

          <input
            id={`gross-${channel.id}`}
            type="number"
            min="0"
            step="0.01"
            required
            value={grossRevenue}
            onChange={(event) => setGrossRevenue(event.target.value)}
            className="w-full rounded-lg border border-slate-300 px-4 py-3"
          />
        </div>

        <div>
          <label
            htmlFor={`orders-${channel.id}`}
            className="block text-sm font-medium mb-2"
          >
            Pedidos
          </label>

          <input
            id={`orders-${channel.id}`}
            type="number"
            min="0"
            step="1"
            required
            value={orders}
            onChange={(event) => setOrders(event.target.value)}
            className="w-full rounded-lg border border-slate-300 px-4 py-3"
          />
        </div>

        <div>
          <label
            htmlFor={`net-${channel.id}`}
            className="block text-sm font-medium mb-2"
          >
            Líquido recebido (R$)
          </label>

          <input
            id={`net-${channel.id}`}
            type="number"
            min="0"
            step="0.01"
            required
            value={netRevenue}
            onChange={(event) => setNetRevenue(event.target.value)}
            className="w-full rounded-lg border border-slate-300 px-4 py-3"
          />
        </div>

        <div>
          <label
            htmlFor={`fees-${channel.id}`}
            className="block text-sm font-medium mb-2"
          >
            Taxas (R$)
          </label>

          <input
            id={`fees-${channel.id}`}
            type="number"
            min="0"
            step="0.01"
            value={fees}
            onChange={(event) => setFees(event.target.value)}
            className="w-full rounded-lg border border-slate-300 px-4 py-3"
          />
        </div>

        <div>
          <label
            htmlFor={`promotions-${channel.id}`}
            className="block text-sm font-medium mb-2"
          >
            Promoções (R$)
          </label>

          <input
            id={`promotions-${channel.id}`}
            type="number"
            min="0"
            step="0.01"
            value={promotions}
            onChange={(event) => setPromotions(event.target.value)}
            className="w-full rounded-lg border border-slate-300 px-4 py-3"
          />
        </div>
      </div>

      {validationError && (
        <p role="alert" className="text-red-600 text-sm mt-5">
          {validationError}
        </p>
      )}

      {mutation.isError && (
        <p role="alert" className="text-red-600 text-sm mt-5">
          Não foi possível salvar os dados. Verifique as informações.
        </p>
      )}

      {saved && (
        <p role="status" className="text-teal-700 text-sm mt-5">
          Dados salvos com sucesso!
        </p>
      )}

      <div className="flex justify-end mt-7">
        <button
          type="submit"
          disabled={mutation.isPending}
          className="bg-teal-600 text-white font-semibold px-6 py-3 rounded-lg hover:bg-teal-700 disabled:opacity-60 cursor-pointer"
        >
          {mutation.isPending ? "Salvando..." : "Salvar dados"}
        </button>
      </div>
    </form>
  );
}

export default function DataEntryPage() {
  const { restaurantId, periodId } = useParams();

  const restaurant = restaurantId ?? "";
  const period = periodId ?? "";

  const validIds =
    Number.isSafeInteger(Number(restaurant)) &&
    Number(restaurant) > 0 &&
    Number.isSafeInteger(Number(period)) &&
    Number(period) > 0;

  const channelsQuery = useQuery({
    queryKey: ["restaurant-channels", restaurant],

    queryFn: async () => {
      const response = await api.get<RestaurantChannel[]>(
        `/restaurants/${restaurant}/channels`,
      );

      return response.data;
    },

    enabled: validIds,
  });

  const metricsQuery = useQuery({
    queryKey: ["channel-metrics", restaurant, period],

    queryFn: async () => {
      const response = await api.get<ChannelMetric[]>(
        `/restaurants/${restaurant}/periods/${period}/channel-metrics`,
      );

      return response.data;
    },

    enabled: validIds,
  });

  if (!validIds) {
    return <p>Restaurante ou período inválido.</p>;
  }

  if (channelsQuery.isPending || metricsQuery.isPending) {
    return <p className="p-10">Carregando dados...</p>;
  }

  if (channelsQuery.isError || metricsQuery.isError) {
    return (
      <p className="p-10 text-red-600">
        Não foi possível carregar os dados do período.
      </p>
    );
  }

  const channels = channelsQuery.data.filter((channel) => channel.active);
  const metrics = metricsQuery.data;

  return (
    <div className="min-h-screen bg-slate-50">
      <main className="max-w-5xl mx-auto px-6 py-10">
        <Link
          to={`/restaurants/${restaurant}/periods/${period}/dashboard`}
          className="text-sm text-teal-700 hover:underline"
        >
          ← Voltar ao dashboard
        </Link>

        <div className="mt-8 mb-8">
          <h1 className="text-3xl font-bold text-slate-900">
            Inserir / editar dados
          </h1>

          <p className="text-slate-500 mt-2">
            Preencha os resultados financeiros de cada canal.
          </p>
        </div>

        <DataEntryNavigation />

        {channels.length === 0 && (
          <div className="bg-white border border-slate-200 rounded-xl p-6">
            <p className="text-slate-600">
              Este restaurante ainda não possui canais ativos. Um administrador
              precisa associar os canais ao restaurante.
            </p>
          </div>
        )}

        <div className="space-y-6">
          {channels.map((channel) => {
            const existingMetric = metrics.find(
              (metric) => metric.restaurantChannelId === channel.id,
            );

            return (
              <ChannelMetricsForm
                key={`${channel.id}-${existingMetric?.id ?? "new"}`}
                restaurantId={restaurant}
                periodId={period}
                channel={channel}
                existingMetric={existingMetric}
              />
            );
          })}
        </div>
      </main>
    </div>
  );
}
