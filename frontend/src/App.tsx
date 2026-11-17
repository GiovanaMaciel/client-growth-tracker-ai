import { Navigate, Route, Routes } from "react-router";

import LoginPage from "./pages/LoginPage";
import RestaurantsPage from "./pages/RestaurantsPage";
import PeriodsPage from "./pages/PeriodsPage";
import RestaurantChannelsPage from "./pages/RestaurantChannelsPage";
import DashboardPage from "./pages/DashboardPage";
import DataEntryPage from "./pages/DataEntryPage";
import FunnelPage from "./pages/FunnelPage";
import PaidTrafficPage from "./pages/PaidTrafficPage";

import ProtectedRoute from "./auth/ProtectedRoute";

export default function App() {
  return (
    <Routes>
      <Route path="/login" element={<LoginPage />} />

      <Route element={<ProtectedRoute />}>
        <Route path="/restaurants" element={<RestaurantsPage />} />

        <Route
          path="/restaurants/:restaurantId/channels"
          element={<RestaurantChannelsPage />}
        />

        <Route
          path="/restaurants/:restaurantId/periods"
          element={<PeriodsPage />}
        />

        <Route
          path="/restaurants/:restaurantId/periods/:periodId/dashboard"
          element={<DashboardPage />}
        />

        <Route
          path="/restaurants/:restaurantId/periods/:periodId/data"
          element={<DataEntryPage />}
        />

        <Route
          path="/restaurants/:restaurantId/periods/:periodId/funnels"
          element={<FunnelPage />}
        />

        <Route
          path="/restaurants/:restaurantId/periods/:periodId/paid-traffic"
          element={<PaidTrafficPage />}
        />
      </Route>

      <Route path="*" element={<Navigate to="/restaurants" replace />} />
    </Routes>
  );
}
