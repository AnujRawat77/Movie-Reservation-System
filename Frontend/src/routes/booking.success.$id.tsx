import { createFileRoute, Link } from "@tanstack/react-router";
import { motion } from "framer-motion";
import { useEffect, useState } from "react";
import confetti from "canvas-confetti";
import { Check, Calendar, Download } from "lucide-react";
import { reservations as reservationsApi, type ReservationDto } from "@/lib/api";
import { ROUTES } from "@/constants/routes";
import { STRINGS } from "@/constants/strings";
import { HammerButton } from "@/components/HammerButton";

export const Route = createFileRoute("/booking/success/$id")({
  head: () => ({ meta: [{ title: "Booking confirmed — CineReserve" }] }),
  component: Success,
});

function Success() {
  const { id } = Route.useParams();
  const [reservation, setReservation] = useState<ReservationDto | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const colors = ["#e63946", "#f4c95d", "#ffffff"];
    confetti({ particleCount: 90, spread: 70, origin: { y: 0.4 }, colors });
    setTimeout(
      () =>
        confetti({
          particleCount: 60,
          spread: 100,
          origin: { y: 0.4 },
          colors,
        }),
      300,
    );
  }, []);

  useEffect(() => {
    let cancelled = false;
    (async () => {
      try {
        const data = await reservationsApi.get(id);
        if (!cancelled) setReservation(data);
      } catch {
        // ignore - show fallback
      } finally {
        if (!cancelled) setLoading(false);
      }
    })();
    return () => {
      cancelled = true;
    };
  }, [id]);

  if (loading) {
    return (
      <div className="mx-auto max-w-2xl px-4 py-32 text-center font-display text-3xl text-muted-foreground">
        Confirming…
      </div>
    );
  }

  return (
    <div className="mx-auto flex min-h-[80dvh] max-w-2xl flex-col items-center justify-center px-4 py-16 text-center">
      <motion.div
        initial={{ scale: 0, rotate: -180 }}
        animate={{ scale: 1, rotate: 0 }}
        transition={{ type: "spring", stiffness: 200, damping: 15 }}
        className="mb-8 grid h-24 w-24 place-items-center rounded-full bg-gradient-gold shadow-gold"
      >
        <Check className="h-12 w-12 text-gold-foreground" strokeWidth={3} />
      </motion.div>

      <motion.h1
        initial={{ opacity: 0, y: 16 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ delay: 0.3 }}
        className="font-display text-5xl md:text-7xl"
      >
        {STRINGS.booking.successTitle}
      </motion.h1>
      <motion.p
        initial={{ opacity: 0 }}
        animate={{ opacity: 1 }}
        transition={{ delay: 0.5 }}
        className="mt-4 max-w-md text-muted-foreground"
      >
        {STRINGS.booking.successSub}
      </motion.p>

      {reservation && (
        <motion.div
          initial={{ opacity: 0, y: 40 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ delay: 0.6 }}
          className="relative mt-12 w-full max-w-md overflow-hidden rounded-3xl border border-border bg-card shadow-elegant"
        >
          <div className="relative p-6">
            <div className="font-display text-3xl">{reservation.movieTitle}</div>
            <div className="text-xs uppercase tracking-widest text-accent">
              {reservation.hallName}
            </div>
          </div>
          <div className="relative h-4 border-y border-dashed border-border bg-card">
            <div className="absolute -left-2 top-1/2 h-4 w-4 -translate-y-1/2 rounded-full bg-background" />
            <div className="absolute -right-2 top-1/2 h-4 w-4 -translate-y-1/2 rounded-full bg-background" />
          </div>
          <div className="grid grid-cols-3 gap-2 p-5 text-left text-xs">
            <Info label="Date" value={reservation.showDate} />
            <Info label="Time" value={reservation.showTime} />
            <Info
              label="Booking"
              value={`#${reservation.id.slice(-6).toUpperCase()}`}
            />
            <Info
              label="Seats"
              value={reservation.seats
                .map((s) => `${s.rowLabel}${s.seatNumber}`)
                .join(", ")}
            />
            <Info
              label="Total"
              value={`$${Number(reservation.totalAmount).toFixed(2)}`}
            />
            <Info label="Status" value={reservation.status} />
          </div>
        </motion.div>
      )}

      <div className="mt-10 flex flex-wrap justify-center gap-3">
        <HammerButton variant="secondary" size="md">
          <Calendar className="h-4 w-4" /> Add to Calendar
        </HammerButton>
        <HammerButton variant="secondary" size="md">
          <Download className="h-4 w-4" /> Download
        </HammerButton>
        <Link to={ROUTES.myBookings}>
          <HammerButton variant="gold" size="md">
            View My Bookings
          </HammerButton>
        </Link>
      </div>
    </div>
  );
}

function Info({ label, value }: { label: string; value: string }) {
  return (
    <div>
      <div className="text-[10px] uppercase tracking-widest text-muted-foreground">
        {label}
      </div>
      <div className="mt-0.5 font-medium">{value}</div>
    </div>
  );
}
