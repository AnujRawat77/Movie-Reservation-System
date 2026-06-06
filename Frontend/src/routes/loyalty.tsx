import { createFileRoute, useNavigate } from "@tanstack/react-router";
import { motion } from "framer-motion";
import { useEffect, useState } from "react";
import { Award, ArrowDownCircle, ArrowUpCircle, Coins } from "lucide-react";
import { toast } from "sonner";
import { ROUTES } from "@/constants/routes";
import { useAuth } from "@/hooks/use-auth";
import { loyalty, type LoyaltyBalanceDto, ApiError } from "@/lib/api";

export const Route = createFileRoute("/loyalty")({
  head: () => ({
    meta: [{ title: "Loyalty Points — CineReserve" }],
  }),
  component: LoyaltyPage,
});

function LoyaltyPage() {
  const { user, hydrated } = useAuth();
  const navigate = useNavigate();
  const [data, setData] = useState<LoyaltyBalanceDto | null>(null);
  const [loading, setLoading] = useState(true);
  const [redeeming, setRedeeming] = useState(false);
  const [redeemInput, setRedeemInput] = useState("");

  useEffect(() => {
    if (hydrated && !user) navigate({ to: ROUTES.login });
  }, [hydrated, user, navigate]);

  useEffect(() => {
    if (!user) return;
    loyalty
      .balance()
      .then(setData)
      .catch(() => toast.error("Failed to load loyalty points"))
      .finally(() => setLoading(false));
  }, [user]);

  const handleRedeem = async () => {
    const pts = parseInt(redeemInput, 10);
    if (!pts || pts < 1) {
      toast.error("Enter a valid number of points");
      return;
    }
    setRedeeming(true);
    try {
      const updated = await loyalty.redeem(pts);
      setData(updated);
      setRedeemInput("");
      toast.success(`Redeemed ${pts} points`);
    } catch (e) {
      toast.error(e instanceof ApiError ? e.message : "Redemption failed");
    } finally {
      setRedeeming(false);
    }
  };

  if (!user || loading) {
    return (
      <div className="mx-auto max-w-3xl px-4 py-24 text-center font-display text-3xl text-muted-foreground">
        Loading…
      </div>
    );
  }

  return (
    <div className="mx-auto max-w-3xl px-4 pb-24 pt-12 sm:px-6">
      <motion.div
        initial={{ opacity: 0, y: 16 }}
        animate={{ opacity: 1, y: 0 }}
        className="mb-10"
      >
        <div className="mb-2 text-xs uppercase tracking-widest text-accent">
          Rewards
        </div>
        <h1 className="font-display text-5xl md:text-7xl">Loyalty Points</h1>
      </motion.div>

      {/* Balance card */}
      <motion.div
        initial={{ opacity: 0, y: 8 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ delay: 0.1 }}
        className="mb-8 rounded-2xl border border-accent/30 bg-accent/10 p-8 text-center"
      >
        <Award className="mx-auto mb-3 h-10 w-10 text-accent" />
        <div className="font-display text-6xl text-accent">
          {data?.balance ?? 0}
        </div>
        <div className="mt-1 text-sm text-muted-foreground">points available</div>
        <div className="mt-1 text-xs text-muted-foreground">
          Earn 10 pts per $1 spent · Redeem anytime
        </div>
      </motion.div>

      {/* Redeem section */}
      <motion.div
        initial={{ opacity: 0, y: 8 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ delay: 0.15 }}
        className="mb-8 rounded-xl border border-border bg-card/60 p-6"
      >
        <h2 className="mb-4 font-display text-xl">Redeem Points</h2>
        <div className="flex gap-3">
          <div className="relative flex-1">
            <Coins className="absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground" />
            <input
              type="number"
              min={1}
              max={data?.balance ?? 0}
              placeholder="Points to redeem"
              value={redeemInput}
              onChange={(e) => setRedeemInput(e.target.value)}
              className="w-full rounded-md border border-border bg-background py-2 pl-10 pr-3 text-sm focus:outline-none focus:ring-1 focus:ring-accent"
            />
          </div>
          <button
            onClick={handleRedeem}
            disabled={redeeming || !redeemInput}
            className="rounded-md bg-accent px-5 py-2 text-sm font-medium text-background hover:bg-accent/90 disabled:opacity-50"
          >
            {redeeming ? "Redeeming…" : "Redeem"}
          </button>
        </div>
      </motion.div>

      {/* Transaction history */}
      <motion.div
        initial={{ opacity: 0, y: 8 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ delay: 0.2 }}
      >
        <h2 className="mb-4 font-display text-xl">History</h2>
        {!data?.transactions.length ? (
          <p className="text-sm text-muted-foreground">No transactions yet.</p>
        ) : (
          <div className="space-y-2">
            {data.transactions.map((tx) => (
              <div
                key={tx.id}
                className="flex items-center justify-between rounded-lg border border-border bg-card/50 px-4 py-3"
              >
                <div className="flex items-center gap-3">
                  {tx.type === "EARNED" ? (
                    <ArrowUpCircle className="h-5 w-5 text-green-500" />
                  ) : (
                    <ArrowDownCircle className="h-5 w-5 text-red-400" />
                  )}
                  <div>
                    <div className="text-sm font-medium">{tx.description}</div>
                    <div className="text-xs text-muted-foreground">
                      {new Date(tx.createdAt).toLocaleDateString()}
                    </div>
                  </div>
                </div>
                <span
                  className={`font-display text-lg ${
                    tx.type === "EARNED" ? "text-green-500" : "text-red-400"
                  }`}
                >
                  {tx.type === "EARNED" ? "+" : "-"}
                  {tx.points}
                </span>
              </div>
            ))}
          </div>
        )}
      </motion.div>
    </div>
  );
}
