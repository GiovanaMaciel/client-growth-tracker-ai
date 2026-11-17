import { useEffect, useState } from "react";
import { Link, useParams } from "react-router";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import axios from "axios";

import { api } from "../api/client";

interface PaidTrafficResponse {
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
  costPerVisit: number;
  averageTicket: number;
  frequency: number;
  visitToCartConversion: number;
  visitToPurchaseConversion: number;
  cartToPurchaseConversion: number;
}

interface PaidTrafficPayload {
  investment: number;
  reach: number;
  impressions: number;
  visits: number;
  carts: number;
  purchases: number;
  attributedRevenue: number;
}

type FormValues = Record<keyof PaidTrafficPayload, string>;

const emptyForm: FormValues = {
  investment: "",
  reach: "",
  impressions: "",
  visits: "",
  carts: "",
  purchases: "",
  attributedRevenue: "",
};

const fields: {
  key: keyof FormValues;
  label: string;
  kind: "money" | "count";
}[] = [
  { key: "investment", label: "Investimento (R$)", kind: "money" },
  { key: "reach", label: "Alcance", kind: "count" },
  { key: "impressions", label: "Impressões", kind: "count" },
  { key: "visits", label: "Visitas", kind: "count" },
  { key: "carts", label: "Carrinhos", kind: "count" },
  { key: "purchases", label: "Compras atribuídas", kind: "count" },
  {
    key: "attributedRevenue",
    label: "Receita atribuída (R$)",
    kind: "money",
  },
];

const currencyFormatter = new Intl.NumberFormat("pt-BR", {
  style: "currency",
  currency: "BRL",
});

function formatMoney(value: number) {
  return currencyFormatter.format(value);
}

