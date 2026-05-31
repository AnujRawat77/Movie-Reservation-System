import { createFileRoute } from "@tanstack/react-router";
import { useEffect, useState } from "react";
import { motion } from "framer-motion";
import { Film, Clock, Users, DollarSign } from "lucide-react";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table";
import { Badge } from "@/components/ui/badge";
import {
  admin,
  type MovieDto,
  type ReservationDto,
  type UserDto,
  type ShowtimeDto,
  type RevenueReport,
} from "@/lib/api";

export const Route = createFileRoute("/admin/")({
  component: AdminOverview,
});

function AdminOverview() {
  const [movies, setMovies] = useState<MovieDto[]>([]);
  const [users, setUsers] = useState<UserDto[]>([]);
  const [reservations, setReservations] = useState<ReservationDto[]>([]);
  const [showtimes, setShowtimes] = useState<ShowtimeDto[]>([]);
  const [revenue, setRevenue] = useState<RevenueReport | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    (async () => {
      try {
        const [m, u, r, s, rev] = await Promise.all([
          admin.movies.list(),
          admin.users.list(),
          admin.reservations.list(),
          admin.showtimes.list({ status: "SCHEDULED" }),
          admin.reports.revenue(),
        ]);
        setMovies(m);
        setUsers(u);
        setReservations(r);
        setShowtimes(s);
        setRevenue(rev);
      } catch {
        // ignore
      } finally {
        setLoading(false);
      }
    })();
  }, []);

  const stats = [
    {
      label: "Total Movies",
      value: movies.length,
      icon: Film,
      color: "text-blue-400",
    },
    {
      label: "Scheduled Showtimes",
      value: showtimes.length,
      icon: Clock,
      color: "text-green-400",
    },
    {
      label: "Registered Users",
      value: users.length,
      icon: Users,
      color: "text-purple-400",
    },
    {
      label: "Total Revenue",
      value: `$${(revenue?.totalRevenue ?? 0).toFixed(2)}`,
      icon: DollarSign,
      color: "text-accent",
    },
  ];

  const recentReservations = reservations.slice(0, 8);

  return (
    <div>
      <div className="mb-8">
        <h1 className="font-display text-4xl tracking-wider">Dashboard</h1>
        <p className="mt-1 text-sm text-muted-foreground">
          Overview of your cinema operations
        </p>
      </div>

      {/* Stats */}
      <div className="mb-8 grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-4">
        {stats.map((stat, i) => {
          const Icon = stat.icon;
          return (
            <motion.div
              key={stat.label}
              initial={{ opacity: 0, y: 16 }}
              animate={{ opacity: 1, y: 0 }}
              transition={{ delay: i * 0.05 }}
            >
              <Card className="border-border bg-card/60 backdrop-blur">
                <CardHeader className="flex flex-row items-center justify-between pb-2">
                  <CardTitle className="text-xs font-medium uppercase tracking-widest text-muted-foreground">
                    {stat.label}
                  </CardTitle>
                  <Icon className={`h-5 w-5 ${stat.color}`} />
                </CardHeader>
                <CardContent>
                  <div className="font-display text-3xl">
                    {loading ? "…" : stat.value}
                  </div>
                </CardContent>
              </Card>
            </motion.div>
          );
        })}
      </div>

      {/* Recent reservations */}
      <Card className="border-border bg-card/60">
        <CardHeader>
          <CardTitle className="font-display text-xl tracking-wider">
            Recent Reservations
          </CardTitle>
        </CardHeader>
        <CardContent>
          {loading ? (
            <div className="py-8 text-center text-muted-foreground">Loading…</div>
          ) : recentReservations.length === 0 ? (
            <div className="py-8 text-center text-muted-foreground">
              No reservations yet
            </div>
          ) : (
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>User</TableHead>
                  <TableHead>Movie</TableHead>
                  <TableHead>Date</TableHead>
                  <TableHead>Seats</TableHead>
                  <TableHead>Amount</TableHead>
                  <TableHead>Status</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {recentReservations.map((r) => (
                  <TableRow key={r.id}>
                    <TableCell className="font-medium">{r.userName}</TableCell>
                    <TableCell>{r.movieTitle}</TableCell>
                    <TableCell className="text-muted-foreground">
                      {r.showDate} {r.showTime}
                    </TableCell>
                    <TableCell>
                      {r.seats.map((s) => `${s.rowLabel}${s.seatNumber}`).join(", ")}
                    </TableCell>
                    <TableCell className="text-accent">
                      ${Number(r.totalAmount).toFixed(2)}
                    </TableCell>
                    <TableCell>
                      <Badge
                        variant={r.status === "CONFIRMED" ? "default" : "secondary"}
                      >
                        {r.status}
                      </Badge>
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          )}
        </CardContent>
      </Card>
    </div>
  );
}
