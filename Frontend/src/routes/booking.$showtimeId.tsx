import { createFileRoute, Link, useNavigate } from "@tanstack/react-router";
import { motion } from "framer-motion";
import { useCallback, useEffect, useMemo, useRef, useState } from "react";
import { toast } from "sonner";
import {
  holds as holdsApi,
  showtimes as showtimesApi,
  ApiError,
  type SeatDto,
  type SeatHoldDto,
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

type SeatState = "available" | "selected" | "booked" | "premium" | "held-by-other";

const POLL_INTERVAL_MS = 3000;

function SeatSelection() {
  const { showtimeId } = Route.useParams();
  const { isAuthenticated, hydrated } = useAuth();
  const navigate = useNavigate();

  const [seats, setSeats] = useState<SeatDto[]>([]);
  const [showtime, setShowtime] = useState<ShowtimeDto | null>(null);
  const [loading, setLoading] = useState(true);
  const [selected, setSelected] = useState<Set<number>>(new Set());
  const [submitting, setSubmitting] = useState(false);

  // Hold state
  const [hold, setHold] = useState<SeatHoldDto | null>(null);
  const [countdown, setCountdown] = useState(0);

  const pollRef = useRef<ReturnType<typeof setInterval> | null>(null);
  const countdownRef = useRef<ReturnType<typeof setInterval> | null>(null);
  const holdRef = useRef<SeatHoldDto | null>(null);
  holdRef.current = hold;

  useEffect(() => {
    if (hydrated && !isAuthenticated) {
      toast.error("Please sign in to book seats");
      navigate({ to: ROUTES.login });
    }
  }, [hydrated, isAuthenticated, navigate]);

  // Initial load
  useEffect(() => {
    let cancelled = false;
    (async () => {
      try {
        const [s, st] = await Promise.all([
          showtimesApi.seatMap(showtimeId),
          showtimesApi.get(showtimeId).catch(() => null),
        ]);
        if (cancelled) return;
        setSeats(s);
        setShowtime(st);
      } catch (e) {
        if (!cancelled) {
          toast.error(e instanceof ApiError ? e.message : "Failed to load seats");
        }
      } finally {
        if (!cancelled) setLoading(false);
      }
    })();
    return () => { cancelled = true; };
  }, [showtimeId, isAuthenticated]);

  // Polling for live seat-map updates
  useEffect(() => {
    if (!isAuthenticated) return;
    pollRef.current = setInterval(async () => {
      try {
        const fresh = await showtimesApi.seatMap(showtimeId);
        setSeats((prev) => {
          // Merge: preserve our locally-selected state, update server statuses
          const heldByMe = new Set(
            fresh.filter((s) => s.status === "HELD_BY_ME").map((s) => s.id)
          );
          setSelected((sel) => {
            const next = new Set(sel);
            // Auto-deselect seats that were lost to another user
            sel.forEach((id) => {
              const serverSeat = fresh.find((s) => s.id === id);
              if (serverSeat && serverSeat.status === "HELD_BY_OTHER") {
                next.delete(id);
                toast.warning(
                  `Seat ${serverSeat.rowLabel}${serverSeat.seatNumber} was taken by another user`
                );
              }
            });
            return next;
          });
          return fresh;
        });
      } catch {
        // silent — polling failure shouldn't disrupt user
      }
    }, POLL_INTERVAL_MS);

    return () => {
      if (pollRef.current) clearInterval(pollRef.current);
    };
  }, [showtimeId, isAuthenticated]);

  // Countdown timer
  useEffect(() => {
    if (countdownRef.current) clearInterval(countdownRef.current);
    if (!hold || hold.status !== "ACTIVE") return;

    setCountdown(hold.expiresInSeconds);
    countdownRef.current = setInterval(() => {
      setCountdown((prev) => {
        if (prev <= 1) {
          clearInterval(countdownRef.current!);
          setHold((h) => (h ? { ...h, status: "EXPIRED" } : h));
          toast.error("Your seat hold has expired. Please re-select your seats.");
          return 0;
        }
        return prev - 1;
      });
    }, 1000);

    return () => { if (countdownRef.current) clearInterval(countdownRef.current); };
  }, [hold?.holdId, hold?.status]);

  // Release hold on page leave
  useEffect(() => {
    return () => {
      const current = holdRef.current;
      if (current && current.status === "ACTIVE") {
        holdsApi.release(current.holdId).catch(() => {});
      }
    };
  }, []);

  const rows = useMemo(() => {
    const map = new Map<string, SeatDto[]>();
    seats.forEach((s) => {
      const arr = map.get(s.rowLabel) ?? [];
      arr.push(s);
      map.set(s.rowLabel, arr);
    });
    return Array.from(map.entries())
      .sort(([a], [b]) => a.localeCompare(b))
      .map(([label, list]) => [label, list.sort((a, b) => a.seatNumber - b.seatNumber)]) as Array<[string, SeatDto[]]>;
  }, [seats]);

  const seatById = useMemo(() => {
    const m = new Map<number, SeatDto>();
    seats.forEach((s) => m.set(s.id, s));
    return m;
  }, [seats]);

  const toggle = useCallback((seat: SeatDto) => {
    if (seat.status === "BOOKED" || seat.status === "HELD_BY_OTHER") {
      toast.error(seat.status === "BOOKED" ? STRINGS.toasts.seatTaken : "This seat is currently held by another user");
      return;
    }
    // If a hold already exists, release it before allowing re-selection
    if (hold && hold.status === "ACTIVE") {
      holdsApi.release(hold.holdId).catch(() => {});
      setHold(null);
    }
    setSelected((prev) => {
      const next = new Set(prev);
      if (next.has(seat.id)) next.delete(seat.id);
      else next.add(seat.id);
      return next;
    });
  }, [hold]);

  const stateOf = (seat: SeatDto): SeatState => {
    if (seat.status === "BOOKED") return "booked";
    if (seat.status === "HELD_BY_OTHER") return "held-by-other";
    if (seat.status === "HELD_BY_ME" || selected.has(seat.id)) return "selected";
    if (seat.seatType === "PREMIUM") return "premium";
    return "available";
  };

  const total = useMemo(() => {
    if (!showtime) return 0;
    if (hold && hold.status === "ACTIVE") return hold.totalAmount;
    return Array.from(selected).reduce((sum, id) => {
      const seat = seatById.get(id);
      if (!seat) return sum;
      const base = Number(showtime.price);
      return sum + (seat.seatType === "PREMIUM" ? base * 1.5 : base);
    }, 0);
  }, [selected, seatById, showtime, hold]);

  // Step 1: Create hold
  const requestHold = async () => {
    if (selected.size === 0) {
      toast.warning(STRINGS.booking.pickAtLeastOne);
      return;
    }
    setSubmitting(true);
    try {
      const h = await holdsApi.create(Number(showtimeId), Array.from(selected));
      setHold(h);
      toast.success(`Seats held for ${Math.ceil(h.expiresInSeconds / 60)} minutes!`);
    } catch (e) {
      const msg = e instanceof ApiError ? e.message : STRINGS.toasts.bookingFailed;
      toast.error(msg);
    } finally {
      setSubmitting(false);
    }
  };

  // Step 2: Navigate to payment confirmation
  const proceedToPayment = () => {
    if (!hold || hold.status !== "ACTIVE" || countdown <= 0) {
      toast.error("Your hold has expired. Please re-select seats.");
      setHold(null);
      return;
    }
    navigate({ to: `/booking/confirm/${hold.holdId}` });
  };

  if (loading) {
    return (
      <div className="mx-auto max-w-5xl px-4 py-32 text-center">
        <div className="font-display text-3xl text-muted-foreground">Loading seats…</div>
      </div>
    );
  }

  if (!showtime) {
    return (
      <div className="mx-auto max-w-2xl px-4 py-32 text-center">
        <h1 className="font-display text-5xl">Showtime not found</h1>
        <Link to={ROUTES.movies} className="mt-6 inline-block text-accent">← Back to movies</Link>
      </div>
    );
  }

  const holdActive = hold && hold.status === "ACTIVE" && countdown > 0;

  return (
    <div className="mx-auto max-w-5xl px-4 pb-40 pt-12 sm:px-6 lg:px-8">
      <Link to={ROUTES.movie(String(showtime.movieId))} className="text-sm text-muted-foreground hover:text-foreground">
        ← Back to movie
      </Link>

      <div className="mt-4 mb-12 text-center">
        <div className="text-xs uppercase tracking-widest text-accent">
          {showtime.date} · {showtime.time} · {showtime.hallName}
        </div>
        <h1 className="mt-2 font-display text-5xl md:text-6xl">{STRINGS.booking.selectSeats}</h1>

        {holdActive && (
          <motion.div
            initial={{ opacity: 0, y: -8 }}
            animate={{ opacity: 1, y: 0 }}
            className="mt-4 inline-flex items-center gap-2 rounded-full border border-accent/40 bg-accent/10 px-4 py-1.5 text-sm text-accent"
          >
            <span className="h-2 w-2 rounded-full bg-accent animate-pulse" />
            Seats held — {Math.floor(countdown / 60)}:{String(countdown % 60).padStart(2, "0")} remaining
          </motion.div>
        )}

        {hold && hold.status === "EXPIRED" && (
          <div className="mt-4 rounded-lg border border-destructive/40 bg-destructive/10 p-3 text-sm text-destructive">
            Hold expired. Re-select your seats and try again.
          </div>
        )}
      </div>

      {/* Screen */}
      <div className="mb-12 flex flex-col items-center">
        <div className="relative h-20 w-full max-w-2xl">
          <div className="absolute inset-x-0 top-0 mx-auto h-3 rounded-[100%] bg-gradient-to-b from-accent via-accent/40 to-transparent shadow-gold" />
          <div className="absolute inset-x-12 top-2 h-16 rounded-[50%] opacity-60 blur-2xl" style={{ background: "var(--gold)" }} />
        </div>
        <div className="text-xs uppercase tracking-[0.3em] text-muted-foreground">{STRINGS.booking.screen}</div>
      </div>

      <motion.div
        initial="hidden"
        animate="show"
        variants={{ hidden: {}, show: { transition: { staggerChildren: 0.015 } } }}
        className="mx-auto w-fit space-y-2.5"
      >
        {rows.map(([row, rowSeats]) => (
          <motion.div
            key={row}
            variants={{ hidden: { opacity: 0, y: -10 }, show: { opacity: 1, y: 0 } }}
            className="flex items-center gap-2"
          >
            <span className="w-5 text-center text-xs font-medium text-muted-foreground">{row}</span>
            <div className="flex gap-1.5">
              {rowSeats.map((seat) => (
                <Seat key={seat.id} state={stateOf(seat)} onClick={() => toggle(seat)} label={`${seat.rowLabel}${seat.seatNumber}`} />
              ))}
            </div>
            <span className="w-5 text-center text-xs font-medium text-muted-foreground">{row}</span>
          </motion.div>
        ))}
      </motion.div>

      {/* Legend */}
      <div className="mx-auto mt-12 flex max-w-md flex-wrap items-center justify-center gap-4 text-xs text-muted-foreground">
        <Legend className="border-border bg-background" label={STRINGS.booking.available} />
        <Legend className="border-transparent bg-gradient-primary shadow-glow" label={STRINGS.booking.selected} />
        <Legend className="border-transparent bg-gradient-gold shadow-gold" label={STRINGS.booking.premium} />
        <Legend className="border-transparent bg-muted opacity-40" label={STRINGS.booking.booked} />
        <Legend className="border-orange-400/60 bg-orange-400/20" label="Held by other" />
      </div>

      {/* Bottom bar */}
      <motion.div initial={{ y: 100 }} animate={{ y: 0 }} className="fixed inset-x-0 bottom-0 z-40">
        <div className="glass border-t border-border">
          <div className="mx-auto flex max-w-5xl items-center justify-between gap-4 px-4 py-4 sm:px-6 lg:px-8">
            <div>
              <div className="text-xs uppercase tracking-widest text-muted-foreground">
                {holdActive
                  ? `${hold.seats.length} seat${hold.seats.length !== 1 ? "s" : ""} held`
                  : STRINGS.booking.seatsCount(selected.size)}
              </div>
              <div className="font-display text-3xl text-gradient-gold">${Number(total).toFixed(2)}</div>
            </div>

            {holdActive ? (
              <HammerButton variant="gold" size="lg" onClick={proceedToPayment}>
                Confirm Ticket →
              </HammerButton>
            ) : (
              <HammerButton
                variant="gold"
                size="lg"
                onClick={requestHold}
                loading={submitting}
                disabled={selected.size === 0}
              >
                Hold Seats
              </HammerButton>
            )}
          </div>
        </div>
      </motion.div>
    </div>
  );
}

function Seat({ state, onClick, label }: { state: SeatState; onClick: () => void; label: string }) {
  const base = "relative h-7 w-7 rounded-md rounded-b-lg text-[9px] font-medium transition-all sm:h-8 sm:w-8";
  const styles: Record<SeatState, string> = {
    available: "border border-border bg-background hover:border-accent hover:scale-110",
    selected: "bg-gradient-primary text-primary-foreground shadow-glow scale-110",
    booked: "bg-muted opacity-30 cursor-not-allowed",
    premium: "border border-accent/40 bg-accent/10 text-accent hover:bg-accent/20 hover:scale-110",
    "held-by-other": "border border-orange-400/60 bg-orange-400/20 opacity-70 cursor-not-allowed",
  };
  const disabled = state === "booked" || state === "held-by-other";
  return (
    <motion.button
      variants={{ hidden: { opacity: 0, scale: 0.3 }, show: { opacity: 1, scale: 1 } }}
      whileTap={!disabled ? { scale: 0.85 } : undefined}
      onClick={onClick}
      disabled={disabled}
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
