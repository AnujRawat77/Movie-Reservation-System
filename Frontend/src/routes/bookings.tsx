import { createFileRoute, Link, Outlet, useChildMatches, useNavigate } from "@tanstack/react-router";
import { motion } from "framer-motion";
import { useEffect, useState } from "react";
import { Calendar, Clock, Eye, Filter, MapPin, Search, Ticket, XCircle } from "lucide-react";
import { toast } from "sonner";
import { ROUTES } from "@/constants/routes";
import { STRINGS } from "@/constants/strings";
import { HammerButton } from "@/components/HammerButton";
import {
  reservations as reservationsApi,
  ApiError,
  type ReservationDto,
  type ReservationFilters,
} from "@/lib/api";
import { useAuth } from "@/hooks/use-auth";

export const Route = createFileRoute("/bookings")({
  head: () => ({ meta: [{ title: "My Bookings — CineReserve" }] }),
  component: MyBookingsPage,
});

function MyBookingsPage() {
  const childMatches = useChildMatches();
  if (childMatches.length > 0) return <Outlet />;

  return <BookingsList />;
}

function BookingsList() {
  const { isAuthenticated, hydrated } = useAuth();
  const navigate = useNavigate();
  const [list, setList] = useState<ReservationDto[]>([]);
  const [loading, setLoading] = useState(true);
  const [filters, setFilters] = useState<ReservationFilters>({});
  const [statusFilter, setStatusFilter] = useState("");
  const [fromDate, setFromDate] = useState("");
  const [toDate, setToDate] = useState("");
  const [movieSearch, setMovieSearch] = useState("");

  useEffect(() => {
    if (hydrated && !isAuthenticated) {
      navigate({ to: ROUTES.login });
    }
  }, [hydrated, isAuthenticated, navigate]);

  const applyFilters = () => {
    const f: ReservationFilters = {};
    if (statusFilter) f.status = statusFilter;
    if (fromDate) f.fromDate = new Date(fromDate).toISOString();
    if (toDate) {
      const d = new Date(toDate);
      d.setDate(d.getDate() + 1);
      f.toDate = d.toISOString();
    }
    if (movieSearch.trim()) f.movieTitle = movieSearch.trim();
    setFilters(f);
  };

  const clearFilters = () => {
    setStatusFilter("");
    setFromDate("");
    setToDate("");
    setMovieSearch("");
    setFilters({});
  };

  const refresh = async (activeFilters = filters) => {
    try {
      const data = await reservationsApi.me(activeFilters);
      setList(data);
    } catch (e) {
      if (e instanceof ApiError && e.status !== 401) toast.error(e.message);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    if (!isAuthenticated) return;
    refresh(filters);
  }, [isAuthenticated, filters]);

  const cancel = async (id: string) => {
    try {
      await reservationsApi.cancel(id);
      toast.success("Reservation cancelled");
      refresh(filters);
    } catch (e) {
      const msg = e instanceof ApiError ? e.message : "Cancel failed";
      toast.error(msg);
    }
  };

  if (!isAuthenticated || loading) {
    return (
      <div className="mx-auto max-w-3xl px-4 py-24 text-center font-display text-3xl text-muted-foreground">
        Loading…
      </div>
    );
  }

  if (list.length === 0) {
    return (
      <div className="mx-auto flex min-h-[70dvh] max-w-2xl flex-col items-center justify-center px-4 py-24 text-center">
        <div className="mb-6 grid h-20 w-20 place-items-center rounded-full border border-accent/30 bg-accent/10">
          <Ticket className="h-8 w-8 text-accent" />
        </div>
        <h1 className="font-display text-5xl md:text-6xl">No bookings yet</h1>
        <p className="mt-3 max-w-sm text-muted-foreground">
          Your story starts here. Pick a film, choose a seat, and the lights
          will do the rest.
        </p>
        <div className="mt-8">
          <Link to={ROUTES.movies}>
            <HammerButton variant="gold" size="lg">
              {STRINGS.movies.bookNow}
            </HammerButton>
          </Link>
        </div>
      </div>
    );
  }

  return (
    <div className="mx-auto max-w-4xl px-4 pb-24 pt-12 sm:px-6 lg:px-8">
      <motion.div
        initial={{ opacity: 0, y: 16 }}
        animate={{ opacity: 1, y: 0 }}
        className="mb-10"
      >
        <div className="mb-2 text-xs uppercase tracking-widest text-accent">
          Your collection
        </div>
        <h1 className="font-display text-5xl md:text-7xl">My Bookings</h1>
      </motion.div>

      {/* Filter bar */}
      <motion.div
        initial={{ opacity: 0, y: 8 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ delay: 0.1 }}
        className="mb-6 rounded-xl border border-border bg-card/60 p-4"
      >
        <div className="flex flex-wrap items-end gap-3">
          <div className="flex flex-col gap-1 min-w-[140px]">
            <label className="text-xs uppercase tracking-widest text-muted-foreground">Status</label>
            <select
              value={statusFilter}
              onChange={(e) => setStatusFilter(e.target.value)}
              className="rounded-md border border-border bg-background px-3 py-2 text-sm focus:outline-none focus:ring-1 focus:ring-accent"
            >
              <option value="">All</option>
              <option value="CONFIRMED">Confirmed</option>
              <option value="CANCELLED">Cancelled</option>
            </select>
          </div>
          <div className="flex flex-col gap-1">
            <label className="text-xs uppercase tracking-widest text-muted-foreground">From</label>
            <input
              type="date"
              value={fromDate}
              onChange={(e) => setFromDate(e.target.value)}
              className="rounded-md border border-border bg-background px-3 py-2 text-sm focus:outline-none focus:ring-1 focus:ring-accent"
            />
          </div>
          <div className="flex flex-col gap-1">
            <label className="text-xs uppercase tracking-widest text-muted-foreground">To</label>
            <input
              type="date"
              value={toDate}
              onChange={(e) => setToDate(e.target.value)}
              className="rounded-md border border-border bg-background px-3 py-2 text-sm focus:outline-none focus:ring-1 focus:ring-accent"
            />
          </div>
          <div className="flex flex-col gap-1 min-w-[180px] flex-1">
            <label className="text-xs uppercase tracking-widest text-muted-foreground">Movie</label>
            <div className="relative">
              <Search className="absolute left-3 top-1/2 h-3.5 w-3.5 -translate-y-1/2 text-muted-foreground" />
              <input
                type="text"
                placeholder="Search by movie title…"
                value={movieSearch}
                onChange={(e) => setMovieSearch(e.target.value)}
                onKeyDown={(e) => e.key === "Enter" && applyFilters()}
                className="w-full rounded-md border border-border bg-background py-2 pl-9 pr-3 text-sm focus:outline-none focus:ring-1 focus:ring-accent"
              />
            </div>
          </div>
          <button
            onClick={applyFilters}
            className="inline-flex items-center gap-1.5 rounded-md bg-accent px-4 py-2 text-sm font-medium text-background hover:bg-accent/90"
          >
            <Filter className="h-3.5 w-3.5" /> Apply
          </button>
          {(statusFilter || fromDate || toDate || movieSearch) && (
            <button
              onClick={clearFilters}
              className="text-xs text-muted-foreground hover:text-foreground underline"
            >
              Clear
            </button>
          )}
        </div>
      </motion.div>

      {list.length === 0 ? (
        <div className="flex flex-col items-center py-16 text-center">
          <Ticket className="mb-4 h-10 w-10 text-muted-foreground/40" />
          <p className="text-muted-foreground">No bookings match your filters.</p>
        </div>
      ) : (
      <div className="space-y-4">
        {list.map((r, i) => (
          <motion.div
            key={r.id}
            initial={{ opacity: 0, y: 16 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ delay: i * 0.05 }}
            className="rounded-2xl border border-border bg-card/80 p-6 shadow-elegant backdrop-blur"
          >
            <div className="flex flex-wrap items-start justify-between gap-4">
              <div className="min-w-0 flex-1">
                <h2 className="font-display text-2xl tracking-wide">
                  {r.movieTitle}
                </h2>
                <div className="mt-2 flex flex-wrap gap-x-4 gap-y-1 text-sm text-muted-foreground">
                  <span className="flex items-center gap-1.5">
                    <Calendar className="h-3.5 w-3.5" /> {r.showDate}
                  </span>
                  <span className="flex items-center gap-1.5">
                    <Clock className="h-3.5 w-3.5" /> {r.showTime}
                  </span>
                  <span className="flex items-center gap-1.5">
                    <MapPin className="h-3.5 w-3.5" /> {r.hallName}
                  </span>
                </div>
                <div className="mt-3 flex flex-wrap gap-1.5">
                  {r.seats.map((s, idx) => (
                    <span
                      key={idx}
                      className="rounded-md border border-border bg-background/60 px-2 py-0.5 text-xs"
                    >
                      {s.rowLabel}
                      {s.seatNumber}
                    </span>
                  ))}
                </div>
              </div>
              <div className="flex flex-col items-end gap-2">
                <span
                  className={`rounded-full px-3 py-1 text-[10px] uppercase tracking-widest ${
                    r.status === "CONFIRMED"
                      ? "bg-accent/15 text-accent"
                      : "bg-muted text-muted-foreground"
                  }`}
                >
                  {r.status}
                </span>
                <div className="font-display text-2xl text-gradient-gold">
                  ${Number(r.totalAmount).toFixed(2)}
                </div>
                <Link
                  to={ROUTES.bookingDetail(r.id)}
                  className="inline-flex items-center gap-1 text-xs text-accent transition-colors hover:underline"
                >
                  <Eye className="h-3.5 w-3.5" /> View Details
                </Link>
                {r.status === "CONFIRMED" && (
                  <button
                    onClick={() => cancel(r.id)}
                    className="inline-flex items-center gap-1 text-xs text-muted-foreground transition-colors hover:text-destructive"
                  >
                    <XCircle className="h-3.5 w-3.5" /> Cancel
                  </button>
                )}
              </div>
            </div>
          </motion.div>
        ))}
      </div>
      )}
    </div>
  );
}
