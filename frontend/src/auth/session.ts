export type UserRole = "AGENCY_ADMIN" | "RESTAURANT_USER";

export interface LoginResponse {
  accessToken: string;
  tokenType: string;
  expiresIn: number;

  userId: number;
  name: string;
  email: string;
  role: UserRole;
  restaurantId: number | null;
}

export interface Session extends LoginResponse {
  expiresAt: number;
}

const SESSION_KEY = "cgt_session";

export function saveSession(login: LoginResponse): void {
  const session: Session = {
    ...login,
    expiresAt: Date.now() + login.expiresIn * 1000,
  };

  sessionStorage.setItem(SESSION_KEY, JSON.stringify(session));
}

export function getSession(): Session | null {
  const stored = sessionStorage.getItem(SESSION_KEY);

  if (!stored) {
    return null;
  }

  try {
    const session = JSON.parse(stored) as Session;

    if (
      !session.accessToken ||
      !session.expiresAt ||
      Date.now() >= session.expiresAt
    ) {
      clearSession();
      return null;
    }

    return session;
  } catch {
    clearSession();
    return null;
  }
}

export function clearSession(): void {
  sessionStorage.removeItem(SESSION_KEY);
}
