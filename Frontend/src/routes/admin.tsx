import { createFileRoute, Link, Outlet, useNavigate, useRouterState } from "@tanstack/react-router";
import { useEffect } from "react";
import {
  LayoutDashboard,
  Film,
  Tags,
  Building2,
  Clock,
  Users,
  Ticket,
  BarChart3,
  ArrowLeft,
} from "lucide-react";
import { useAuth } from "@/hooks/use-auth";
import { cn } from "@/lib/utils";

export const Route = createFileRoute("/admin")({
  component: AdminLayout,
});

const navItems = [
  { to: "/admin", label: "Overview", icon: LayoutDashboard, exact: true },
  { to: "/admin/movies", label: "Movies", icon: Film },
  { to: "/admin/genres", label: "Genres", icon: Tags },
  { to: "/admin/halls", label: "Halls", icon: Building2 },
  { to: "/admin/showtimes", label: "Showtimes", icon: Clock },
  { to: "/admin/users", label: "Users", icon: Users },
  { to: "/admin/reservations", label: "Reservations", icon: Ticket },
  { to: "/admin/reports", label: "Reports", icon: BarChart3 },
];

function AdminLayout() {
  const { user, isAdmin, hydrated } = useAuth();
  const navigate = useNavigate();
  const path = useRouterState({ select: (s) => s.location.pathname });

  useEffect(() => {
    if (hydrated && (!user || !isAdmin)) {
      navigate({ to: "/" });
    }
  }, [hydrated, user, isAdmin, navigate]);

  if (!hydrated || !isAdmin) return null;

  return (
    <div className="flex min-h-[calc(100dvh-4rem)]">
      {/* Sidebar */}
      <aside className="hidden w-64 shrink-0 border-r border-border bg-card/50 lg:block">
        <div className="sticky top-16 flex flex-col gap-1 p-4">
          <Link
            to="/"
            className="mb-4 flex items-center gap-2 text-xs text-muted-foreground hover:text-foreground"
          >
            <ArrowLeft className="h-3 w-3" /> Back to site
          </Link>
          <div className="mb-4 text-xs font-semibold uppercase tracking-widest text-accent">
            Admin Panel
          </div>
          {navItems.map((item) => {
            const active = item.exact
              ? path === item.to
              : path.startsWith(item.to) && item.to !== "/admin";
            const Icon = item.icon;
            return (
              <Link
                key={item.to}
                to={item.to}
                className={cn(
                  "flex items-center gap-3 rounded-lg px-3 py-2 text-sm transition-colors",
                  active
                    ? "bg-accent/15 text-accent font-medium"
                    : "text-muted-foreground hover:bg-secondary hover:text-foreground",
                )}
              >
                <Icon className="h-4 w-4" />
                {item.label}
              </Link>
            );
          })}
        </div>
      </aside>

      {/* Mobile nav - horizontal scroll */}
      <div className="fixed bottom-0 left-0 right-0 z-40 border-t border-border bg-card/90 backdrop-blur lg:hidden">
        <div className="no-scrollbar flex gap-1 overflow-x-auto p-2">
          {navItems.map((item) => {
            const active = item.exact
              ? path === item.to
              : path.startsWith(item.to) && item.to !== "/admin";
            const Icon = item.icon;
            return (
              <Link
                key={item.to}
                to={item.to}
                className={cn(
                  "flex shrink-0 flex-col items-center gap-1 rounded-lg px-3 py-2 text-[10px]",
                  active ? "text-accent" : "text-muted-foreground",
                )}
              >
                <Icon className="h-4 w-4" />
                {item.label}
              </Link>
            );
          })}
        </div>
      </div>

      {/* Main content */}
      <main className="flex-1 overflow-x-hidden px-4 pb-24 pt-8 sm:px-6 lg:px-8 lg:pb-8">
        <Outlet />
      </main>
    </div>
  );
}
