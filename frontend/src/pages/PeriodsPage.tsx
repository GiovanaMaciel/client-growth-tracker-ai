import { useState, type SubmitEvent } from "react";
import { Link, useParams } from "react-router";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import axios from "axios";
import { api } from "../api/client";
import { getSession } from "../auth/session";

type ReportingPeriod = {
  id: number;
  restaurantId: number;
  referenceMonth: string;
  openDays: number;
  notes: string | null;
};

type Restaurant = {
  id: number;
  name: string;
};

type CreatePeriodRequest = {
  referenceMonth: string;
  openDays: number;
  notes: string | null;
};

function formatMonth(referenceMonth: string): string {
  const [year, month] = referenceMonth.split("-").map(Number);

  return new Intl.DateTimeFormat("pt-BR", {
    month: "long",
    year: "numeric",
  }).format(new Date(year, month - 1, 1));
}

function getDaysInMonth(month: string): number {
  const [year, monthNumber] = month.split("-").map(Number);
  return new Date(year, monthNumber, 0).getDate();
}

function getErrorMessage(error: unknown): string {
  if (axios.isAxiosError(error)) {
    const message = error.response?.data?.message;

    if (typeof message === "string") {
      return message;
    }
  }

  return "Não foi possível concluir a operação.";
}

export default function PeriodsPage() {
  const { restaurantId } = useParams();
  const id = Number(restaurantId);

  const queryClient = useQueryClient();
  const session = getSession();

  const [month, setMonth] = useState("");
  const [openDays, setOpenDays] = useState("26");
  const [notes, setNotes] = useState("");
  const [formError, setFormError] = useState("");
  const [success, setSuccess] = useState("");

  const validRestaurantId = Number.isInteger(id) && id > 0;
  const isAdmin = session?.role === "AGENCY_ADMIN";

  const restaurantQuery = useQuery({
    queryKey: ["restaurant", id],
    queryFn: async () => {
      const response = await api.get<Restaurant>(`/restaurants/${id}`);
      return response.data;
    },
    enabled: validRestaurantId,
  });

  const periodsQuery = useQuery({
    queryKey: ["periods", id],
    queryFn: async () => {
      const response = await api.get<ReportingPeriod[]>(
        `/restaurants/${id}/periods`,
      );

      return response.data;
    },
    enabled: validRestaurantId,
  });

  const createMutation = useMutation({
    mutationFn: async (data: CreatePeriodRequest) => {
      const response = await api.post<ReportingPeriod>(
        `/restaurants/${id}/periods`,
        data,
      );

      return response.data;
    },

    onSuccess: async () => {
      await queryClient.invalidateQueries({
        queryKey: ["periods", id],
      });

      setSuccess("Período criado com sucesso!");
      setFormError("");
      setMonth("");
      setOpenDays("26");
      setNotes("");
    },

    onError: (error: unknown) => {
      setSuccess("");
      setFormError(getErrorMessage(error));
    },
  });

  function handleSubmit(event: SubmitEvent<HTMLFormElement>) {
    event.preventDefault();

    setFormError("");
    setSuccess("");

    if (!month || !/^\d{4}-\d{2}$/.test(month)) {
      setFormError("Selecione um mês válido.");
      return;
    }

    const days = Number(openDays);
    const maximumDays = getDaysInMonth(month);

    if (!Number.isInteger(days) || days < 1 || days > maximumDays) {
      setFormError(`Informe entre 1 e ${maximumDays} dias de funcionamento.`);
      return;
    }

    const referenceMonth = `${month}-01`;

    const alreadyExists = periodsQuery.data?.some(
      (period) => period.referenceMonth === referenceMonth,
    );

    if (alreadyExists) {
      setFormError("Esse período já está cadastrado.");
      return;
    }

    createMutation.mutate({
      referenceMonth,
      openDays: days,
      notes: notes.trim() || null,
    });
  }

  if (!validRestaurantId) {
    return <p>Restaurante inválido.</p>;
  }

  return (
    <div className="min-h-screen bg-slate-50">
      <main className="mx-auto max-w-6xl space-y-8 px-6 py-10">
        <header className="space-y-5">
          <Link
            to="/restaurants"
            className="text-sm font-medium text-teal-700 hover:underline"
          >
            ← Voltar aos restaurantes
          </Link>

          <div className="flex flex-wrap items-end justify-between gap-5">
            <div>
              <h1 className="text-3xl font-bold text-slate-900">
                Períodos de análise
              </h1>
              <p className="mt-2 text-slate-500">
                {restaurantQuery.data?.name ?? `Restaurante #${id}`}
              </p>
              <p className="mt-1 text-sm text-slate-400">
                Organize as informações mensais de desempenho.
              </p>
            </div>

            {isAdmin && (
              <Link
                to={`/restaurants/${id}/channels`}
                className="inline-flex rounded-lg border border-teal-200 bg-white px-5 py-3 text-sm font-semibold text-teal-700 transition-colors hover:bg-teal-50"
              >
                Gerenciar canais
              </Link>
            )}
          </div>
        </header>

        <section className="rounded-xl border border-slate-200 bg-white p-6 shadow-sm">
          <h2 className="text-xl font-semibold text-slate-900">
            Criar novo período
          </h2>
          <p className="mt-1 text-sm text-slate-500">
            Selecione o mês e informe quantos dias o restaurante funcionou.
          </p>

          <form onSubmit={handleSubmit} className="mt-6 space-y-5">
            <div className="grid gap-5 md:grid-cols-2">
              <label className="block space-y-2">
                <span className="text-sm font-medium text-slate-700">
                  Mês de referência
                </span>
                <input
                  type="month"
                  value={month}
                  onChange={(event) => setMonth(event.target.value)}
                  required
                  className="w-full rounded-lg border border-slate-300 bg-white px-4 py-3 text-slate-900 outline-none focus:border-teal-500 focus:ring-2 focus:ring-teal-100"
                />
              </label>

              <label className="block space-y-2">
                <span className="text-sm font-medium text-slate-700">
                  Dias de funcionamento
                </span>
                <input
                  type="number"
                  min="1"
                  max={month ? getDaysInMonth(month) : 31}
                  value={openDays}
                  onChange={(event) => setOpenDays(event.target.value)}
                  required
                  className="w-full rounded-lg border border-slate-300 bg-white px-4 py-3 text-slate-900 outline-none focus:border-teal-500 focus:ring-2 focus:ring-teal-100"
                />
              </label>
            </div>

            <label className="block space-y-2">
              <span className="text-sm font-medium text-slate-700">
                Observações (opcional)
              </span>
              <textarea
                value={notes}
                onChange={(event) => setNotes(event.target.value)}
                rows={3}
                className="w-full rounded-lg border border-slate-300 bg-white px-4 py-3 text-slate-900 outline-none focus:border-teal-500 focus:ring-2 focus:ring-teal-100"
                placeholder="Ex.: Restaurante fechado para reforma por 3 dias."
              />
            </label>

            {formError && (
              <p role="alert" className="text-sm text-red-600">
                {formError}
              </p>
            )}
            {success && (
              <p role="status" className="text-sm text-teal-700">
                {success}
              </p>
            )}

            <button
              type="submit"
              disabled={createMutation.isPending}
              className="rounded-lg bg-teal-600 px-5 py-3 text-sm font-semibold text-white transition-colors hover:bg-teal-700 disabled:cursor-not-allowed disabled:opacity-50 cursor-pointer"
            >
              {createMutation.isPending ? "Salvando..." : "Criar período"}
            </button>
          </form>
        </section>

        <section>
          <div className="mb-5 flex flex-wrap items-baseline justify-between gap-3">
            <h2 className="text-xl font-semibold text-slate-900">
              Períodos cadastrados
            </h2>
            {periodsQuery.isSuccess && (
              <span className="text-sm text-slate-500">
                {periodsQuery.data.length}{" "}
                {periodsQuery.data.length === 1 ? "período" : "períodos"}
              </span>
            )}
          </div>

          {periodsQuery.isPending && (
            <p className="text-slate-500">Carregando períodos...</p>
          )}
          {periodsQuery.isError && (
            <p
              role="alert"
              className="rounded-lg border border-red-200 bg-red-50 p-4 text-red-700"
            >
              Não foi possível carregar os períodos.
            </p>
          )}
          {periodsQuery.isSuccess && periodsQuery.data.length === 0 && (
            <p className="rounded-xl border border-slate-200 bg-white p-6 text-slate-500">
              Nenhum período cadastrado ainda.
            </p>
          )}

          <div className="grid gap-5 md:grid-cols-2">
            {periodsQuery.data?.map((period) => (
              <article
                key={period.id}
                className="rounded-xl border border-slate-200 bg-white p-6 shadow-sm transition-all hover:border-teal-300 hover:shadow-md"
              >
                <h3 className="text-lg font-semibold capitalize text-slate-900">
                  {formatMonth(period.referenceMonth)}
                </h3>
                <p className="mt-2 text-sm text-slate-600">
                  {period.openDays} dias de funcionamento
                </p>
                {period.notes && (
                  <p className="mt-2 text-sm text-slate-500">{period.notes}</p>
                )}

                <div className="mt-6 flex flex-wrap items-center gap-3">
                  <Link
                    to={`/restaurants/${id}/periods/${period.id}/dashboard`}
                    className="inline-flex rounded-lg bg-teal-600 px-4 py-2.5 text-sm font-semibold text-white transition-colors hover:bg-teal-700"
                  >
                    Ver dashboard
                  </Link>
                  <Link
                    to={`/restaurants/${id}/periods/${period.id}/data`}
                    className="inline-flex rounded-lg border border-teal-200 bg-white px-4 py-2.5 text-sm font-semibold text-teal-700 transition-colors hover:bg-teal-50"
                  >
                    Inserir dados
                  </Link>
                </div>
              </article>
            ))}
          </div>
        </section>
      </main>
    </div>
  );
}
