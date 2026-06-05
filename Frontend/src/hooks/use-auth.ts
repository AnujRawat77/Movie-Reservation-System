import { useEffect, useState, useCallback } from "react";
import { clearToken, setToken } from "@/lib/api";

const KEY = "cinereserve:auth";

export type AuthUser = {
  userId?: number;
  email: string;
  name: string;
  role?: "USER" | "ADMIN";
};

function read(): AuthUser | null {
  if (typeof window === "undefined") return null;
  try {
    const raw = localStorage.getItem(KEY);
    return raw ? (JSON.parse(raw) as AuthUser) : null;
  } catch {
    return null;
  }
}

export function signIn(user: AuthUser, token?: string) {
  try {
    localStorage.setItem(KEY, JSON.stringify(user));
    if (token) setToken(token);
  } catch (e) {
    console.warn("[signIn] localStorage unavailable:", e);
  }
  window.dispatchEvent(new Event("cinereserve:auth-change"));
}

export function signOut() {
  localStorage.removeItem(KEY);
  clearToken();
  window.dispatchEvent(new Event("cinereserve:auth-change"));
}

export function useAuth() {
  const [user, setUser] = useState<AuthUser | null>(null);
  const [hydrated, setHydrated] = useState(false);

  useEffect(() => {
    setUser(read());
    setHydrated(true);
    const sync = () => setUser(read());
    window.addEventListener("cinereserve:auth-change", sync);
    window.addEventListener("storage", sync);
    return () => {
      window.removeEventListener("cinereserve:auth-change", sync);
      window.removeEventListener("storage", sync);
    };
  }, []);

  const logout = useCallback(() => signOut(), []);

  return {
    user,
    isAuthenticated: !!user,
    isAdmin: user?.role === "ADMIN",
    hydrated,
    logout,
  };
}
