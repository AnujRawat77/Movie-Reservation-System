import poster1 from "@/assets/poster-1.jpg";
import poster2 from "@/assets/poster-2.jpg";
import poster3 from "@/assets/poster-3.jpg";
import poster4 from "@/assets/poster-4.jpg";
import poster5 from "@/assets/poster-5.jpg";
import poster6 from "@/assets/poster-6.jpg";

export const API_BASE =
  (typeof import.meta !== "undefined" &&
    (import.meta as any).env?.VITE_API_BASE_URL) ||
  "http://localhost:8080";

const POSTER_MAP: Record<string, string> = {
  "/images/poster-1.jpg": poster1,
  "/images/poster-2.jpg": poster2,
  "/images/poster-3.jpg": poster3,
  "/images/poster-4.jpg": poster4,
  "/images/poster-5.jpg": poster5,
  "/images/poster-6.jpg": poster6,
};

export function resolvePoster(url?: string | null): string {
  if (!url) return poster1;
  if (POSTER_MAP[url]) return POSTER_MAP[url];
  return url;
}

// ---------- Types ----------

export type Genre = { id: number; name: string };

export type ShowtimeDto = {
  id: number;
  movieId: number;
  hallId: number;
  hallName: string;
  startTime: string;
  endTime: string;
  price: number;
  status: string;
  date: string;
  time: string;
};

export type MovieDto = {
  id: number;
  title: string;
  tagline: string;
  description?: string;
  posterUrl?: string;
  durationMinutes: number;
  rating: number;
  year: number;
  language: string;
  synopsis: string;
  status: "now" | "soon";
  releaseDate?: string | null;
  trailerUrl?: string | null;
  director?: string | null;
  cast?: string | null;
  censorRating?: string | null;
  genres: Genre[];
  showtimes?: ShowtimeDto[] | null;
};

export type SeatStatus = "AVAILABLE" | "BOOKED" | "HELD_BY_ME" | "HELD_BY_OTHER";

export type SeatDto = {
  id: number;
  rowLabel: string;
  seatNumber: number;
  seatType: "REGULAR" | "PREMIUM";
  status: SeatStatus;
};

export type SeatHoldSeatInfo = {
  seatId: number;
  rowLabel: string;
  seatNumber: number;
  seatType: string;
};

export type SeatHoldDto = {
  holdId: string;
  showtimeId: number;
  movieTitle: string;
  hallName: string;
  showDate: string;
  showTime: string;
  seats: SeatHoldSeatInfo[];
  status: "ACTIVE" | "CONFIRMED" | "EXPIRED" | "RELEASED";
  expiresAt: string;
  expiresInSeconds: number;
  totalAmount: number;
};

export type ReservationSeatInfo = {
  rowLabel: string;
  seatNumber: number;
  seatType: string;
};

export type ReservationDto = {
  id: string;
  userId: number;
  userName: string;
  showtimeId: number;
  movieTitle: string;
  hallName: string;
  showDate: string;
  showTime: string;
  seats: ReservationSeatInfo[];
  status: "CONFIRMED" | "CANCELLED";
  totalAmount: number;
  createdAt: string;
  cancelledAt: string | null;
  refundAmount: number | null;
  refundPercentage: number | null;
};

export type AuthDto = {
  token: string;
  userId: number;
  email: string;
  name: string;
  role: "USER" | "ADMIN";
};

export type ApiResponse<T> = { success: true; data: T; message?: string };
export type ApiErrorResponse = {
  success: false;
  error: { code: string; message: string };
};

// ---------- Token management ----------

const TOKEN_KEY = "cinereserve:token";

export function getToken(): string | null {
  if (typeof window === "undefined") return null;
  try {
    return localStorage.getItem(TOKEN_KEY);
  } catch {
    return null;
  }
}

export function setToken(token: string) {
  if (typeof window === "undefined") return;
  try {
    localStorage.setItem(TOKEN_KEY, token);
  } catch {
    // localStorage may be unavailable (private mode / storage quota)
  }
}

export function clearToken() {
  if (typeof window === "undefined") return;
  localStorage.removeItem(TOKEN_KEY);
}

// ---------- Fetch wrapper ----------

export class ApiError extends Error {
  code: string;
  status: number;
  constructor(code: string, message: string, status: number) {
    super(message);
    this.code = code;
    this.status = status;
  }
}

