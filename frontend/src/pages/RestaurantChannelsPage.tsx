import { useState, type SubmitEvent } from "react";
import { Link, useParams } from "react-router";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import axios from "axios";

import { api } from "../api/client";
import { getSession } from "../auth/session";

type Channel = {
  id: number;
  name: string;
  slug: string;
};

type Restaurant = {
  id: number;
  name: string;
};

type RestaurantChannel = {
  id: number;
  restaurantId: number;
  channelId: number;
  channelName: string;
  channelSlug: string;
  externalStoreId: string | null;
  active: boolean;
};

type CreateRestaurantChannelRequest = {
  channelId: number;
  externalStoreId: string | null;
};

type UpdateRestaurantChannelRequest = {
  externalStoreId: string | null;
  active: boolean;
};

function getErrorMessage(error: unknown): string {
  if (axios.isAxiosError(error)) {
    const message = error.response?.data?.message;

    if (typeof message === "string") {
      return message;
    }

    if (error.response?.status === 409) {
      return "Este canal já está associado ao restaurante.";
    }

    if (error.response?.status === 403) {
      return "Você não tem permissão para realizar esta operação.";
    }
  }

  return "Não foi possível concluir a operação.";
}

type ChannelCardProps = {
  restaurantId: number;
  channel: RestaurantChannel;
};

function ChannelCard({ restaurantId, channel }: ChannelCardProps) {
  const queryClient = useQueryClient();

  const [externalStoreId, setExternalStoreId] = useState(
    channel.externalStoreId ?? "",
  );

  const [active, setActive] = useState(channel.active);
  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");

  const updateMutation = useMutation({
    mutationFn: async (data: UpdateRestaurantChannelRequest) => {
      const response = await api.put<RestaurantChannel>(
        `/restaurants/${restaurantId}/channels/${channel.id}`,
        data,
      );

      return response.data;
    },

    onSuccess: async () => {
      await Promise.all([
        queryClient.invalidateQueries({
          queryKey: ["restaurant-channels", restaurantId],
        }),

        queryClient.invalidateQueries({
          queryKey: ["dashboard", restaurantId],
        }),
      ]);

      setError("");
      setSuccess("Canal atualizado com sucesso!");
    },

    onError: (error: unknown) => {
      setSuccess("");
      setError(getErrorMessage(error));
    },
  });

  function handleSubmit(event: SubmitEvent<HTMLFormElement>) {
    event.preventDefault();

    setError("");
    setSuccess("");

    updateMutation.mutate({
      externalStoreId: externalStoreId.trim() || null,
      active,
    });
  }

  return (
    <article className="rounded-xl border border-slate-200 bg-white p-6 shadow-sm transition-all hover:border-teal-300 hover:shadow-md">
      <div className="mb-4 flex items-center justify-between gap-4">
        <h3 className="text-lg font-semibold text-slate-900">
          {channel.channelName}
        </h3>

        <span
          className={`rounded-full px-3 py-1 text-xs font-medium ${
            active ? "bg-teal-50 text-teal-700" : "bg-slate-100 text-slate-600"
          }`}
        >
          {active ? "Ativo" : "Inativo"}
        </span>
      </div>

      <form onSubmit={handleSubmit} className="space-y-4">
        <label className="block space-y-2">
          <span className="text-sm font-medium text-slate-700">
            Identificador externo da loja
          </span>

          <input
            type="text"
            value={externalStoreId}
            onChange={(event) => setExternalStoreId(event.target.value)}
            placeholder="Opcional"
            className="w-full rounded-lg border border-slate-300 bg-white px-4 py-3 text-slate-900 outline-none focus:border-teal-500 focus:ring-2 focus:ring-teal-100"
          />
        </label>

        <label className="flex items-center gap-3">
          <input
            type="checkbox"
            checked={active}
            onChange={(event) => setActive(event.target.checked)}
          />

          <span className="text-sm text-slate-700">Canal ativo</span>
        </label>

        {error && (
          <p role="alert" className="text-sm text-red-600">
            {error}
          </p>
        )}

        {success && (
          <p role="status" className="text-sm text-green-600">
            {success}
          </p>
        )}

        <button
          type="submit"
          disabled={updateMutation.isPending}
          className="rounded-lg bg-teal-600 px-5 py-3 text-sm font-semibold text-white transition-colors hover:bg-teal-700 disabled:cursor-not-allowed disabled:opacity-50 cursor-pointer"
        >
          {updateMutation.isPending ? "Salvando..." : "Salvar alterações"}
        </button>
      </form>
    </article>
  );
}

