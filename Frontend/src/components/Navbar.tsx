import { Link, useRouterState } from "@tanstack/react-router";
import { motion } from "framer-motion";
import { Film, Menu, X, LogOut, User as UserIcon, Ticket, Shield } from "lucide-react";
import { useState } from "react";
import { STRINGS } from "@/constants/strings";
import { ROUTES } from "@/constants/routes";
import { HammerButton } from "./HammerButton";
import { ThemeToggle } from "./ThemeToggle";
import { useAuth } from "@/hooks/use-auth";
import {
  DropdownMenu,
  DropdownMenuTrigger,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuLabel,
  DropdownMenuSeparator,
} from "@/components/ui/dropdown-menu";
import { cn } from "@/lib/utils";
import { toast } from "sonner";
import { useNavigate } from "@tanstack/react-router";

const links = [
  { to: ROUTES.home, label: STRINGS.nav.home },
  { to: ROUTES.movies, label: STRINGS.nav.movies },
  { to: ROUTES.myBookings, label: STRINGS.nav.myBookings },
];

function initials(name: string) {
  return name
    .split(" ")
    .map((s) => s[0])
    .filter(Boolean)
    .slice(0, 2)
    .join("")
    .toUpperCase();
}

function ProfileMenu() {
  const { user, isAdmin, logout } = useAuth();
  const navigate = useNavigate();
  if (!user) return null;

  const handleLogout = () => {
    logout();
    toast.success("Signed out");
    navigate({ to: ROUTES.home });
  };

  return (
    <DropdownMenu>
      <DropdownMenuTrigger asChild>
        <motion.button
          whileHover={{ scale: 1.06 }}
          whileTap={{ scale: 0.94 }}
          transition={{ type: "spring", stiffness: 400, damping: 17 }}
          aria-label="Open profile menu"
          className="grid h-10 w-10 place-items-center rounded-full bg-gradient-primary text-sm font-bold text-primary-foreground shadow-glow ring-1 ring-border focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring"
        >
          {initials(user.name)}
        </motion.button>
      </DropdownMenuTrigger>
      <DropdownMenuContent align="end" className="w-56 glass">
        <DropdownMenuLabel className="flex flex-col gap-0.5">
          <span className="text-sm font-medium">{user.name}</span>
          <span className="truncate text-xs font-normal text-muted-foreground">
            {user.email}
          </span>
        </DropdownMenuLabel>
        <DropdownMenuSeparator />
        <DropdownMenuItem asChild>
          <Link to={ROUTES.profile} className="cursor-pointer">
            <UserIcon className="h-4 w-4" /> {STRINGS.nav.profile}
          </Link>
        </DropdownMenuItem>
        <DropdownMenuItem asChild>
          <Link to={ROUTES.myBookings} className="cursor-pointer">
            <Ticket className="h-4 w-4" /> {STRINGS.nav.myBookings}
          </Link>
        </DropdownMenuItem>
        {isAdmin && (
          <>
            <DropdownMenuSeparator />
            <DropdownMenuItem asChild>
              <Link to="/admin" className="cursor-pointer text-accent focus:text-accent">
                <Shield className="h-4 w-4" /> Admin Dashboard
              </Link>
            </DropdownMenuItem>
          </>
        )}
        <DropdownMenuSeparator />
        <DropdownMenuItem onClick={handleLogout} className="cursor-pointer text-destructive focus:text-destructive">
          <LogOut className="h-4 w-4" /> {STRINGS.nav.logout}
        </DropdownMenuItem>
      </DropdownMenuContent>
    </DropdownMenu>
  );
}