async function request<T>(
  path: string,
  options: RequestInit = {},
): Promise<T> {
  const token = getToken();
  const headers: Record<string, string> = {
    "Content-Type": "application/json",
    ...((options.headers as Record<string, string>) ?? {}),
  };
  if (token) headers["Authorization"] = `Bearer ${token}`;

  const res = await fetch(`${API_BASE}${path}`, { ...options, headers });
  const isJson = res.headers
    .get("content-type")
    ?.includes("application/json");
  const body = isJson ? await res.json() : null;

  if (!res.ok || (body && body.success === false)) {
    const errMsg =
      body?.error?.message ?? body?.message ?? `Request failed (${res.status})`;
    const code = body?.error?.code ?? `HTTP_${res.status}`;
    throw new ApiError(code, errMsg, res.status);
  }

  return (body?.data ?? body) as T;
}

// ---------- Auth ----------

export const auth = {
  login: (email: string, password: string) =>
    request<AuthDto>("/api/auth/login", {
      method: "POST",
      body: JSON.stringify({ email, password }),
    }),
  register: (name: string, email: string, password: string) =>
    request<AuthDto>("/api/auth/register", {
      method: "POST",
      body: JSON.stringify({ name, email, password }),
    }),
  me: () => request<AuthDto>("/api/users/me"),
};

// ---------- User Profile ----------

export type UpdateProfilePayload = {
  name?: string;
  phone?: string;
  address?: string;
  profilePictureUrl?: string;
};

export const users = {
  me: () => request<UserDto>("/api/users/me"),
  updateProfile: (data: UpdateProfilePayload) =>
    request<UserDto>("/api/users/me", {
      method: "PUT",
      body: JSON.stringify(data),
    }),
};

// ---------- Movies ----------

export const movies = {
  list: (params: { status?: string; genre?: string; search?: string } = {}) => {
    const q = new URLSearchParams();
    if (params.status) q.set("status", params.status);
    if (params.genre) q.set("genre", params.genre);
    if (params.search) q.set("search", params.search);
    const qs = q.toString();
    return request<MovieDto[]>(`/api/movies${qs ? `?${qs}` : ""}`);
  },
  get: (id: number | string) => request<MovieDto>(`/api/movies/${id}`),
  showtimes: (movieId: number | string, date?: string) => {
    const qs = date ? `?date=${encodeURIComponent(date)}` : "";
    return request<ShowtimeDto[]>(`/api/movies/${movieId}/showtimes${qs}`);
  },
};

// ---------- Genres ----------

export const genres = {
  list: () => request<Genre[]>("/api/genres"),
};

// ---------- Showtimes ----------

export const showtimes = {
  seats: (showtimeId: number | string) =>
    request<SeatDto[]>(`/api/showtimes/${showtimeId}/seats`),
  get: (showtimeId: number | string) =>
    request<ShowtimeDto>(`/api/showtimes/${showtimeId}`),
  seatMap: (showtimeId: number | string) =>
    request<SeatDto[]>(`/api/showtimes/${showtimeId}/seat-map`),
};

// ---------- Holds ----------

export const holds = {
  create: (showtimeId: number, seatIds: number[]) =>
    request<SeatHoldDto>("/api/holds", {
      method: "POST",
      body: JSON.stringify({ showtimeId, seatIds }),
    }),
  get: (holdId: string) => request<SeatHoldDto>(`/api/holds/${holdId}`),
  refresh: (holdId: string) =>
    request<SeatHoldDto>(`/api/holds/${holdId}/refresh`, { method: "POST" }),
  release: (holdId: string) =>
    request<void>(`/api/holds/${holdId}`, { method: "DELETE" }),
  confirm: (holdId: string) =>
    request<ReservationDto>(`/api/holds/${holdId}/confirm`, { method: "POST" }),
};

// ---------- Reservations ----------

export type ReservationFilters = {
  status?: string;
  fromDate?: string;
  toDate?: string;
  movieTitle?: string;
};

export const reservations = {
  create: (showtimeId: number, seatIds: number[]) =>
    request<ReservationDto>("/api/reservations", {
      method: "POST",
      body: JSON.stringify({ showtimeId, seatIds }),
    }),
  me: (filters: ReservationFilters = {}) => {
    const q = new URLSearchParams();
    if (filters.status) q.set("status", filters.status);
    if (filters.fromDate) q.set("fromDate", filters.fromDate);
    if (filters.toDate) q.set("toDate", filters.toDate);
    if (filters.movieTitle) q.set("movieTitle", filters.movieTitle);
    const qs = q.toString();
    return request<ReservationDto[]>(`/api/reservations/me${qs ? `?${qs}` : ""}`);
  },
  get: (id: string) => request<ReservationDto>(`/api/reservations/${id}`),
  cancel: (id: string) =>
    request<ReservationDto>(`/api/reservations/${id}`, { method: "DELETE" }),
};

