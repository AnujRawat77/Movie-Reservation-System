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

function downloadReceipt(reservation: ReservationDto) {
  const ref = `#${reservation.id.slice(-6).toUpperCase()}`;
  const seatList = reservation.seats
    .map((s) => `${s.rowLabel}${s.seatNumber}`)
    .join(", ");
  const html = `<!DOCTYPE html>
<html>
<head>
  <meta charset="UTF-8" />
  <title>CineReserve Receipt ${ref}</title>
  <style>
    body{font-family:Arial,sans-serif;max-width:480px;margin:40px auto;padding:24px;border:1px solid #ddd;}
    h1{font-size:24px;margin-bottom:4px;}
    .sub{color:#888;font-size:12px;text-transform:uppercase;letter-spacing:1px;}
    hr{border:none;border-top:1px dashed #ccc;margin:16px 0;}
    table{width:100%;border-collapse:collapse;font-size:14px;}
    td{padding:6px 0;} td:first-child{color:#888;width:130px;}
    .total{font-size:20px;font-weight:bold;margin-top:12px;}
    .footer{text-align:center;color:#aaa;font-size:11px;margin-top:24px;}
  </style>
</head>
<body>
  <h1>CineReserve</h1>
  <div class="sub">Booking Receipt</div>
  <hr />
  <table>
    <tr><td>Movie</td><td>${reservation.movieTitle}</td></tr>
    <tr><td>Hall</td><td>${reservation.hallName}</td></tr>
    <tr><td>Date</td><td>${reservation.showDate}</td></tr>
    <tr><td>Time</td><td>${reservation.showTime}</td></tr>
    <tr><td>Seats</td><td>${seatList}</td></tr>
    <tr><td>Booking Ref</td><td>${ref}</td></tr>
    <tr><td>Status</td><td>${reservation.status}</td></tr>
  </table>
  <hr />
  <div class="total">Total: $${Number(reservation.totalAmount).toFixed(2)}</div>
  <div class="footer">Thank you for booking with CineReserve!</div>
</body>
</html>`;
  const blob = new Blob([html], { type: "text/html" });
  const url = URL.createObjectURL(blob);
  const a = document.createElement("a");
  a.href = url;
  a.download = `receipt-${reservation.id.slice(-6).toUpperCase()}.html`;
  document.body.appendChild(a);
  a.click();
  document.body.removeChild(a);
  URL.revokeObjectURL(url);
}

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
        <HammerButton
          variant="secondary"
          size="md"
          onClick={() => reservation && downloadReceipt(reservation)}
          disabled={!reservation}
        >
          <Download className="h-4 w-4" /> Download Receipt
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
