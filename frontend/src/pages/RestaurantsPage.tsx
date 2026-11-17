import { useEffect, useState, type SubmitEvent } from "react";

import axios from "axios";

import { useNavigate } from "react-router";

import { api } from "../api/client";

import { clearSession, getSession } from "../auth/session";

import { useQueryClient } from "@tanstack/react-query";

import { Link } from "react-router";

interface Restaurant {
  id: number;
  name: string;
}

export default function RestaurantsPage() {
  const navigate = useNavigate();

  const session = getSession();

  const [restaurants, setRestaurants] = useState<Restaurant[]>([]);

  const [loading, setLoading] = useState(true);

  const [error, setError] = useState("");

  const [showCreateForm, setShowCreateForm] = useState(false);
  const [newRestaurantName, setNewRestaurantName] = useState("");
  const [isCreating, setIsCreating] = useState(false);
  const [createError, setCreateError] = useState("");
  const [createSuccess, setCreateSuccess] = useState("");

  const [restaurantToDelete, setRestaurantToDelete] =
    useState<Restaurant | null>(null);
  const [deleteConfirmation, setDeleteConfirmation] = useState("");
  const [deleteError, setDeleteError] = useState("");
  const [isDeleting, setIsDeleting] = useState(false);

  const queryClient = useQueryClient();

  useEffect(() => {
    let cancelled = false;

    async function loadRestaurants() {
      try {
        const response = await api.get<Restaurant[]>("/restaurants");

        if (!cancelled) {
          setRestaurants(response.data);
        }
      } catch (err) {
        if (cancelled) return;

        if (axios.isAxiosError(err) && err.response?.status === 401) {
          clearSession();

          navigate("/login", {
            replace: true,
          });

          return;
        }

        setError("Não foi possível carregar os restaurantes.");
      } finally {
        if (!cancelled) {
          setLoading(false);
        }
      }
    }

    void loadRestaurants();

    return () => {
      cancelled = true;
    };
  }, [navigate]);

  function logout() {
    queryClient.clear();
    clearSession();

    navigate("/login", {
      replace: true,
    });
  }

  async function handleCreateRestaurant(event: SubmitEvent<HTMLFormElement>) {
    event.preventDefault();

    if (session?.role !== "AGENCY_ADMIN") {
      return;
    }

    const name = newRestaurantName.trim();

    if (!name || name.length > 160) {
      setCreateError("Informe um nome com até 160 caracteres.");
      return;
    }

    setCreateError("");
    setCreateSuccess("");
    setIsCreating(true);

    try {
      const response = await api.post<Restaurant>("/restaurants", { name });

      setRestaurants((current) =>
        [...current, response.data].sort((a, b) =>
          a.name.localeCompare(b.name, "pt-BR"),
        ),
      );
      setNewRestaurantName("");
      setShowCreateForm(false);
      setCreateSuccess(`${response.data.name} foi cadastrado com sucesso!`);
    } catch (err: unknown) {
      if (axios.isAxiosError(err)) {
        if (err.response?.status === 401) {
          queryClient.clear();
          clearSession();
          navigate("/login", { replace: true });
          return;
        }

        if (err.response?.status === 403) {
          setCreateError("Você não tem permissão para cadastrar restaurantes.");
          return;
        }

        const message = err.response?.data?.message;
        if (typeof message === "string") {
          setCreateError(message);
          return;
        }
      }

      setCreateError(
        "Não foi possível cadastrar o restaurante. Tente novamente.",
      );
    } finally {
      setIsCreating(false);
    }
  }

  function closeDeleteDialog() {
    if (isDeleting) return;

    setRestaurantToDelete(null);
    setDeleteConfirmation("");
    setDeleteError("");
  }

  async function handleDeleteRestaurant() {
    const restaurant = restaurantToDelete;

    if (
      session?.role !== "AGENCY_ADMIN" ||
      !restaurant ||
      deleteConfirmation.trim() !== restaurant.name ||
      isDeleting
    ) {
      return;
    }

    setDeleteError("");
    setIsDeleting(true);

    try {
      await api.delete(`/restaurants/${restaurant.id}`);

      setRestaurants((current) =>
        current.filter((item) => item.id !== restaurant.id),
      );
      queryClient.removeQueries({ queryKey: ["restaurant", restaurant.id] });
      queryClient.removeQueries({ queryKey: ["periods", restaurant.id] });
      queryClient.removeQueries({
        queryKey: ["restaurant-channels", restaurant.id],
      });
      closeDeleteDialogAfterSuccess();
      setCreateSuccess(`${restaurant.name} foi excluído permanentemente.`);
      await queryClient.invalidateQueries({ queryKey: ["restaurants"] });
    } catch (err: unknown) {
      if (axios.isAxiosError(err)) {
        if (err.response?.status === 401) {
          queryClient.clear();
          clearSession();
          navigate("/login", { replace: true });
          return;
        }

        if (err.response?.status === 403) {
          setDeleteError("Você não tem permissão para excluir restaurantes.");
          return;
        }

        if (err.response?.status === 404) {
          setDeleteError(
            "Este restaurante não existe mais. Atualize a página.",
          );
          return;
        }
      }

      setDeleteError(
        "Não foi possível excluir o restaurante. Tente novamente.",
      );
    } finally {
      setIsDeleting(false);
    }
  }

  function closeDeleteDialogAfterSuccess() {
    setRestaurantToDelete(null);
    setDeleteConfirmation("");
    setDeleteError("");
  }

  return (
    <div className="min-h-screen bg-slate-50">
      <header className="bg-white border-b border-slate-200">
        <div className="max-w-6xl mx-auto px-6 py-5 flex items-center justify-between">
          <h1 className="text-lg font-bold text-teal-700">
            Client Growth Tracker
          </h1>

          <div className="flex items-center gap-5">
            <span className="text-sm text-slate-600">Olá, {session?.name}</span>

            <button
              onClick={logout}
              className="text-sm font-medium text-slate-600 hover:text-red-600 cursor-pointer"
            >
              Sair
            </button>
          </div>
        </div>
      </header>

      <main className="max-w-6xl mx-auto px-6 py-10">
        <div className="mb-8 flex flex-wrap items-center justify-between gap-5">
          <div>
            <h2 className="text-3xl font-bold text-slate-900">
              {session?.role === "AGENCY_ADMIN"
                ? "Restaurantes"
                : "Meu restaurante"}
            </h2>

            <p className="text-slate-500 mt-2">
              {session?.role === "AGENCY_ADMIN"
                ? "Gerencie os clientes da Digital Quinze."
                : "Acompanhe os dados do seu restaurante."}
            </p>
          </div>

          {session?.role === "AGENCY_ADMIN" && (
            <button
              type="button"
              onClick={() => {
                setShowCreateForm((open) => !open);
                setCreateError("");
                setCreateSuccess("");
              }}
              aria-expanded={showCreateForm}
              aria-controls="create-restaurant-form"
              className="inline-flex items-center justify-center rounded-lg bg-teal-600 px-5 py-3 text-sm font-semibold text-white transition-colors hover:bg-teal-700 cursor-pointer"
            >
              {showCreateForm ? "Cancelar cadastro" : "+ Novo restaurante"}
            </button>
          )}
        </div>

        {session?.role === "AGENCY_ADMIN" && showCreateForm && (
          <section
            id="create-restaurant-form"
            className="mb-8 max-w-2xl rounded-xl border border-teal-200 bg-white p-6 shadow-sm"
          >
            <h3 className="text-xl font-semibold text-slate-900">
              Cadastrar novo restaurante
            </h3>
            <p className="mt-2 text-sm text-slate-500">
              O acesso do cliente será criado separadamente.
            </p>

            <form onSubmit={handleCreateRestaurant} className="mt-5 space-y-4">
              <label
                htmlFor="new-restaurant-name"
                className="block text-sm font-medium text-slate-700"
              >
                Nome do restaurante
              </label>
              <input
                id="new-restaurant-name"
                type="text"
                value={newRestaurantName}
                onChange={(event) => setNewRestaurantName(event.target.value)}
                maxLength={160}
                autoFocus
                required
                placeholder="Ex.: Casa Pratello Restaurante"
                className="w-full rounded-lg border border-slate-300 bg-white px-4 py-3 text-slate-900 outline-none focus:border-teal-500 focus:ring-2 focus:ring-teal-100"
              />

              {createError && (
                <p role="alert" className="text-sm text-red-600">
                  {createError}
                </p>
              )}

              <button
                type="submit"
                disabled={isCreating}
                className="rounded-lg bg-teal-600 px-5 py-3 text-sm font-semibold text-white transition-colors hover:bg-teal-700 disabled:cursor-not-allowed disabled:opacity-50 cursor-pointer"
              >
                {isCreating ? "Cadastrando..." : "Cadastrar restaurante"}
              </button>
            </form>
          </section>
        )}

        {createSuccess && (
          <p
            role="status"
            className="mb-6 rounded-lg border border-teal-200 bg-teal-50 p-4 text-sm text-teal-800"
          >
            {createSuccess}
          </p>
        )}

        {loading && (
          <p className="text-slate-500">Carregando restaurantes...</p>
        )}

        {error && (
          <div
            role="alert"
            className="rounded-lg bg-red-50 border border-red-200 text-red-700 p-4"
          >
            {error}
          </div>
        )}

        {!loading && !error && restaurants.length === 0 && (
          <p className="text-slate-500">Nenhum restaurante cadastrado.</p>
        )}

        <div className="grid gap-5 md:grid-cols-2 lg:grid-cols-3">
          {restaurants.map((restaurant) => (
            <article
              key={restaurant.id}
              className="group relative rounded-xl border border-slate-200 bg-white shadow-sm transition-all hover:border-teal-300 hover:shadow-md"
            >
              <Link
                to={`/restaurants/${restaurant.id}/periods`}
                className="block rounded-xl p-6 pr-16"
              >
                <div className="mb-5 flex h-12 w-12 items-center justify-center rounded-xl bg-teal-50 font-bold text-teal-700">
                  {restaurant.name.charAt(0)}
                </div>

                <h3 className="text-lg font-semibold text-slate-900 group-hover:text-teal-700">
                  {restaurant.name}
                </h3>

                <p className="mt-2 text-sm text-slate-500">
                  Visualizar períodos e dashboards
                </p>

                <div className="mt-5 text-sm font-semibold text-teal-600">
                  Acessar restaurante →
                </div>
              </Link>

              {session?.role === "AGENCY_ADMIN" && (
                <button
                  type="button"
                  aria-label={`Excluir restaurante ${restaurant.name}`}
                  title={`Excluir ${restaurant.name}`}
                  onClick={() => {
                    setRestaurantToDelete(restaurant);
                    setDeleteConfirmation("");
                    setDeleteError("");
                    setCreateSuccess("");
                  }}
                  className="absolute right-4 top-4 flex h-9 w-9 items-center justify-center rounded-lg text-slate-400 transition-colors hover:bg-red-50 hover:text-red-600 focus-visible:outline-2 focus-visible:outline-red-600 cursor-pointer"
                >
                  <svg
                    aria-hidden="true"
                    width="18"
                    height="18"
                    viewBox="0 0 24 24"
                    fill="none"
                    stroke="currentColor"
                    strokeWidth="1.8"
                    strokeLinecap="round"
                    strokeLinejoin="round"
                  >
                    <path d="M3 6h18M8 6V4h8v2m3 0-1 14H6L5 6m5 4v6m4-6v6" />
                  </svg>
                </button>
              )}
            </article>
          ))}
        </div>
      </main>

      {session?.role === "AGENCY_ADMIN" && restaurantToDelete && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-slate-950/60 px-4 py-6">
          <div
            role="alertdialog"
            aria-modal="true"
            aria-labelledby="delete-restaurant-title"
            aria-describedby="delete-restaurant-description"
            className="w-full max-w-lg rounded-2xl bg-white p-6 shadow-xl"
          >
            <h2
              id="delete-restaurant-title"
              className="text-xl font-bold text-slate-900"
            >
              Excluir restaurante?
            </h2>

            <p
              id="delete-restaurant-description"
              className="mt-3 text-sm leading-relaxed text-slate-600"
            >
              Esta ação é irreversível. O restaurante, seus usuários, períodos,
              métricas financeiras, funis e dados de tráfego pago serão
              apagados.
            </p>

            <label
              htmlFor="delete-restaurant-confirmation"
              className="mt-5 block text-sm font-medium text-slate-700"
            >
              Para confirmar, digite <strong>{restaurantToDelete.name}</strong>:
            </label>
            <input
              id="delete-restaurant-confirmation"
              type="text"
              value={deleteConfirmation}
              onChange={(event) => setDeleteConfirmation(event.target.value)}
              autoFocus
              autoComplete="off"
              className="mt-2 w-full rounded-lg border border-slate-300 px-4 py-3 text-slate-900 outline-none focus:border-red-500 focus:ring-2 focus:ring-red-100"
            />

            {deleteError && (
              <p role="alert" className="mt-3 text-sm text-red-600">
                {deleteError}
              </p>
            )}

            <div className="mt-6 flex flex-wrap justify-end gap-3">
              <button
                type="button"
                disabled={isDeleting}
                onClick={closeDeleteDialog}
                className="rounded-lg border border-slate-300 px-5 py-2.5 text-sm font-semibold text-slate-700 hover:bg-slate-50 disabled:opacity-50 cursor-pointer"
              >
                Cancelar
              </button>
              <button
                type="button"
                onClick={() => void handleDeleteRestaurant()}
                disabled={
                  isDeleting ||
                  deleteConfirmation.trim() !== restaurantToDelete.name
                }
                className="rounded-lg bg-red-600 px-5 py-2.5 text-sm font-semibold text-white hover:bg-red-700 disabled:cursor-not-allowed disabled:opacity-50 cursor-pointer"
              >
                {isDeleting ? "Excluindo..." : "Excluir permanentemente"}
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
