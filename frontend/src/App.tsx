import { Navigate, Route, Routes } from "react-router";

import LoginPage from "./pages/LoginPage";

import RestaurantsPage from "./pages/RestaurantsPage";

import PeriodsPage from "./pages/PeriodsPage";

import DashboardPage from "./pages/DashboardPage";

import ProtectedRoute from "./auth/ProtectedRoute";

function App() {
  return (
    <Routes>
      <Route path="/login" element={<LoginPage />} />

      <Route element={<ProtectedRoute />}>
        <Route path="/restaurants" element={<RestaurantsPage />} />

        <Route
          path="/restaurants/:restaurantId/periods"
          element={<PeriodsPage />}
        />

        <Route
          path="/restaurants/:restaurantId/periods/:periodId/dashboard"
          element={<DashboardPage />}
        />
      </Route>

      <Route path="*" element={<Navigate to="/restaurants" replace />} />
    </Routes>
  );
}

export default App;