// ---------- Reviews ----------

export type ReviewDto = {
  id: number;
  userId: number;
  userName: string;
  movieId: number;
  rating: number;
  comment: string | null;
  createdAt: string;
};

export const reviews = {
  list: (movieId: number | string) =>
    request<ReviewDto[]>(`/api/movies/${movieId}/reviews`),
  create: (movieId: number | string, rating: number, comment?: string) =>
    request<ReviewDto>(`/api/movies/${movieId}/reviews`, {
      method: "POST",
      body: JSON.stringify({ rating, comment: comment || null }),
    }),
  delete: (movieId: number | string, reviewId: number) =>
    request<void>(`/api/movies/${movieId}/reviews/${reviewId}`, {
      method: "DELETE",
    }),
};

// ---------- Admin types ----------

export type UserDto = {
  id: number;
  name: string;
  email: string;
  phone: string | null;
  address: string | null;
  profilePictureUrl: string | null;
  loyaltyPoints: number;
  role: "USER" | "ADMIN";
  active: boolean;
  createdAt: string;
};

export type HallDto = {
  id: number;
  name: string;
  totalRows: number;
  seatsPerRow: number;
};

export type MovieFormPayload = {
  title: string;
  tagline?: string;
  description?: string;
  posterUrl?: string;
  durationMinutes: number;
  rating: number;
  year: number;
  language: string;
  synopsis: string;
  status: "now" | "soon";
  releaseDate?: string | null;
  trailerUrl?: string | null;
  director?: string | null;
  cast?: string | null;
  censorRating?: string | null;
  genreIds: number[];
};

export type ShowtimeFormPayload = {
  movieId: number;
  hallId: number;
  startTime: string; // ISO LocalDateTime, e.g. 2026-06-01T18:00:00
  price: number;
};

export type BulkShowtimePayload = {
  movieId: number;
  hallId: number;
  startDate: string;
  endDate: string;
  times: string[];
  price: number;
  daysOfWeek: string[];
};

export type HallFormPayload = {
  name: string;
  totalRows: number;
  seatsPerRow: number;
};

export type RevenueByMovie = { movieTitle: string; revenue: number };
export type RevenueReport = {
  from: string | null;
  to: string | null;
  totalRevenue: number;
  byMovie: RevenueByMovie[];
};

export type CapacityReport = {
  showtimeId: number;
  movieTitle: string;
  hallName: string;
  startTime: string;
  totalSeats: number;
  bookedSeats: number;
  availableSeats: number;
  occupancyPercent: number;
};

export type TopMovie = { movieId: number; title: string; revenue: number };

export type DashboardSummary = {
  totalBookings: number;
  totalCancellations: number;
  totalRevenue: number;
};

export type DailySale = { date: string; revenue: number; bookings: number };
export type GenreRevenue = { genre: string; revenue: number };
export type HallStats = { hallName: string; showtimeCount: number };

// ---------- Loyalty ----------

export type LoyaltyTransactionDto = {
  id: number;
  type: "EARNED" | "REDEEMED";
  points: number;
  description: string;
  reservationId: string | null;
  createdAt: string;
};

export type LoyaltyBalanceDto = {
  balance: number;
  transactions: LoyaltyTransactionDto[];
};

export const loyalty = {
  balance: () => request<LoyaltyBalanceDto>("/api/users/me/loyalty"),
  redeem: (points: number) =>
    request<LoyaltyBalanceDto>("/api/users/me/loyalty/redeem", {
      method: "POST",
      body: JSON.stringify({ points }),
    }),
};

// ---------- Watchlist ----------

export type WatchlistItemDto = {
  movieId: number;
  movieTitle: string;
  moviePosterUrl: string | null;
  addedAt: string;
};

export const watchlist = {
  list: () => request<WatchlistItemDto[]>("/api/users/me/watchlist"),
  add: (movieId: number) =>
    request<void>(`/api/movies/${movieId}/watchlist`, { method: "POST" }),
  remove: (movieId: number) =>
    request<void>(`/api/movies/${movieId}/watchlist`, { method: "DELETE" }),
  status: (movieId: number) =>
    request<{ inWatchlist: boolean }>(`/api/movies/${movieId}/watchlist/status`),
};

