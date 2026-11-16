import { useEffect, useState } from "react";

import axios from "axios";

import { useNavigate } from "react-router";

import { api } from "../api/client";

import { clearSession, getSession } from "../auth/session";

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
    clearSession();

    navigate("/login", {
      replace: true,
    });
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
        <div className="mb-8">
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
            <div
              key={restaurant.id}
              className="bg-white border border-slate-200 rounded-xl p-6 shadow-sm"
            >
              <div className="h-12 w-12 rounded-xl bg-teal-50 text-teal-700 flex items-center justify-center font-bold mb-5">
                {restaurant.name.charAt(0)}
              </div>

              <h3 className="text-lg font-semibold text-slate-900">
                {restaurant.name}
              </h3>

              <p className="text-sm text-slate-500 mt-2">Cliente cadastrado</p>
            </div>
          ))}
        </div>
      </main>
    </div>
  );
}
