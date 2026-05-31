import { createFileRoute } from "@tanstack/react-router";
import { useEffect, useState } from "react";
import { Badge } from "@/components/ui/badge";
import {
  Table, TableBody, TableCell, TableHead, TableHeader, TableRow,
} from "@/components/ui/table";
import { admin, type ReservationDto } from "@/lib/api";

export const Route = createFileRoute("/admin/reservations")({ component: AdminReservations });

function AdminReservations() {
  const [list, setList] = useState<ReservationDto[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    (async () => {
      try { setList(await admin.reservations.list()); }
      finally { setLoading(false); }
    })();
  }, []);

  return (
    <div>
      <div className="mb-6">
        <h1 className="font-display text-4xl tracking-wider">Reservations</h1>
        <p className="text-sm text-muted-foreground">All bookings across all users</p>
      </div>

      {loading ? (
        <div className="py-12 text-center text-muted-foreground">Loading…</div>
      ) : list.length === 0 ? (
        <div className="py-12 text-center text-muted-foreground">No reservations yet</div>
      ) : (
        <div className="rounded-xl border border-border bg-card/60">
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead>ID</TableHead>
                <TableHead>User</TableHead>
                <TableHead>Movie</TableHead>
                <TableHead>Hall</TableHead>
                <TableHead>Date & Time</TableHead>
                <TableHead>Seats</TableHead>
                <TableHead>Total</TableHead>
                <TableHead>Status</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {list.map((r) => (
                <TableRow key={r.id}>
                  <TableCell className="font-mono text-xs text-muted-foreground">
                    {r.id.slice(0, 8)}…
                  </TableCell>
                  <TableCell className="font-medium">{r.userName}</TableCell>
                  <TableCell>{r.movieTitle}</TableCell>
                  <TableCell className="text-muted-foreground">{r.hallName}</TableCell>
                  <TableCell className="text-muted-foreground">
                    {r.showDate} {r.showTime}
                  </TableCell>
                  <TableCell>
                    {r.seats.map((s) => `${s.rowLabel}${s.seatNumber}`).join(", ")}
                  </TableCell>
                  <TableCell className="text-accent">${Number(r.totalAmount).toFixed(2)}</TableCell>
                  <TableCell>
                    <Badge variant={r.status === "CONFIRMED" ? "default" : "secondary"}>
                      {r.status}
                    </Badge>
                  </TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        </div>
      )}
    </div>
  );
}
