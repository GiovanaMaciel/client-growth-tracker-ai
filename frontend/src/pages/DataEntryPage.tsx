import { useState } from "react";
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

interface IfoodFinancialDetails {
  servicesAndPromotions: number | null;
  adjustments: number | null;
  directReceipts: number | null;
  marketplaceTransfers: number | null;
}

interface ChannelMetric {
  id: number;
  restaurantChannelId: number;
  grossRevenue: number;
  orders: number;
  netRevenue: number;
  fees: number | null;
  promotions: number | null;
  ifoodDetails?: IfoodFinancialDetails | null;
}

interface FinancialPayload {
  grossRevenue: number;
  orders: number;
  netRevenue: number;
  fees: number | null;
  promotions: number | null;
  ifoodDetails?: IfoodFinancialDetails;
}

interface FormProps {
  restaurantId: string;
  periodId: string;
  channel: RestaurantChannel;
  existingMetric?: ChannelMetric;
}

// Money is calculated in integer cents on the frontend.
function parseCents(value: string): number | null {
  const match = value.trim().match(/^(-?)(\d+)(?:[.,](\d{1,2}))?$/);

  if (!match) return null;

  const whole = Number(match[2]);
  const decimal = Number((match[3] ?? "").padEnd(2, "0"));

  const cents = whole * 100 + decimal;

  if (!Number.isSafeInteger(cents)) {
    return null;
  }

  return match[1] === "-" ? -cents : cents;
}

function formatCents(value: number): string {
  const negative = value < 0 ? "-" : "";
  const absolute = Math.abs(value);

  return (
    negative +
    Math.floor(absolute / 100).toString() +
    "," +
    (absolute % 100).toString().padStart(2, "0")
  );
}

function initialMoney(value: number | null | undefined): string {
  if (value == null) return "";

  return value.toFixed(2).replace(".", ",");
}

function MoneyField({
  id,
  label,
  value,
  onChange,
  readOnly = false,
  required = false,
  allowNegative = false,
}: {
  id: string;
  label: string;
  value: string;
  onChange?: (value: string) => void;
  readOnly?: boolean;
  required?: boolean;
  allowNegative?: boolean;
}) {
  return (
    <div>
      <label
        htmlFor={id}
        className="mb-2 block text-sm font-medium text-slate-800"
      >
        {label}
      </label>

      <input
        id={id}
        type="text"
        inputMode="decimal"
        value={value}
        required={required}
        readOnly={readOnly}
        placeholder={readOnly ? "Calculado automaticamente" : "0,00"}
        onChange={(event) => onChange?.(event.target.value)}
        aria-describedby={readOnly ? `${id}-help` : undefined}
        className={
          "w-full rounded-lg border px-4 py-3 outline-none " +
          (readOnly
            ? "border-teal-200 bg-teal-50 text-teal-900 font-semibold"
            : "border-slate-300 bg-white focus:border-teal-500")
        }
      />

      {readOnly && (
        <p id={`${id}-help`} className="mt-1 text-xs text-teal-700">
          Calculado automaticamente
        </p>
      )}

      {allowNegative && !readOnly && (
        <p className="mt-1 text-xs text-slate-500">
          Aceita valores positivos e negativos.
        </p>
      )}
    </div>
  );
}