export default function PaidTrafficPage() {
  const { restaurantId, periodId } = useParams();

  const queryClient = useQueryClient();

  const [form, setForm] = useState<FormValues>({ ...emptyForm });
  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");

  const validIds =
    Number.isSafeInteger(Number(restaurantId)) &&
    Number(restaurantId) > 0 &&
    Number.isSafeInteger(Number(periodId)) &&
    Number(periodId) > 0;

  const paidTrafficQuery = useQuery<PaidTrafficResponse | null>({
    queryKey: ["paid-traffic", restaurantId, periodId],
    enabled: validIds,

    queryFn: async () => {
      try {
        const response = await api.get<PaidTrafficResponse>(
          `/restaurants/${restaurantId}/periods/${periodId}/paid-traffic`,
        );

        return response.data;
      } catch (err) {
        if (axios.isAxiosError(err) && err.response?.status === 404) {
          return null;
        }

        throw err;
      }
    },
  });

  useEffect(() => {
    if (!paidTrafficQuery.isSuccess) return;

    const data = paidTrafficQuery.data;

    if (!data) {
      setForm({ ...emptyForm });
      return;
    }

    setForm({
      investment: String(data.investment),
      reach: String(data.reach),
      impressions: String(data.impressions),
      visits: String(data.visits),
      carts: String(data.carts),
      purchases: String(data.purchases),
      attributedRevenue: String(data.attributedRevenue),
    });
  }, [paidTrafficQuery.isSuccess, paidTrafficQuery.data]);

  const saveMutation = useMutation({
    mutationFn: async (payload: PaidTrafficPayload) => {
      const response = await api.put<PaidTrafficResponse>(
        `/restaurants/${restaurantId}/periods/${periodId}/paid-traffic`,
        payload,
      );

      return response.data;
    },

    onSuccess: async () => {
      await Promise.all([
        queryClient.invalidateQueries({
          queryKey: ["paid-traffic", restaurantId, periodId],
        }),

        queryClient.invalidateQueries({
          queryKey: ["dashboard"],
        }),
      ]);

      setError("");
      setSuccess("Métricas de tráfego pago salvas com sucesso.");
    },
  });

  function handleSave() {
    setError("");
    setSuccess("");

    for (const field of fields) {
      const rawValue = form[field.key].trim();

      if (rawValue === "") {
        setError(`Preencha o campo "${field.label}".`);
        return;
      }

      const value = Number(rawValue);

      if (field.kind === "count") {
        if (!Number.isSafeInteger(value) || value < 0 || value > 2147483647) {
          setError(`"${field.label}" deve ser um número inteiro não negativo.`);
          return;
        }
      } else {
        if (
          !/^\d+(\.\d{1,2})?$/.test(rawValue) ||
          !Number.isFinite(value) ||
          value > 9999999999.99
        ) {
          setError(
            `"${field.label}" deve ser um valor monetário válido, ` +
              "usando ponto decimal e no máximo duas casas.",
          );
          return;
        }
      }
    }

    const payload: PaidTrafficPayload = {
      investment: Number(form.investment),
      reach: Number(form.reach),
      impressions: Number(form.impressions),
      visits: Number(form.visits),
      carts: Number(form.carts),
      purchases: Number(form.purchases),
      attributedRevenue: Number(form.attributedRevenue),
    };

    saveMutation.mutate(payload);
  }

  const data = paidTrafficQuery.data;

  return (
    <main className="min-h-screen bg-slate-50 px-5 py-10">
      <div className="mx-auto max-w-4xl">
        <Link
          to={`/restaurants/${restaurantId}/periods/${periodId}/data`}
          className="text-sm text-teal-700 hover:underline"
        >
          ← Voltar para entrada de dados
        </Link>

        <div className="mt-6 mb-8">
          <h1 className="text-3xl font-bold text-slate-900">Tráfego pago</h1>

          <p className="mt-2 text-slate-500">
            Registre o investimento e os resultados de campanhas do período
            selecionado.
          </p>
        </div>

        <div className="rounded-xl border border-slate-200 bg-white p-6 shadow-sm">
          {paidTrafficQuery.isPending && (
            <p className="text-slate-500">Carregando métricas...</p>
          )}

          {paidTrafficQuery.isError && (
            <p role="alert" className="text-red-600">
              Não foi possível consultar o tráfego pago.
            </p>
          )}

          {paidTrafficQuery.isSuccess && (
            <>
              {data === null && (
                <div className="mb-6 rounded-lg bg-amber-50 p-4 text-sm text-amber-800">
                  Ainda não existem dados de tráfego pago para este período.
                </div>
              )}

              <form
                onSubmit={(event) => {
                  event.preventDefault();
                  handleSave();
                }}
              >
                <div className="grid gap-5 sm:grid-cols-2">
                  {fields.map((field) => (
                    <div key={field.key}>
                      <label
                        htmlFor={field.key}
                        className="mb-2 block text-sm font-medium text-slate-700"
                      >
                        {field.label}
                      </label>

                      <input
                        id={field.key}
                        type="text"
                        inputMode={
                          field.kind === "money" ? "decimal" : "numeric"
                        }
                        value={form[field.key]}
                        placeholder={field.kind === "money" ? "0.00" : "0"}
                        onChange={(event) => {
                          setForm((previous) => ({
                            ...previous,
                            [field.key]: event.target.value,
                          }));
                          setSuccess("");
                        }}
                        className="w-full rounded-lg border border-slate-300 p-3 outline-none focus:border-teal-500"
                      />
                    </div>
                  ))}
                </div>

                <p className="mt-4 text-xs text-slate-500">
                  Valores monetários devem utilizar ponto decimal, por exemplo:
                  212.94. Zero significa um valor confirmado como zero.
                </p>

                {error && (
                  <p role="alert" className="mt-5 text-sm text-red-600">
                    {error}
                  </p>
                )}

                {saveMutation.isError && (
                  <p role="alert" className="mt-5 text-sm text-red-600">
                    Não foi possível salvar as métricas. Confira os valores e as
                    permissões.
                  </p>
                )}

                {success && (
                  <p role="status" className="mt-5 text-sm text-emerald-700">
                    {success}
                  </p>
                )}

                <button
                  type="submit"
                  disabled={saveMutation.isPending}
                  className="mt-6 rounded-lg bg-teal-600 px-6 py-3 font-semibold text-white hover:bg-teal-700 disabled:opacity-50"
                >
                  {saveMutation.isPending
                    ? "Salvando..."
                    : "Salvar tráfego pago"}
                </button>
              </form>
            </>
          )}
        </div>

        {paidTrafficQuery.isSuccess && data != null && (
          <section className="mt-8">
            <h2 className="mb-4 text-xl font-bold text-slate-900">
              Indicadores calculados
            </h2>

            <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
              <MetricCard
                label="ROAS"
                value={data.investment === 0 ? "—" : `${data.roas.toFixed(2)}x`}
              />

              <MetricCard
                label="CPA"
                value={data.purchases === 0 ? "—" : formatMoney(data.cpa)}
              />

              <MetricCard
                label="Custo por visita"
                value={data.visits === 0 ? "—" : formatMoney(data.costPerVisit)}
              />

              <MetricCard
                label="Ticket médio atribuído"
                value={
                  data.purchases === 0 ? "—" : formatMoney(data.averageTicket)
                }
              />

              <MetricCard
                label="Frequência"
                value={data.reach === 0 ? "—" : data.frequency.toFixed(2)}
              />

              <MetricCard
                label="Visitas → compras"
                value={
                  data.visits === 0
                    ? "—"
                    : `${data.visitToPurchaseConversion.toFixed(2)}%`
                }
              />
            </div>
          </section>
        )}

        <Link
          to={`/restaurants/${restaurantId}/periods/${periodId}/dashboard`}
          className="mt-8 inline-block text-sm font-medium text-teal-700 hover:underline"
        >
          Visualizar dashboard →
        </Link>
      </div>
    </main>
  );
}

function MetricCard({ label, value }: { label: string; value: string }) {
  return (
    <div className="rounded-xl border border-slate-200 bg-white p-5">
      <p className="text-sm text-slate-500">{label}</p>

      <p className="mt-2 text-2xl font-bold text-slate-900">{value}</p>
    </div>
  );
}
