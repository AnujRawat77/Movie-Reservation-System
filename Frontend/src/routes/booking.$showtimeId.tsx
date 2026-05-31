import { createFileRoute, Link, useNavigate } from "@tanstack/react-router";
import { motion } from "framer-motion";
import { useEffect, useMemo, useState } from "react";
import { toast } from "sonner";
import {
  reservations as reservationsApi,
  showtimes as showtimesApi,
  ApiError,
  type SeatDto,
  type ShowtimeDto,
} from "@/lib/api";
import { ROUTES } from "@/constants/routes";
import { STRINGS } from "@/constants/strings";
import { HammerButton } from "@/components/HammerButton";
import { useAuth } from "@/hooks/use-auth";

export const Route = createFileRoute("/booking/$showtimeId")({
  head: () => ({ meta: [{ title: "Select seats — CineReserve" }] }),
  component: SeatSelection,
});

type SeatState = "available" | "selected" | "booked" | "premium";

function SeatSelection() {
  const { showtimeId } = Route.useParams();
  const { isAuthenticated, hydrated } = useAuth();
  const navigate = useNavigate();

  const [seats, setSeats] = useState<SeatDto[]>([]);
  const [showtime, setShowtime] = useState<ShowtimeDto | null>(null);
  const [loading, setLoading] = useState(true);
  const [selected, setSelected] = useState<Set<number>>(new Set());
  const [submitting, setSubmitting] = useState(false);

  useEffect(() => {
    if (hydrated && !isAuthenticated) {
      toast.error("Please sign in to book seats");
      navigate({ to: ROUTES.login });
    }
  }, [hydrated, isAuthenticated, navigate]);

  useEffect(() => {
    let cancelled = false;
    (async () => {
      try {
        const [s, st] = await Promise.all([
          showtimesApi.seats(showtimeId),
          showtimesApi.get(showtimeId).catch(() => null),
        ]);
        if (cancelled) return;
        setSeats(s);
        setShowtime(st);
      } catch (e) {
        if (!cancelled) {
          const msg = e instanceof ApiError ? e.message : "Failed to load seats";
          toast.error(msg);
        }
      } finally {
        if (!cancelled) setLoading(false);
      }
    })();
    return () => {
      cancelled = true;
    };
  }, [showtimeId, isAuthenticated]);

  // Group seats by row
  const rows = useMemo(() => {
    const map = new Map<string, SeatDto[]>();
    seats.forEach((s) => {
      const arr = map.get(s.rowLabel) ?? [];
      arr.push(s);
      map.set(s.rowLabel, arr);
    });
    return Array.from(map.entries())
      .sort(([a], [b]) => a.localeCompare(b))
      .map(([label, list]) => [
        label,
        list.sort((a, b) => a.seatNumber - b.seatNumber),
      ]) as Array<[string, SeatDto[]]>;
  }, [seats]);

  const seatById = useMemo(() => {
    const m = new Map<number, SeatDto>();
    seats.forEach((s) => m.set(s.id, s));
    return m;
  }, [seats]);

  const toggle = (seat: SeatDto) => {
    if (seat.status === "BOOKED") {
      toast.error(STRINGS.toasts.seatTaken);
      return;
    }
    setSelected((prev) => {
      const next = new Set(prev);
      if (next.has(seat.id)) next.delete(seat.id);
      else next.add(seat.id);
      return next;
    });
  };

  const stateOf = (seat: SeatDto): SeatState => {
    if (seat.status === "BOOKED") return "booked";
    if (selected.has(seat.id)) return "selected";
    if (seat.seatType === "PREMIUM") return "premium";
    return "available";
  };

  const total = useMemo(() => {
    if (!showtime) return 0;
    return Array.from(selected).reduce((sum, id) => {
      const seat = seatById.get(id);
      if (!seat) return sum;
      const base = Number(showtime.price);
      return sum + (seat.seatType === "PREMIUM" ? base * 1.5 : base);
    }, 0);
  }, [selected, seatById, showtime]);

  const confirm = async () => {
    if (selected.size === 0) {
      toast.warning(STRINGS.booking.pickAtLeastOne);
      return;
    }
    setSubmitting(true);
    try {
      const reservation = await reservationsApi.create(
        Number(showtimeId),
        Array.from(selected),
      );
      toast.success(STRINGS.toasts.bookingSuccess);
      navigate({ to: ROUTES.bookingSuccess(reservation.id) });
    } catch (e) {
      const msg = e instanceof ApiError ? e.message : STRINGS.toasts.bookingFailed;
      toast.error(msg);
    } finally {
      setSubmitting(false);
    }
  };

  if (loading) {
    return (
      <div className="mx-auto max-w-5xl px-4 py-32 text-center">
        <div className="font-display text-3xl text-muted-foreground">
          Loading seats…
        </div>
      </div>
    );
  }

  if (!showtime) {
    return (
      <div className="mx-auto max-w-2xl px-4 py-32 text-center">
        <h1 className="font-display text-5xl">Showtime not found</h1>
        <Link to={ROUTES.movies} className="mt-6 inline-block text-accent">
          ← Back to movies
        </Link>
      </div>
    );
  }

  return (
    <div className="mx-auto max-w-5xl px-4 pb-40 pt-12 sm:px-6 lg:px-8">
      <Link
        to={ROUTES.movie(String(showtime.movieId))}
        className="text-sm text-muted-foreground hover:text-foreground"
      >
        ← Back to movie
      </Link>

      <div className="mt-4 mb-12 text-center">
        <div className="text-xs uppercase tracking-widest text-accent">
          {showtime.date} · {showtime.time} · {showtime.hallName}
        </div>
        <h1 className="mt-2 font-display text-5xl md:text-6xl">
          {STRINGS.booking.selectSeats}
        </h1>
      </div>

      <div className="mb-12 flex flex-col items-center">
        <div className="relative h-20 w-full max-w-2xl">
          <div className="absolute inset-x-0 top-0 mx-auto h-3 rounded-[100%] bg-gradient-to-b from-accent via-accent/40 to-transparent shadow-gold" />
          <div
            className="absolute inset-x-12 top-2 h-16 rounded-[50%] opacity-60 blur-2xl"
            style={{ background: "var(--gold)" }}
          />
        </div>
        <div className="text-xs uppercase tracking-[0.3em] text-muted-foreground">
          {STRINGS.booking.screen}
        </div>
      </div>

      <motion.div
        initial="hidden"
        animate="show"
        variants={{
          hidden: {},
          show: { transition: { staggerChildren: 0.015 } },
        }}
        className="mx-auto w-fit space-y-2.5"
      >
        {rows.map(([row, rowSeats]) => (
          <motion.div
            key={row}
            variants={{
              hidden: { opacity: 0, y: -10 },
              show: { opacity: 1, y: 0 },
            }}
            className="flex items-center gap-2"
          >
            <span className="w-5 text-center text-xs font-medium text-muted-foreground">
              {row}
            </span>
            <div className="flex gap-1.5">
              {rowSeats.map((seat) => (
                <Seat
                  key={seat.id}
                  state={stateOf(seat)}
                  onClick={() => toggle(seat)}
                  label={`${seat.rowLabel}${seat.seatNumber}`}
                />
              ))}
            </div>
            <span className="w-5 text-center text-xs font-medium text-muted-foreground">
              {row}
            </span>
          </motion.div>
        ))}
      </motion.div>

      <div className="mx-auto mt-12 flex max-w-md flex-wrap items-center justify-center gap-4 text-xs text-muted-foreground">
        <Legend className="border-border bg-background" label={STRINGS.booking.available} />
        <Legend className="border-transparent bg-gradient-primary shadow-glow" label={STRINGS.booking.selected} />
        <Legend className="border-transparent bg-gradient-gold shadow-gold" label={STRINGS.booking.premium} />
        <Legend className="border-transparent bg-muted opacity-40" label={STRINGS.booking.booked} />
      </div>

      <motion.div
        initial={{ y: 100 }}
        animate={{ y: 0 }}
        className="fixed inset-x-0 bottom-0 z-40"
      >
        <div className="glass border-t border-border">
          <div className="mx-auto flex max-w-5xl items-center justify-between gap-4 px-4 py-4 sm:px-6 lg:px-8">
            <div>
              <div className="text-xs uppercase tracking-widest text-muted-foreground">
                {STRINGS.booking.seatsCount(selected.size)}
              </div>
              <div className="font-display text-3xl text-gradient-gold">
                ${total.toFixed(2)}
              </div>
            </div>
            <HammerButton
              variant="gold"
              size="lg"
              onClick={confirm}
              loading={submitting}
            >
              {STRINGS.booking.confirmBooking}
            </HammerButton>
          </div>
        </div>
      </motion.div>
    </div>
  );
}

