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
    <main className="mx-auto max-w-5xl space-y-8 p-6">
      <header className="space-y-3">
        <Link
          to="/restaurants"
          className="text-sm text-blue-600 hover:underline"
        >
          Voltar aos restaurantes
        </Link>

        <h1 className="text-3xl font-bold">Períodos de análise</h1>

        <p className="text-gray-600">Restaurante #{id}</p>

        {isAdmin && (
          <Link
            to={`/restaurants/${id}/channels`}
            className="inline-block rounded-lg bg-gray-900 px-4 py-2 text-white"
          >
            Gerenciar canais
          </Link>
        )}
      </header>

      <section className="rounded-xl border bg-white p-6 shadow-sm">
        <h2 className="mb-5 text-xl font-semibold">Criar novo período</h2>

        <form onSubmit={handleSubmit} className="space-y-4">
          <div className="grid gap-4 md:grid-cols-2">
            <label className="block space-y-2">
              <span className="font-medium">Mês de referência</span>

              <input
                type="month"
                value={month}
                onChange={(event) => setMonth(event.target.value)}
                required
                className="w-full rounded-lg border p-3"
              />
            </label>

            <label className="block space-y-2">
              <span className="font-medium">Dias de funcionamento</span>

              <input
                type="number"
                min="1"
                max={month ? getDaysInMonth(month) : 31}
                value={openDays}
                onChange={(event) => setOpenDays(event.target.value)}
                required
                className="w-full rounded-lg border p-3"
              />
            </label>
          </div>

          <label className="block space-y-2">
            <span className="font-medium">Observações</span>

            <textarea
              value={notes}
              onChange={(event) => setNotes(event.target.value)}
              rows={3}
              className="w-full rounded-lg border p-3"
              placeholder="Ex.: Restaurante fechado para reforma por 3 dias."
            />
          </label>

          {formError && (
            <p role="alert" className="text-sm text-red-600">
              {formError}
            </p>
          )}

          {success && (
            <p role="status" className="text-sm text-green-600">
              {success}
            </p>
          )}

          <button
            type="submit"
            disabled={createMutation.isPending}
            className="rounded-lg bg-blue-600 px-5 py-3 font-medium text-white disabled:opacity-50"
          >
            {createMutation.isPending ? "Salvando..." : "Criar período"}
          </button>
        </form>
      </section>

      <section>
        <h2 className="mb-4 text-xl font-semibold">Períodos cadastrados</h2>

        {periodsQuery.isPending && <p>Carregando períodos...</p>}

        {periodsQuery.isError && (
          <p role="alert" className="text-red-600">
            Não foi possível carregar os períodos.
          </p>
        )}

        {periodsQuery.isSuccess && periodsQuery.data.length === 0 && (
          <p>Nenhum período cadastrado ainda.</p>
        )}

        <div className="grid gap-4 md:grid-cols-2">
          {periodsQuery.data?.map((period) => (
            <article
              key={period.id}
              className="rounded-xl border bg-white p-5 shadow-sm"
            >
              <h3 className="text-lg font-semibold capitalize">
                {formatMonth(period.referenceMonth)}
              </h3>

              <p className="mt-2 text-gray-600">
                {period.openDays} dias de funcionamento
              </p>

              {period.notes && (
                <p className="mt-2 text-sm text-gray-500">{period.notes}</p>
              )}

              <div className="mt-4 flex flex-wrap gap-4">
                <Link
                  to={`/restaurants/${id}/periods/${period.id}/dashboard`}
                  className="text-blue-600 hover:underline"
                >
                  Ver dashboard
                </Link>

                <Link
                  to={`/restaurants/${id}/periods/${period.id}/data`}
                  className="text-blue-600 hover:underline"
                >
                  Inserir dados
                </Link>
              </div>
            </article>
          ))}
        </div>
      </section>
    </main>
  );
}
