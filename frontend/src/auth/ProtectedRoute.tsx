import { Navigate, Outlet } from "react-router";

import { getSession } from "./session";

export default function ProtectedRoute() {
  const session = getSession();

  if (!session) {
    return <Navigate to="/login" replace />;
  }

  return <Outlet />;
}
