import { createFileRoute, Link, useNavigate } from "@tanstack/react-router";
import { motion } from "framer-motion";
import { useEffect, useState } from "react";
import { toast } from "sonner";
import { CreditCard, Lock, ShieldCheck, Ticket } from "lucide-react";
import { holds as holdsApi, ApiError, type SeatHoldDto } from "@/lib/api";
import { ROUTES } from "@/constants/routes";
import { HammerButton } from "@/components/HammerButton";
import { useAuth } from "@/hooks/use-auth";

export const Route = createFileRoute("/booking/confirm/$holdId")({
  head: () => ({ meta: [{ title: "Confirm payment — CineReserve" }] }),
  component: ConfirmPayment,
});

function ConfirmPayment() {
  const { holdId } = Route.useParams();
  const { isAuthenticated, hydrated } = useAuth();
  const navigate = useNavigate();

  const [hold, setHold] = useState<SeatHoldDto | null>(null);
  const [loading, setLoading] = useState(true);
  const [paying, setPaying] = useState(false);
  const [countdown, setCountdown] = useState(0);

  // Payment form state
  const [cardHolder, setCardHolder] = useState("");
  const [cardNumber, setCardNumber] = useState("");
  const [expiry, setExpiry] = useState("");
  const [cvv, setCvv] = useState("");

  useEffect(() => {
    if (hydrated && !isAuthenticated) {
      navigate({ to: ROUTES.login });
    }
  }, [hydrated, isAuthenticated, navigate]);

  useEffect(() => {
    holdsApi.get(holdId)
      .then((h) => {
        setHold(h);
        setCountdown(h.expiresInSeconds);
      })
      .catch(() => toast.error("Hold not found or expired"))
      .finally(() => setLoading(false));
  }, [holdId]);

  // Countdown
  useEffect(() => {
    if (!hold || hold.status !== "ACTIVE") return;
    const timer = setInterval(() => {
      setCountdown((prev) => {
        if (prev <= 1) {
          clearInterval(timer);
          setHold((h) => (h ? { ...h, status: "EXPIRED" } : h));
          toast.error("Hold expired — returning to seat selection");
          navigate({ to: `/booking/${hold.showtimeId}` });
          return 0;
        }
        return prev - 1;
      });
    }, 1000);
    return () => clearInterval(timer);
  }, [hold?.holdId, hold?.status]);

  const formatCard = (val: string) =>
    val.replace(/\D/g, "").slice(0, 16).replace(/(.{4})/g, "$1 ").trim();

  const formatExpiry = (val: string) => {
    const digits = val.replace(/\D/g, "").slice(0, 4);
    return digits.length > 2 ? `${digits.slice(0, 2)}/${digits.slice(2)}` : digits;
  };

  const isFormValid =
    cardHolder.trim().length >= 2 &&
    cardNumber.replace(/\s/g, "").length === 16 &&
    expiry.length === 5 &&
    cvv.length >= 3;

  const handlePay = async () => {
    if (!isFormValid) {
      toast.error("Please fill in all payment details correctly");
      return;
    }
    if (!hold || hold.status !== "ACTIVE" || countdown <= 0) {
      toast.error("Hold has expired");
      return;
    }
    setPaying(true);
    try {
      const reservation = await holdsApi.confirm(holdId);
      toast.success("Payment successful! Booking confirmed 🎉");
      navigate({ to: ROUTES.bookingSuccess(reservation.id) });
    } catch (e) {
      const msg = e instanceof ApiError ? e.message : "Payment failed. Please try again.";
      toast.error(msg);
    } finally {
      setPaying(false);
    }
  };

  if (loading) {
    return (
      <div className="mx-auto max-w-2xl px-4 py-32 text-center">
        <div className="font-display text-3xl text-muted-foreground">Loading…</div>
      </div>
    );
  }

  if (!hold) {
    return (
      <div className="mx-auto max-w-2xl px-4 py-32 text-center">
        <h1 className="font-display text-4xl">Hold not found</h1>
        <Link to={ROUTES.movies} className="mt-6 inline-block text-accent">← Browse movies</Link>
      </div>
    );
  }

  const holdActive = hold.status === "ACTIVE" && countdown > 0;
  const mins = Math.floor(countdown / 60);
  const secs = String(countdown % 60).padStart(2, "0");

  return (
    <div className="mx-auto max-w-4xl px-4 pb-24 pt-12 sm:px-6 lg:px-8">
      <Link to={`/booking/${hold.showtimeId}`} className="text-sm text-muted-foreground hover:text-foreground">
        ← Back to seat selection
      </Link>

      <div className="mt-6 mb-10 text-center">
        <div className="text-xs uppercase tracking-widest text-accent">Confirm your booking</div>
        <h1 className="mt-2 font-display text-5xl md:text-6xl">Payment</h1>

        {holdActive ? (
          <motion.div
            initial={{ opacity: 0, y: -6 }}
            animate={{ opacity: 1, y: 0 }}
            className="mt-4 inline-flex items-center gap-2 rounded-full border border-accent/40 bg-accent/10 px-4 py-1.5 text-sm text-accent"
          >
            <span className="h-2 w-2 animate-pulse rounded-full bg-accent" />
            Hold expires in {mins}:{secs}
          </motion.div>
        ) : (
          <div className="mt-4 rounded-lg border border-destructive/40 bg-destructive/10 p-3 text-sm text-destructive">
            Hold expired — <Link to={`/booking/${hold.showtimeId}`} className="underline">go back and re-select seats</Link>
          </div>
        )}
      </div>

      <div className="grid gap-8 lg:grid-cols-2">
        {/* Order Summary */}
        <motion.div
          initial={{ opacity: 0, x: -20 }}
          animate={{ opacity: 1, x: 0 }}
          className="rounded-2xl border border-border bg-card p-6"
        >
          <div className="mb-4 flex items-center gap-2 text-accent">
            <Ticket className="h-5 w-5" />
            <span className="text-sm font-medium uppercase tracking-wider">Order Summary</span>
          </div>

          <h2 className="font-display text-2xl">{hold.movieTitle}</h2>
          <p className="mt-1 text-sm text-muted-foreground">
            {hold.showDate} · {hold.showTime} · {hold.hallName}
          </p>

          <div className="mt-4 space-y-2">
            {hold.seats.map((s) => (
              <div key={s.seatId} className="flex items-center justify-between text-sm">
                <span className="text-muted-foreground">
                  Seat {s.rowLabel}{s.seatNumber}
                  <span className="ml-2 rounded bg-muted px-1.5 py-0.5 text-[10px] uppercase">{s.seatType}</span>
                </span>
              </div>
            ))}
          </div>

          <div className="mt-6 border-t border-border pt-4">
            <div className="flex items-center justify-between">
              <span className="text-sm text-muted-foreground">Total</span>
              <span className="font-display text-3xl text-gradient-gold">${Number(hold.totalAmount).toFixed(2)}</span>
            </div>
          </div>
        </motion.div>

        {/* Payment Form */}
        <motion.div
          initial={{ opacity: 0, x: 20 }}
          animate={{ opacity: 1, x: 0 }}
          className="rounded-2xl border border-border bg-card p-6"
        >
          <div className="mb-4 flex items-center gap-2 text-accent">
            <CreditCard className="h-5 w-5" />
            <span className="text-sm font-medium uppercase tracking-wider">Payment Details</span>
          </div>

          <div className="space-y-4">
            <div>
              <label className="mb-1 block text-xs text-muted-foreground">Card Holder Name</label>
              <input
                type="text"
                value={cardHolder}
                onChange={(e) => setCardHolder(e.target.value)}
                placeholder="John Smith"
                disabled={!holdActive || paying}
                className="w-full rounded-lg border border-border bg-background px-4 py-2.5 text-sm outline-none focus:border-accent disabled:opacity-50"
              />
            </div>

            <div>
              <label className="mb-1 block text-xs text-muted-foreground">Card Number</label>
              <input
                type="text"
                value={cardNumber}
                onChange={(e) => setCardNumber(formatCard(e.target.value))}
                placeholder="1234 5678 9012 3456"
                maxLength={19}
                disabled={!holdActive || paying}
                className="w-full rounded-lg border border-border bg-background px-4 py-2.5 text-sm font-mono outline-none focus:border-accent disabled:opacity-50"
              />
            </div>

            <div className="grid grid-cols-2 gap-3">
              <div>
                <label className="mb-1 block text-xs text-muted-foreground">Expiry (MM/YY)</label>
                <input
                  type="text"
                  value={expiry}
                  onChange={(e) => setExpiry(formatExpiry(e.target.value))}
                  placeholder="12/27"
                  maxLength={5}
                  disabled={!holdActive || paying}
                  className="w-full rounded-lg border border-border bg-background px-4 py-2.5 text-sm font-mono outline-none focus:border-accent disabled:opacity-50"
                />
              </div>
              <div>
                <label className="mb-1 block text-xs text-muted-foreground">CVV</label>
                <input
                  type="password"
                  value={cvv}
                  onChange={(e) => setCvv(e.target.value.replace(/\D/g, "").slice(0, 4))}
                  placeholder="•••"
                  maxLength={4}
                  disabled={!holdActive || paying}
                  className="w-full rounded-lg border border-border bg-background px-4 py-2.5 text-sm font-mono outline-none focus:border-accent disabled:opacity-50"
                />
              </div>
            </div>
          </div>

          <div className="mt-4 flex items-center gap-2 rounded-lg bg-muted/40 px-3 py-2 text-xs text-muted-foreground">
            <Lock className="h-3 w-3 shrink-0" />
            This is a payment simulation. No real charges are made.
          </div>

          <HammerButton
            variant="gold"
            size="lg"
            className="mt-6 w-full"
            onClick={handlePay}
            loading={paying}
            disabled={!holdActive || !isFormValid || paying}
          >
            <ShieldCheck className="mr-2 h-4 w-4" />
            Pay ${Number(hold.totalAmount).toFixed(2)}
          </HammerButton>
        </motion.div>
      </div>
    </div>
  );
}