export function Navbar() {
  const path = useRouterState({ select: (s) => s.location.pathname });
  const [open, setOpen] = useState(false);
  const { isAuthenticated, user, hydrated, logout } = useAuth();
  const navigate = useNavigate();

  return (
    <header className="sticky top-0 z-50 w-full">
      <div className="glass border-b border-border">
        <div className="mx-auto flex h-16 max-w-7xl items-center justify-between px-4 sm:px-6 lg:px-8">
          <Link to={ROUTES.home} className="group flex items-center gap-2">
            <motion.span
              whileHover={{ rotate: -10, scale: 1.1 }}
              className="grid h-9 w-9 place-items-center rounded-full bg-gradient-primary shadow-glow"
            >
              <Film className="h-4 w-4 text-primary-foreground" />
            </motion.span>
            <span className="font-display text-xl tracking-widest">
              {STRINGS.app.name}
            </span>
          </Link>

          <nav className="hidden items-center gap-1 md:flex">
            {links.map((l) => {
              const active =
                l.to === "/" ? path === "/" : path.startsWith(l.to);
              return (
                <Link
                  key={l.to}
                  to={l.to}
                  className={cn(
                    "relative rounded-full px-4 py-2 text-sm transition-colors",
                    active
                      ? "text-foreground"
                      : "text-muted-foreground hover:text-foreground",
                  )}
                >
                  {active && (
                    <motion.span
                      layoutId="nav-pill"
                      transition={{ type: "spring", stiffness: 380, damping: 30 }}
                      className="absolute inset-0 -z-10 rounded-full bg-secondary"
                    />
                  )}
                  {l.label}
                </Link>
              );
            })}
          </nav>

          <div className="flex items-center gap-2">
            <ThemeToggle />
            {hydrated && isAuthenticated ? (
              <div className="hidden md:block">
                <ProfileMenu />
              </div>
            ) : (
              <div className="hidden md:block">
                <Link to={ROUTES.signup}>
                  <HammerButton size="sm" variant="gold">
                    {STRINGS.nav.signup}
                  </HammerButton>
                </Link>
              </div>
            )}

            {/* Mobile profile icon when signed in */}
            {hydrated && isAuthenticated && (
              <div className="md:hidden">
                <ProfileMenu />
              </div>
            )}

            <button
              aria-label="Open menu"
              onClick={() => setOpen((v) => !v)}
              className="grid h-10 w-10 place-items-center rounded-full border border-border bg-secondary/40 md:hidden"
            >
              {open ? <X className="h-4 w-4" /> : <Menu className="h-4 w-4" />}
            </button>
          </div>
        </div>
      </div>

      {open && (
        <motion.div
          initial={{ opacity: 0, y: -8 }}
          animate={{ opacity: 1, y: 0 }}
          className="glass border-b border-border md:hidden"
        >
          <div className="mx-auto flex max-w-7xl flex-col gap-1 px-4 py-3">
            {links.map((l) => (
              <Link
                key={l.to}
                to={l.to}
                onClick={() => setOpen(false)}
                className="rounded-lg px-3 py-2 text-sm text-muted-foreground hover:bg-secondary hover:text-foreground"
              >
                {l.label}
              </Link>
            ))}
            {hydrated && isAuthenticated && user ? (
              <>
                <Link
                  to={ROUTES.profile}
                  onClick={() => setOpen(false)}
                  className="rounded-lg px-3 py-2 text-sm text-muted-foreground hover:bg-secondary hover:text-foreground"
                >
                  {STRINGS.nav.profile}
                </Link>
                <button
                  onClick={() => {
                    setOpen(false);
                    logout();
                    toast.success("Signed out");
                    navigate({ to: ROUTES.home });
                  }}
                  className="rounded-lg px-3 py-2 text-left text-sm text-destructive hover:bg-secondary"
                >
                  {STRINGS.nav.logout}
                </button>
              </>
            ) : (
              <Link to={ROUTES.signup} onClick={() => setOpen(false)}>
                <HammerButton size="sm" variant="gold" className="mt-2 w-full">
                  {STRINGS.nav.signup}
                </HammerButton>
              </Link>
            )}
          </div>
        </motion.div>
      )}
    </header>
  );
}
