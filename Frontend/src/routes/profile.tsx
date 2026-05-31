import { createFileRoute, Link, useNavigate } from "@tanstack/react-router";
import { motion } from "framer-motion";
import { LogOut, Mail, Ticket, User as UserIcon, Settings } from "lucide-react";
import { useEffect } from "react";
import { HammerButton } from "@/components/HammerButton";
import { useAuth } from "@/hooks/use-auth";
import { ROUTES } from "@/constants/routes";
import { toast } from "sonner";

export const Route = createFileRoute("/profile")({
  head: () => ({
    meta: [
      { title: "My Profile — CineReserve" },
      { name: "description", content: "Manage your CineReserve profile and preferences." },
    ],
  }),
  component: ProfilePage,
});

function initials(name: string) {
  return name
    .split(" ")
    .map((s) => s[0])
    .filter(Boolean)
    .slice(0, 2)
    .join("")
    .toUpperCase();
}

function ProfilePage() {
  const { user, hydrated, logout } = useAuth();
  const navigate = useNavigate();

  useEffect(() => {
    if (hydrated && !user) navigate({ to: ROUTES.login });
  }, [hydrated, user, navigate]);

  if (!user) return null;

  const handleLogout = () => {
    logout();
    toast.success("Signed out");
    navigate({ to: ROUTES.home });
  };

  return (
    <div className="relative min-h-[calc(100dvh-4rem)] overflow-hidden px-4 py-16">
      <div className="pointer-events-none absolute inset-0">
        <div className="absolute -top-40 left-1/2 h-[500px] w-[500px] -translate-x-1/2 rounded-full bg-primary/10 blur-[120px]" />
      </div>

      <motion.div
        initial={{ opacity: 0, y: 24 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ duration: 0.5, ease: "easeOut" }}
        className="relative z-10 mx-auto max-w-2xl"
      >
        <div className="rounded-2xl border border-border bg-card/80 p-8 shadow-elegant backdrop-blur-xl">
          <div className="flex items-center gap-5">
            <div className="grid h-20 w-20 place-items-center rounded-full bg-gradient-primary text-2xl font-bold text-primary-foreground shadow-glow">
              {initials(user.name)}
            </div>
            <div className="min-w-0 flex-1">
              <h1 className="font-display text-3xl tracking-wider text-foreground">
                {user.name}
              </h1>
              <p className="mt-1 flex items-center gap-2 text-sm text-muted-foreground">
                <Mail className="h-3.5 w-3.5" />
                <span className="truncate">{user.email}</span>
              </p>
            </div>
          </div>

          <div className="mt-8 grid gap-3 sm:grid-cols-2">
            <Link
              to={ROUTES.myBookings}
              className="group flex items-center gap-3 rounded-xl border border-border bg-secondary/40 p-4 transition-colors hover:bg-secondary"
            >
              <div className="grid h-10 w-10 place-items-center rounded-lg bg-primary/15 text-primary">
                <Ticket className="h-5 w-5" />
              </div>
              <div>
                <div className="text-sm font-medium text-foreground">My Bookings</div>
                <div className="text-xs text-muted-foreground">View past & upcoming</div>
              </div>
            </Link>

            <div className="flex items-center gap-3 rounded-xl border border-border bg-secondary/40 p-4">
              <div className="grid h-10 w-10 place-items-center rounded-lg bg-gold/15 text-gold">
                <Settings className="h-5 w-5" />
              </div>
              <div>
                <div className="text-sm font-medium text-foreground">Preferences</div>
                <div className="text-xs text-muted-foreground">Coming soon</div>
              </div>
            </div>
          </div>

          <div className="mt-8 flex justify-end">
            <HammerButton variant="ghost" onClick={handleLogout}>
              <LogOut className="h-4 w-4" />
              Sign Out
            </HammerButton>
          </div>
        </div>
      </motion.div>
    </div>
  );
}