function ChannelMetricsForm({
  restaurantId,
  periodId,
  channel,
  existingMetric,
}: FormProps) {
  const queryClient = useQueryClient();
  const isIfood = channel.channelSlug === "ifood";

  const [grossRevenue, setGrossRevenue] = useState(
    initialMoney(existingMetric?.grossRevenue),
  );

  const [orders, setOrders] = useState(existingMetric?.orders.toString() ?? "");

  const [netRevenue, setNetRevenue] = useState(
    initialMoney(existingMetric?.netRevenue),
  );

  const [fees, setFees] = useState(initialMoney(existingMetric?.fees));

  const [promotions, setPromotions] = useState(
    initialMoney(existingMetric?.promotions),
  );

  const [services, setServices] = useState(
    initialMoney(existingMetric?.ifoodDetails?.servicesAndPromotions),
  );

  const [adjustments, setAdjustments] = useState(
    initialMoney(existingMetric?.ifoodDetails?.adjustments),
  );

  const [directReceipts, setDirectReceipts] = useState(
    initialMoney(existingMetric?.ifoodDetails?.directReceipts),
  );

  const [error, setError] = useState("");
  const [saved, setSaved] = useState(false);

  const endpoint = `/restaurants/${restaurantId}/periods/${periodId}/channel-metrics`;

  const grossCents = parseCents(grossRevenue);
  const feesCents = parseCents(fees);
  const servicesCents = parseCents(services);
  const adjustmentsCents = parseCents(adjustments);
  const directCents = parseCents(directReceipts);

  const canCalculate =
    grossCents !== null &&
    feesCents !== null &&
    servicesCents !== null &&
    adjustmentsCents !== null;

  const calculatedNet = canCalculate
    ? grossCents! - feesCents! - servicesCents! + adjustmentsCents!
    : null;

  const calculatedTransfers =
    calculatedNet !== null && directCents !== null
      ? calculatedNet - directCents
      : null;

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
          queryKey: ["dashboard"],
        }),
      ]);

      setSaved(true);
      setError("");
    },
  });

  function handleSubmit() {
    setSaved(false);
    setError("");

    const gross = parseCents(grossRevenue);
    const totalOrders = Number(orders);

    if (
      gross === null ||
      gross < 0 ||
      orders.trim() === "" ||
      !Number.isSafeInteger(totalOrders) ||
      totalOrders < 0 ||
      totalOrders > 2147483647
    ) {
      setError("Verifique o faturamento e a quantidade de pedidos.");
      return;
    }

    let payload: FinancialPayload;

    if (isIfood) {
      if (
        calculatedNet === null ||
        calculatedTransfers === null ||
        feesCents === null ||
        servicesCents === null ||
        adjustmentsCents === null ||
        directCents === null
      ) {
        setError(
          "Preencha todos os dados de origem do iFood para calcular os resultados.",
        );
        return;
      }

      if (
        feesCents < 0 ||
        servicesCents < 0 ||
        directCents < 0 ||
        calculatedNet < 0 ||
        calculatedTransfers < 0
      ) {
        setError(
          "Confira as despesas e os recebimentos. " +
            "Os resultados calculados não podem ser negativos.",
        );
        return;
      }

      payload = {
        grossRevenue: gross / 100,
        orders: totalOrders,
        netRevenue: calculatedNet / 100,
        fees: feesCents / 100,
        promotions: null,
        ifoodDetails: {
          servicesAndPromotions: servicesCents / 100,
          adjustments: adjustmentsCents / 100,
          directReceipts: directCents / 100,
          marketplaceTransfers: calculatedTransfers / 100,
        },
      };
    } else {
      const net = parseCents(netRevenue);
      const fee = fees.trim() === "" ? null : parseCents(fees);
      const promo = promotions.trim() === "" ? null : parseCents(promotions);

      if (
        net === null ||
        net < 0 ||
        net > gross ||
        (fees.trim() !== "" && fee === null) ||
        (promotions.trim() !== "" && promo === null) ||
        (fee !== null && fee < 0) ||
        (promo !== null && promo < 0)
      ) {
        setError("Verifique os valores financeiros informados.");
        return;
      }

      payload = {
        grossRevenue: gross / 100,
        orders: totalOrders,
        netRevenue: net / 100,
        fees: fee === null ? null : fee / 100,
        promotions: promo === null ? null : promo / 100,
      };
    }

    mutation.mutate(payload);
  }

  return (
    <form
      onSubmit={(event) => {
        event.preventDefault();
        handleSubmit();
      }}
      className="rounded-xl border border-slate-200 bg-white p-6 shadow-sm"
    >
      <div className="mb-6 flex items-center justify-between">
        <h2 className="text-xl font-semibold text-slate-900">
          {channel.channelName}
        </h2>

        <span className="rounded-full bg-slate-100 px-3 py-1 text-xs text-slate-600">
          {existingMetric ? "Cadastrado" : "Não preenchido"}
        </span>
      </div>

      <div className="grid gap-5 md:grid-cols-2">
        <MoneyField
          id={`gross-${channel.id}`}
          label={isIfood ? "Valor das vendas (R$)" : "Faturamento bruto (R$)"}
          value={grossRevenue}
          onChange={setGrossRevenue}
          required
        />

        <div>
          <label
            htmlFor={`orders-${channel.id}`}
            className="mb-2 block text-sm font-medium text-slate-800"
          >
            Total de pedidos
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

        <MoneyField
          id={`fees-${channel.id}`}
          label={isIfood ? "Taxas e comissões (R$)" : "Taxas (R$)"}
          value={fees}
          onChange={setFees}
          required={isIfood}
        />

        {isIfood ? (
          <>
            <MoneyField
              id={`services-${channel.id}`}
              label="Serviços e promoções (R$)"
              value={services}
              onChange={setServices}
              required
            />

            <MoneyField
              id={`adjustments-${channel.id}`}
              label="Ajustes (R$)"
              value={adjustments}
              onChange={setAdjustments}
              required
              allowNegative
            />

            <MoneyField
              id={`direct-${channel.id}`}
              label="Recebido diretamente pela loja (R$)"
              value={directReceipts}
              onChange={setDirectReceipts}
              required
            />

            <MoneyField
              id={`net-${channel.id}`}
              label="Faturamento após descontos (R$)"
              value={calculatedNet === null ? "" : formatCents(calculatedNet)}
              readOnly
            />

            <MoneyField
              id={`transfers-${channel.id}`}
              label="Valor estimado dos repasses (R$)"
              value={
                calculatedTransfers === null
                  ? ""
                  : formatCents(calculatedTransfers)
              }
              readOnly
            />
          </>
        ) : (
          <>
            <MoneyField
              id={`net-${channel.id}`}
              label="Líquido recebido (R$)"
              value={netRevenue}
              onChange={setNetRevenue}
              required
            />

            <MoneyField
              id={`promotions-${channel.id}`}
              label="Promoções (R$)"
              value={promotions}
              onChange={setPromotions}
            />
          </>
        )}
      </div>

      {isIfood && (
        <p className="mt-5 text-sm text-slate-500">
          Os resultados são estimados a partir dos dados informados. Confira-os
          com o demonstrativo do iFood antes de utilizar o relatório.
        </p>
      )}

      {error && (
        <p role="alert" className="mt-5 text-sm text-red-600">
          {error}
        </p>
      )}

      {mutation.isError && (
        <p role="alert" className="mt-5 text-sm text-red-600">
          Não foi possível salvar os dados financeiros.
        </p>
      )}

      {saved && (
        <p role="status" className="mt-5 text-sm text-teal-700">
          Dados salvos com sucesso!
        </p>
      )}

      <div className="mt-7 flex justify-end">
        <button
          type="submit"
          disabled={mutation.isPending}
          className="cursor-pointer rounded-lg bg-teal-600 px-6 py-3 font-semibold text-white hover:bg-teal-700 disabled:opacity-60"
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
    enabled: validIds,

    queryFn: async () => {
      const response = await api.get<RestaurantChannel[]>(
        `/restaurants/${restaurant}/channels`,
      );

      return response.data;
    },
  });

  const metricsQuery = useQuery({
    queryKey: ["channel-metrics", restaurant, period],
    enabled: validIds,

    queryFn: async () => {
      const response = await api.get<ChannelMetric[]>(
        `/restaurants/${restaurant}/periods/${period}/channel-metrics`,
      );

      return response.data;
    },
  });

  if (!validIds) {
    return <p className="p-10">Restaurante ou período inválido.</p>;
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
      <main className="mx-auto max-w-5xl px-6 py-10">
        <Link
          to={`/restaurants/${restaurant}/periods/${period}/dashboard`}
          className="text-sm text-teal-700 hover:underline"
        >
          ← Voltar ao dashboard
        </Link>

        <div className="mb-8 mt-8">
          <h1 className="text-3xl font-bold text-slate-900">
            Inserir / editar dados
          </h1>

          <p className="mt-2 text-slate-500">
            Preencha os dados financeiros de cada canal.
          </p>
        </div>

        <DataEntryNavigation />

        {channels.length === 0 && (
          <div className="rounded-xl border border-slate-200 bg-white p-6">
            Nenhum canal ativo cadastrado neste restaurante.
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