function Seat({
  state,
  onClick,
  label,
}: {
  state: SeatState;
  onClick: () => void;
  label: string;
}) {
  const base =
    "relative h-7 w-7 rounded-md rounded-b-lg text-[9px] font-medium transition-all sm:h-8 sm:w-8";
  const styles: Record<SeatState, string> = {
    available:
      "border border-border bg-background hover:border-accent hover:scale-110",
    selected:
      "bg-gradient-primary text-primary-foreground shadow-glow scale-110",
    booked: "bg-muted opacity-30 cursor-not-allowed",
    premium:
      "border border-accent/40 bg-accent/10 text-accent hover:bg-accent/20 hover:scale-110",
  };
  return (
    <motion.button
      variants={{
        hidden: { opacity: 0, scale: 0.3 },
        show: { opacity: 1, scale: 1 },
      }}
      whileTap={state !== "booked" ? { scale: 0.85 } : undefined}
      onClick={onClick}
      disabled={state === "booked"}
      aria-label={`Seat ${label} — ${state}`}
      className={`${base} ${styles[state]}`}
    />
  );
}

function Legend({ className, label }: { className: string; label: string }) {
  return (
    <span className="flex items-center gap-2">
      <span className={`inline-block h-4 w-4 rounded border ${className}`} />
      {label}
    </span>
  );
}