// ---------- Admin ----------

function buildQuery(params: Record<string, string | number | boolean | undefined | null>) {
  const q = new URLSearchParams();
  for (const [k, v] of Object.entries(params)) {
    if (v === undefined || v === null || v === "") continue;
    q.set(k, String(v));
  }
  const qs = q.toString();
  return qs ? `?${qs}` : "";
}

export const admin = {
  movies: {
    list: (includeDeleted = false) =>
      request<MovieDto[]>(`/api/movies${buildQuery({ includeDeleted })}`),
    create: (data: MovieFormPayload) =>
      request<MovieDto>("/api/movies", {
        method: "POST",
        body: JSON.stringify(data),
      }),
    update: (id: number, data: MovieFormPayload) =>
      request<MovieDto>(`/api/movies/${id}`, {
        method: "PUT",
        body: JSON.stringify(data),
      }),
    delete: (id: number) =>
      request<void>(`/api/movies/${id}`, { method: "DELETE" }),
  },
  genres: {
    create: (name: string) =>
      request<Genre>("/api/genres", {
        method: "POST",
        body: JSON.stringify({ name }),
      }),
    delete: (id: number) =>
      request<void>(`/api/genres/${id}`, { method: "DELETE" }),
  },
  halls: {
    list: () => request<HallDto[]>("/api/halls"),
    get: (id: number) => request<HallDto>(`/api/halls/${id}`),
    create: (data: HallFormPayload) =>
      request<HallDto>("/api/halls", {
        method: "POST",
        body: JSON.stringify(data),
      }),
    update: (id: number, data: HallFormPayload) =>
      request<HallDto>(`/api/halls/${id}`, {
        method: "PUT",
        body: JSON.stringify(data),
      }),
    delete: (id: number) =>
      request<void>(`/api/halls/${id}`, { method: "DELETE" }),
  },
  showtimes: {
    list: (filters: {
      movieId?: number;
      hallId?: number;
      status?: string;
      from?: string;
      to?: string;
    } = {}) => request<ShowtimeDto[]>(`/api/showtimes${buildQuery(filters)}`),
    create: (data: ShowtimeFormPayload) =>
      request<ShowtimeDto>("/api/showtimes", {
        method: "POST",
        body: JSON.stringify(data),
      }),
    update: (id: number, data: ShowtimeFormPayload) =>
      request<ShowtimeDto>(`/api/showtimes/${id}`, {
        method: "PUT",
        body: JSON.stringify(data),
      }),
    cancel: (id: number) =>
      request<void>(`/api/showtimes/${id}`, { method: "DELETE" }),
    bulk: (data: BulkShowtimePayload) =>
      request<ShowtimeDto[]>("/api/showtimes/bulk", {
        method: "POST",
        body: JSON.stringify(data),
      }),
  },
  users: {
    list: () => request<UserDto[]>("/api/users"),
    updateRole: (id: number, role: "USER" | "ADMIN") =>
      request<UserDto>(`/api/users/${id}/role`, {
        method: "PATCH",
        body: JSON.stringify({ role }),
      }),
    toggleActive: (id: number) =>
      request<UserDto>(`/api/users/${id}/toggle-active`, { method: "PATCH" }),
    bookings: (id: number) =>
      request<ReservationDto[]>(`/api/users/${id}/bookings`),
  },
  reservations: {
    list: () => request<ReservationDto[]>("/api/reservations"),
  },
  reports: {
    revenue: (params: { from?: string; to?: string } = {}) =>
      request<RevenueReport>(`/api/reports/revenue${buildQuery(params)}`),
    capacity: (showtimeId: number) =>
      request<CapacityReport>(`/api/reports/capacity/${showtimeId}`),
    topMovies: () => request<TopMovie[]>("/api/reports/top-movies"),
    dashboard: () => request<DashboardSummary>("/api/reports/dashboard"),
    dailySales: (days = 30) =>
      request<DailySale[]>(`/api/reports/daily-sales?days=${days}`),
    revenueByGenre: () => request<GenreRevenue[]>("/api/reports/revenue-by-genre"),
    topHalls: () => request<HallStats[]>("/api/reports/top-halls"),
  },
};