export default function RestaurantChannelsPage() {
  const { restaurantId } = useParams();

  const id = Number(restaurantId);

  const queryClient = useQueryClient();
  const session = getSession();

  const isAdmin = session?.role === "AGENCY_ADMIN";
  const validRestaurantId = Number.isInteger(id) && id > 0;

  const restaurantQuery = useQuery({
    queryKey: ["restaurant", id],
    queryFn: async () => {
      const response = await api.get<Restaurant>(`/restaurants/${id}`);
      return response.data;
    },
    enabled: isAdmin && validRestaurantId,
  });

  const [selectedChannelId, setSelectedChannelId] = useState("");
  const [externalStoreId, setExternalStoreId] = useState("");
  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");

  const channelsQuery = useQuery({
    queryKey: ["channels"],

    queryFn: async () => {
      const response = await api.get<Channel[]>("/channels");
      return response.data;
    },

    enabled: isAdmin && validRestaurantId,
  });

  const restaurantChannelsQuery = useQuery({
    queryKey: ["restaurant-channels", id],

    queryFn: async () => {
      const response = await api.get<RestaurantChannel[]>(
        `/restaurants/${id}/channels`,
      );

      return response.data;
    },

    enabled: isAdmin && validRestaurantId,
  });

  const createMutation = useMutation({
    mutationFn: async (data: CreateRestaurantChannelRequest) => {
      const response = await api.post<RestaurantChannel>(
        `/restaurants/${id}/channels`,
        data,
      );

      return response.data;
    },

    onSuccess: async () => {
      await queryClient.invalidateQueries({
        queryKey: ["restaurant-channels", id],
      });

      setSelectedChannelId("");
      setExternalStoreId("");
      setError("");
      setSuccess("Canal associado com sucesso!");
    },

    onError: (error: unknown) => {
      setSuccess("");
      setError(getErrorMessage(error));
    },
  });

  const existingChannelIds = new Set(
    restaurantChannelsQuery.data?.map((channel) => channel.channelId) ?? [],
  );

  const availableChannels =
    channelsQuery.data?.filter(
      (channel) => !existingChannelIds.has(channel.id),
    ) ?? [];

  function handleCreate(event: SubmitEvent<HTMLFormElement>) {
    event.preventDefault();

    setError("");
    setSuccess("");

    const channelId = Number(selectedChannelId);

    if (
      !Number.isInteger(channelId) ||
      !availableChannels.some((channel) => channel.id === channelId)
    ) {
      setError("Selecione um canal disponível.");
      return;
    }

    createMutation.mutate({
      channelId,
      externalStoreId: externalStoreId.trim() || null,
    });
  }

  if (!validRestaurantId) {
    return <p>Restaurante inválido.</p>;
  }

  if (!isAdmin) {
    return (
      <main className="mx-auto min-h-screen max-w-4xl bg-slate-50 p-10">
        <h1 className="text-2xl font-bold text-slate-900">Acesso negado</h1>

        <p className="mt-2 text-slate-600">
          Apenas administradores podem gerenciar canais.
        </p>

        <Link
          to={`/restaurants/${id}/periods`}
          className="mt-4 inline-block text-teal-700 hover:underline"
        >
          Voltar aos períodos
        </Link>
      </main>
    );
  }

  const isLoading =
    channelsQuery.isPending || restaurantChannelsQuery.isPending;

  const hasError = channelsQuery.isError || restaurantChannelsQuery.isError;

  return (
    <div className="min-h-screen bg-slate-50">
      <main className="mx-auto max-w-6xl space-y-8 px-6 py-10">
        <header className="space-y-5">
          <Link
            to={`/restaurants/${id}/periods`}
            className="text-sm font-medium text-teal-700 hover:underline"
          >
            ← Voltar aos períodos
          </Link>

          <h1 className="text-3xl font-bold text-slate-900">
            Gerenciar canais
          </h1>

          <p className="text-slate-500">
            {restaurantQuery.data?.name ?? `Restaurante #${id}`}
          </p>
          <p className="text-sm text-slate-400">
            Configure os canais de vendas disponíveis para este restaurante.
          </p>
        </header>

        {isLoading && <p className="text-slate-500">Carregando canais...</p>}

        {hasError && (
          <p
            role="alert"
            className="rounded-lg border border-red-200 bg-red-50 p-4 text-red-700"
          >
            Não foi possível carregar os canais.
          </p>
        )}

        {!isLoading && !hasError && (
          <>
            <section className="rounded-xl border border-slate-200 bg-white p-6 shadow-sm">
              <h2 className="mb-5 text-xl font-semibold text-slate-900">
                Associar novo canal
              </h2>

              {availableChannels.length === 0 ? (
                <p className="text-slate-600">
                  Todos os canais disponíveis já estão associados.
                </p>
              ) : (
                <form onSubmit={handleCreate} className="space-y-4">
                  <label className="block space-y-2">
                    <span className="text-sm font-medium text-slate-700">
                      Canal
                    </span>

                    <select
                      value={selectedChannelId}
                      onChange={(event) =>
                        setSelectedChannelId(event.target.value)
                      }
                      required
                      className="w-full rounded-lg border border-slate-300 bg-white px-4 py-3 text-slate-900 outline-none focus:border-teal-500 focus:ring-2 focus:ring-teal-100"
                    >
                      <option value="">Selecione um canal</option>

                      {availableChannels.map((channel) => (
                        <option key={channel.id} value={channel.id}>
                          {channel.name}
                        </option>
                      ))}
                    </select>
                  </label>

                  <label className="block space-y-2">
                    <span className="text-sm font-medium text-slate-700">
                      Identificador externo da loja
                    </span>

                    <input
                      type="text"
                      value={externalStoreId}
                      onChange={(event) =>
                        setExternalStoreId(event.target.value)
                      }
                      placeholder="Opcional"
                      className="w-full rounded-lg border border-slate-300 bg-white px-4 py-3 text-slate-900 outline-none focus:border-teal-500 focus:ring-2 focus:ring-teal-100"
                    />
                  </label>

                  {error && (
                    <p role="alert" className="text-sm text-red-600">
                      {error}
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
                    className="rounded-lg bg-teal-600 px-5 py-3 text-sm font-semibold text-white transition-colors hover:bg-teal-700 disabled:cursor-not-allowed disabled:opacity-50 cursor-pointer"
                  >
                    {createMutation.isPending
                      ? "Associando..."
                      : "Associar canal"}
                  </button>
                </form>
              )}
            </section>

            <section>
              <h2 className="mb-5 text-xl font-semibold text-slate-900">
                Canais associados
              </h2>

              {restaurantChannelsQuery.data?.length === 0 && (
                <p className="rounded-xl border border-slate-200 bg-white p-6 text-slate-500">
                  Nenhum canal associado a este restaurante.
                </p>
              )}

              <div className="grid gap-5 md:grid-cols-2">
                {restaurantChannelsQuery.data?.map((channel) => (
                  <ChannelCard
                    key={channel.id}
                    restaurantId={id}
                    channel={channel}
                  />
                ))}
              </div>
            </section>
          </>
        )}
      </main>
    </div>
  );
}
