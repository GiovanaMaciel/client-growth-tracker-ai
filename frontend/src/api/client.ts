import axios from "axios";

import { getSession } from "../auth/session";

export const api = axios.create({
  baseURL: "/api",
});

api.interceptors.request.use((config) => {
  const session = getSession();

  if (session) {
    config.headers.set("Authorization", `Bearer ${session.accessToken}`);
  }

  return config;
});
