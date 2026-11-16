import { useParams, Link } from "react-router";

import { useQuery } from "@tanstack/react-query";

import { api } from "../api/client";

interface ReportingPeriod {
  id: number;
  restaurantId: number;
  referenceMonth: string;
  openDays: number;
  notes: string | null;
}

function formatMonth(date: string): string {
  const [year, month] = date.split("-").map(Number);

  return new Intl.DateTimeFormat("pt-BR", {
    month: "long",
    year: "numeric",
  }).format(new Date(year, month - 1, 1));
}

export default function PeriodsPage() {
  const { restaurantId } = useParams();

  const id = Number(restaurantId);

  const validId = Number.isSafeInteger(id) && id > 0;

  const {
    data: periods,
    isPending,
    isError,
  } = useQuery({
    queryKey: ["periods", id],

    queryFn: async () => {
      const response = await api.get<ReportingPeriod[]>(
        `/restaurants/${id}/periods`,
      );

      return response.data;
    },

    enabled: validId,
  });

  if (!validId) {
    return <p>Restaurante inválido.</p>;
  }

  return (
    <div className="min-h-screen bg-slate-50">
      <main className="max-w-6xl mx-auto px-6 py-10">
        <Link
          to="/restaurants"
          className="text-sm text-teal-700 hover:underline"
        >
          ← Voltar aos restaurantes
        </Link>

        <div className="mt-8 mb-8">
          <h1 className="text-3xl font-bold text-slate-900">
            Períodos de análise
          </h1>

          <p className="text-slate-500 mt-2">
            Selecione um mês para visualizar o desempenho.
          </p>
        </div>

        {isPending && <p className="text-slate-500">Carregando períodos...</p>}

        {isError && (
          <p className="text-red-600">Não foi possível carregar os períodos.</p>
        )}

        {!isPending && !isError && periods?.length === 0 && (
          <div className="bg-white border border-slate-200 rounded-xl p-8">
            <p className="text-slate-500">
              Nenhum período cadastrado para este restaurante.
            </p>
          </div>
        )}

        <div className="grid gap-5 md:grid-cols-2 lg:grid-cols-3">
          {periods?.map((period) => (
            <Link
              key={period.id}
              to={`/restaurants/${id}/periods/${period.id}/dashboard`}
              className="bg-white border border-slate-200 rounded-xl p-6 hover:border-teal-300 hover:shadow-md transition-all"
            >
              <p className="text-xs font-semibold text-teal-600 uppercase tracking-wide">
                Relatório mensal
              </p>

              <h2 className="text-xl font-bold text-slate-900 mt-3 capitalize">
                {formatMonth(period.referenceMonth)}
              </h2>

              <p className="text-sm text-slate-500 mt-2">
                {period.openDays} dias de funcionamento
              </p>

              <div className="mt-6 text-sm font-semibold text-teal-700">
                Visualizar dashboard →
              </div>
            </Link>
          ))}
        </div>
      </main>
    </div>
  );
}
