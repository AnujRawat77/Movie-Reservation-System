import { createFileRoute, Link, useNavigate } from "@tanstack/react-router";
import { motion } from "framer-motion";
import { useEffect, useState } from "react";
import {
  ArrowLeft,
  Calendar,
  Clock,
  Download,
  MapPin,
  Star,
  Ticket,
  User2,
  XCircle,
} from "lucide-react";
import { toast } from "sonner";
import { ROUTES } from "@/constants/routes";
import { HammerButton } from "@/components/HammerButton";
import {
  reservations as reservationsApi,
  ApiError,
  type ReservationDto,
} from "@/lib/api";
import { useAuth } from "@/hooks/use-auth";

export const Route = createFileRoute("/bookings/$id")({
  head: () => ({ meta: [{ title: "Booking Details — CineReserve" }] }),
  component: BookingDetailPage,
});

function downloadReceipt(reservation: ReservationDto) {
  const ref = `#${reservation.id.slice(-6).toUpperCase()}`;
  const seatList = reservation.seats
    .map((s) => `${s.rowLabel}${s.seatNumber} (${s.seatType})`)
    .join(", ");
  const html = `<!DOCTYPE html>
<html><head>
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
</head><body>
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
</body></html>`;
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

function formatBookedOn(createdAt: string): string {
  try {
    return new Date(createdAt).toLocaleString("en-US", {
      dateStyle: "medium",
      timeStyle: "short",
    });
  } catch {
    return createdAt;
  }
}

function BookingDetailPage() {
  const { id } = Route.useParams();
  const { isAuthenticated, hydrated } = useAuth();
  const navigate = useNavigate();
  const [reservation, setReservation] = useState<ReservationDto | null>(null);
  const [loading, setLoading] = useState(true);
  const [cancelling, setCancelling] = useState(false);

  useEffect(() => {
    if (hydrated && !isAuthenticated) navigate({ to: ROUTES.login });
  }, [hydrated, isAuthenticated, navigate]);

  useEffect(() => {
    if (!isAuthenticated) return;
    reservationsApi
      .get(id)
      .then(setReservation)
      .catch((e) => {
        if (e instanceof ApiError && e.status !== 401) toast.error(e.message);
      })
      .finally(() => setLoading(false));
  }, [id, isAuthenticated]);

  const handleCancel = async () => {
    if (!reservation) return;
    setCancelling(true);
    try {
      const updated = await reservationsApi.cancel(reservation.id);
      const refundMsg = updated.refundPercentage
        ? `Refund: $${Number(updated.refundAmount).toFixed(2)} (${updated.refundPercentage}%)`
        : "No refund (less than 2 hours before showtime)";
      toast.success(`Reservation cancelled. ${refundMsg}`);
      setReservation(updated);
    } catch (e) {
      toast.error(e instanceof ApiError ? e.message : "Cancel failed");
    } finally {
      setCancelling(false);
    }
  };

  if (!isAuthenticated || loading) {
    return (
      <div className="mx-auto max-w-3xl px-4 py-24 text-center font-display text-3xl text-muted-foreground">
        Loading…
      </div>
    );
  }

  if (!reservation) {
    return (
      <div className="mx-auto flex min-h-[70dvh] max-w-2xl flex-col items-center justify-center px-4 py-24 text-center">
        <Ticket className="mb-4 h-12 w-12 text-muted-foreground" />
        <h1 className="font-display text-4xl">Booking not found</h1>
        <Link to={ROUTES.myBookings} className="mt-6 text-sm text-accent underline">
          ← Back to My Bookings
        </Link>
      </div>
    );
  }

  const ref = `#${reservation.id.slice(-6).toUpperCase()}`;
  const isConfirmed = reservation.status === "CONFIRMED";
  const premiumCount = reservation.seats.filter((s) => s.seatType === "PREMIUM").length;

  return (
    <div className="mx-auto max-w-3xl px-4 pb-24 pt-10 sm:px-6 lg:px-8">
      {/* Back navigation */}
      <Link
        to={ROUTES.myBookings}
        className="mb-8 inline-flex items-center gap-2 text-sm text-muted-foreground transition-colors hover:text-foreground"
      >
        <ArrowLeft className="h-4 w-4" /> My Bookings
      </Link>

      <motion.div
        initial={{ opacity: 0, y: 20 }}
        animate={{ opacity: 1, y: 0 }}
        className="space-y-5"
      >
        {/* Hero card — title + status */}
        <div className="rounded-2xl border border-border bg-card/80 p-8 shadow-elegant backdrop-blur">
          <div className="flex flex-wrap items-start justify-between gap-4">
            <div>
              <div className="mb-1 text-xs uppercase tracking-widest text-muted-foreground">
                Booking {ref}
              </div>
              <h1 className="font-display text-4xl leading-tight md:text-5xl">
                {reservation.movieTitle}
              </h1>
            </div>
            <span
              className={`mt-1 rounded-full px-4 py-1.5 text-[10px] font-medium uppercase tracking-widest ${
                isConfirmed
                  ? "bg-accent/15 text-accent"
                  : "bg-muted text-muted-foreground"
              }`}
            >
              {reservation.status}
            </span>
          </div>

          {/* Show details grid */}
          <div className="mt-6 grid grid-cols-1 gap-3 sm:grid-cols-3">
            {[
              { icon: Calendar, label: "Date", value: reservation.showDate },
              { icon: Clock,    label: "Time", value: reservation.showTime },
              { icon: MapPin,   label: "Hall", value: reservation.hallName },
            ].map(({ icon: Icon, label, value }) => (
              <div
                key={label}
                className="flex items-center gap-3 rounded-xl border border-border/60 bg-background/50 p-4"
              >
                <Icon className="h-5 w-5 shrink-0 text-accent" />
                <div>
                  <div className="text-[10px] uppercase tracking-widest text-muted-foreground">
                    {label}
                  </div>
                  <div className="text-sm font-medium">{value}</div>
                </div>
              </div>
            ))}
          </div>
        </div>

        {/* Seats card */}
        <div className="rounded-2xl border border-border bg-card/80 p-6 shadow-elegant backdrop-blur">
          <div className="mb-4 flex items-center gap-2">
            <Ticket className="h-4 w-4 text-accent" />
            <h2 className="text-sm font-semibold uppercase tracking-widest">
              Seats ({reservation.seats.length})
            </h2>
          </div>
          <div className="flex flex-wrap gap-2">
            {reservation.seats.map((seat, idx) => (
              <div
                key={idx}
                className={`flex items-center gap-1.5 rounded-lg border px-3 py-2 text-sm ${
                  seat.seatType === "PREMIUM"
                    ? "border-amber-500/40 bg-amber-500/10 text-amber-400"
                    : "border-border bg-background/60 text-foreground"
                }`}
              >
                {seat.seatType === "PREMIUM" && (
                  <Star className="h-3 w-3 fill-amber-400 text-amber-400" />
                )}
                <span className="font-mono font-medium">
                  {seat.rowLabel}{seat.seatNumber}
                </span>
                <span className="text-[10px] uppercase tracking-wider opacity-60">
                  {seat.seatType === "PREMIUM" ? "Premium" : "Regular"}
                </span>
              </div>
            ))}
          </div>
          {premiumCount > 0 && (
            <p className="mt-3 text-xs text-muted-foreground">
              ★ {premiumCount} premium seat{premiumCount > 1 ? "s" : ""} include 1.5× pricing
            </p>
          )}
        </div>

        {/* Price + meta card */}
        <div className="rounded-2xl border border-border bg-card/80 p-6 shadow-elegant backdrop-blur">
          <div className="flex items-center justify-between">
            <div>
              <div className="text-xs uppercase tracking-widest text-muted-foreground">
                Total Paid
              </div>
              <div className="font-display text-4xl text-gradient-gold">
                ${Number(reservation.totalAmount).toFixed(2)}
              </div>
            </div>
            <div className="space-y-1 text-right text-xs text-muted-foreground">
              <div className="flex items-center justify-end gap-1.5">
                <User2 className="h-3.5 w-3.5" /> {reservation.userName}
              </div>
              <div>Booked on {formatBookedOn(reservation.createdAt)}</div>
            </div>
          </div>

          {reservation.status === "CANCELLED" && reservation.refundAmount != null && (
            <div className="mt-4 rounded-lg border border-amber-500/30 bg-amber-500/5 p-3">
              <div className="flex items-center justify-between text-sm">
                <span className="text-muted-foreground">Refund ({reservation.refundPercentage}%)</span>
                <span className="font-medium text-amber-400">
                  ${Number(reservation.refundAmount).toFixed(2)}
                </span>
              </div>
              {reservation.cancelledAt && (
                <div className="mt-1 text-xs text-muted-foreground">
                  Cancelled on {formatBookedOn(reservation.cancelledAt)}
                </div>
              )}
            </div>
          )}
        </div>

        {/* Action buttons */}
        <div className="flex flex-wrap gap-3">
          <HammerButton
            variant="outline"
            size="sm"
            onClick={() => downloadReceipt(reservation)}
          >
            <Download className="mr-2 h-4 w-4" /> Download Receipt
          </HammerButton>
          {isConfirmed && (
            <button
              onClick={handleCancel}
              disabled={cancelling}
              className="inline-flex items-center gap-1.5 rounded-lg border border-destructive/40 px-4 py-2 text-sm text-destructive transition-colors hover:bg-destructive/10 disabled:opacity-50"
            >
              <XCircle className="h-4 w-4" />
              {cancelling ? "Cancelling…" : "Cancel Booking"}
            </button>
          )}
        </div>
      </motion.div>
    </div>
  );
}
