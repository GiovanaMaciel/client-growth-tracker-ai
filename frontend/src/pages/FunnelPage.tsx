import { useEffect, useState } from "react";
import { Link, useParams } from "react-router";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";

import { api } from "../api/client";

interface ChannelMetric {
  id: number;
  channelName: string;
  orders: number;
}

interface FunnelStage {
  stageId: number;
  stageKey: string;
  stageName: string;
  stageOrder: number;
  valueSource: "MANUAL" | "ORDERS";
  value: number | null;
  conversionFromVisits: number | null;
}

interface FunnelResponse {
  channelMetricId: number;
  channelId: number;
  channelName: string;
  stages: FunnelStage[];
}

interface StageValueRequest {
  stageId: number;
  value: number;
}

interface UpdateFunnelRequest {
  stages: StageValueRequest[];
}

export default function FunnelPage() {
  const { restaurantId, periodId } = useParams();

  const queryClient = useQueryClient();

  const [selectedMetricId, setSelectedMetricId] = useState("");
  const [values, setValues] = useState<Record<number, string>>({});
  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");

  const validIds =
    Number.isSafeInteger(Number(restaurantId)) &&
    Number(restaurantId) > 0 &&
    Number.isSafeInteger(Number(periodId)) &&
    Number(periodId) > 0;

  const metricsQuery = useQuery({
    queryKey: ["channel-metrics", restaurantId, periodId],
    enabled: validIds,

    queryFn: async () => {
      const response = await api.get<ChannelMetric[]>(
        `/restaurants/${restaurantId}/periods/${periodId}/channel-metrics`,
      );

      return response.data;
    },
  });

  useEffect(() => {
    const metrics = metricsQuery.data;

    if (!metrics) return;

    setSelectedMetricId((current) => {
      const ids = metrics.map((metric) => String(metric.id));

      if (ids.includes(current)) return current;

      return ids[0] ?? "";
    });
  }, [metricsQuery.data]);

  const funnelQuery = useQuery({
    queryKey: ["funnel", restaurantId, periodId, selectedMetricId],

    enabled: validIds && selectedMetricId !== "",

    queryFn: async () => {
      const response = await api.get<FunnelResponse>(
        `/restaurants/${restaurantId}/periods/${periodId}` +
          `/channel-metrics/${selectedMetricId}/funnel`,
      );

      return response.data;
    },
  });

  useEffect(() => {
    if (!funnelQuery.data) return;

    const initialValues: Record<number, string> = {};

    for (const stage of funnelQuery.data.stages) {
      if (stage.valueSource === "MANUAL") {
        initialValues[stage.stageId] =
          stage.value === null ? "" : String(stage.value);
      }
    }

    setValues(initialValues);
    setError("");
    setSuccess("");
  }, [funnelQuery.data]);

  const saveMutation = useMutation({
    mutationFn: async (request: UpdateFunnelRequest) => {
      const response = await api.put<FunnelResponse>(
        `/restaurants/${restaurantId}/periods/${periodId}` +
          `/channel-metrics/${selectedMetricId}/funnel`,
        request,
      );

      return response.data;
    },

    onSuccess: async () => {
      await Promise.all([
        queryClient.invalidateQueries({
          queryKey: ["funnel", restaurantId, periodId, selectedMetricId],
        }),

        queryClient.invalidateQueries({
          queryKey: ["dashboard"],
        }),
      ]);

      setError("");
      setSuccess("Funil atualizado com sucesso.");
    },
  });

  function handleSave() {
    setError("");
    setSuccess("");

    const stages = funnelQuery.data?.stages;

    if (!stages) return;

    const updates: StageValueRequest[] = [];

    for (const stage of stages) {
      if (stage.valueSource !== "MANUAL") {
        continue;
      }

      const rawValue = (values[stage.stageId] ?? "").trim();

      if (rawValue === "") {
        if (stage.value !== null) {
          setError(
            `A etapa "${stage.stageName}" já possui um valor. ` +
              "O backend atual não permite apagá-lo deixando o campo vazio.",
          );
          return;
        }

        continue;
      }

      const numericValue = Number(rawValue);

      if (!Number.isSafeInteger(numericValue) || numericValue < 0) {
        setError(
          `Informe um número inteiro não negativo para "${stage.stageName}".`,
        );
        return;
      }

      updates.push({
        stageId: stage.stageId,
        value: numericValue,
      });
    }

    if (updates.length === 0) {
      setError("Preencha pelo menos uma etapa antes de salvar.");
      return;
    }

    saveMutation.mutate({ stages: updates });
  }

  const stages = funnelQuery.data?.stages ?? [];

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
          <h1 className="text-3xl font-bold text-slate-900">Funil de vendas</h1>

          <p className="mt-2 text-slate-500">
            Preencha as etapas de conversão de cada marketplace.
          </p>
        </div>

        <div className="rounded-xl border border-slate-200 bg-white p-6 shadow-sm">
          {metricsQuery.isPending && (
            <p className="text-slate-500">Carregando canais...</p>
          )}

          {metricsQuery.isError && (
            <p className="text-red-600">
              Não foi possível carregar as métricas dos canais.
            </p>
          )}

          {metricsQuery.data?.length === 0 && (
            <p className="text-slate-600">
              Nenhum canal possui métricas financeiras cadastradas neste
              período. Preencha primeiro os dados financeiros.
            </p>
          )}

          {(metricsQuery.data?.length ?? 0) > 0 && (
            <>
              <label
                htmlFor="metric"
                className="mb-2 block text-sm font-semibold text-slate-700"
              >
                Selecione o canal
              </label>

              <select
                id="metric"
                value={selectedMetricId}
                onChange={(event) => {
                  setSelectedMetricId(event.target.value);
                  setError("");
                  setSuccess("");
                }}
                className="w-full rounded-lg border border-slate-300 p-3"
              >
                {metricsQuery.data?.map((metric) => (
                  <option key={metric.id} value={metric.id}>
                    {metric.channelName}
                  </option>
                ))}
              </select>

              {funnelQuery.isPending && (
                <p className="mt-6 text-slate-500">Carregando funil...</p>
              )}

              {funnelQuery.isError && (
                <p className="mt-6 text-red-600">
                  Não foi possível carregar o funil selecionado.
                </p>
              )}

              {funnelQuery.isSuccess && stages.length === 0 && (
                <p className="mt-6 text-slate-600">
                  Este canal não possui etapas de funil configuradas.
                </p>
              )}

              {funnelQuery.isSuccess && stages.length > 0 && (
                <form
                  className="mt-8 space-y-5"
                  onSubmit={(event) => {
                    event.preventDefault();
                    handleSave();
                  }}
                >
                  {stages.map((stage) => (
                    <div
                      key={stage.stageId}
                      className="grid gap-3 border-b border-slate-100 pb-5 sm:grid-cols-[1fr_180px]"
                    >
                      <div>
                        <p className="font-semibold text-slate-800">
                          {stage.stageName}
                        </p>

                        <p className="mt-1 text-xs text-slate-500">
                          {stage.valueSource === "ORDERS"
                            ? "Valor automático das métricas financeiras"
                            : "Preenchimento manual"}
                        </p>

                        {stage.conversionFromVisits !== null && (
                          <p className="mt-2 text-sm text-teal-700">
                            Conversão desde visitas:{" "}
                            {stage.conversionFromVisits.toFixed(2)}%
                          </p>
                        )}
                      </div>

                      {stage.valueSource === "ORDERS" ? (
                        <div className="rounded-lg bg-slate-100 p-3 text-center font-semibold text-slate-700">
                          {stage.value ?? "—"}
                        </div>
                      ) : (
                        <input
                          type="number"
                          min="0"
                          step="1"
                          value={values[stage.stageId] ?? ""}
                          onChange={(event) => {
                            setValues((previous) => ({
                              ...previous,
                              [stage.stageId]: event.target.value,
                            }));
                            setSuccess("");
                          }}
                          placeholder="Não informado"
                          className="w-full rounded-lg border border-slate-300 p-3"
                        />
                      )}
                    </div>
                  ))}

                  {error && (
                    <p role="alert" className="text-sm text-red-600">
                      {error}
                    </p>
                  )}

                  {saveMutation.isError && (
                    <p role="alert" className="text-sm text-red-600">
                      Não foi possível salvar o funil. Confira os valores e
                      tente novamente.
                    </p>
                  )}

                  {success && (
                    <p role="status" className="text-sm text-emerald-700">
                      {success}
                    </p>
                  )}

                  <button
                    type="submit"
                    disabled={saveMutation.isPending}
                    className="rounded-lg bg-teal-600 px-6 py-3 font-semibold text-white hover:bg-teal-700 disabled:opacity-50"
                  >
                    {saveMutation.isPending ? "Salvando..." : "Salvar funil"}
                  </button>
                </form>
              )}
            </>
          )}
        </div>

        <Link
          to={`/restaurants/${restaurantId}/periods/${periodId}/dashboard`}
          className="mt-6 inline-block text-sm font-medium text-teal-700 hover:underline"
        >
          Visualizar dashboard →
        </Link>
      </div>
    </main>
  );
}
