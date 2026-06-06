import { createFileRoute } from "@tanstack/react-router";
import { useEffect, useState } from "react";
import { toast } from "sonner";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import {
  Table, TableBody, TableCell, TableHead, TableHeader, TableRow,
} from "@/components/ui/table";
import {
  Select, SelectContent, SelectItem, SelectTrigger, SelectValue,
} from "@/components/ui/select";
import {
  Dialog, DialogContent, DialogHeader, DialogTitle, DialogClose,
} from "@/components/ui/dialog";
import { admin, ApiError, type UserDto, type ReservationDto } from "@/lib/api";

export const Route = createFileRoute("/admin/users")({ component: AdminUsers });

function AdminUsers() {
  const [users, setUsers] = useState<UserDto[]>([]);
  const [loading, setLoading] = useState(true);
  const [bookingsOpen, setBookingsOpen] = useState(false);
  const [bookingsUser, setBookingsUser] = useState<UserDto | null>(null);
  const [bookings, setBookings] = useState<ReservationDto[]>([]);
  const [bookingsLoading, setBookingsLoading] = useState(false);

  const refresh = async () => {
    try { setUsers(await admin.users.list()); }
    finally { setLoading(false); }
  };
  useEffect(() => { refresh(); }, []);

  const handleRoleChange = async (user: UserDto, newRole: "USER" | "ADMIN") => {
    if (newRole === user.role) return;
    try {
      await admin.users.updateRole(user.id, newRole);
      toast.success(`${user.name} is now ${newRole}`);
      refresh();
    } catch (e) { toast.error(e instanceof ApiError ? e.message : "Failed"); }
  };

  const handleToggleActive = async (user: UserDto) => {
    try {
      await admin.users.toggleActive(user.id);
      toast.success(`${user.name} is now ${user.active ? "suspended" : "active"}`);
      refresh();
    } catch (e) { toast.error(e instanceof ApiError ? e.message : "Failed"); }
  };

  const handleViewBookings = async (user: UserDto) => {
    setBookingsUser(user);
    setBookingsOpen(true);
    setBookingsLoading(true);
    try {
      setBookings(await admin.users.bookings(user.id));
    } catch (e) {
      toast.error(e instanceof ApiError ? e.message : "Failed to load bookings");
    } finally {
      setBookingsLoading(false);
    }
  };

  return (
    <div>
      <div className="mb-6">
        <h1 className="font-display text-4xl tracking-wider">Users</h1>
        <p className="text-sm text-muted-foreground">Manage user roles, status, and view booking history</p>
      </div>

      {loading ? (
        <div className="py-12 text-center text-muted-foreground">Loading…</div>
      ) : (
        <div className="rounded-xl border border-border bg-card/60">
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead>Name</TableHead>
                <TableHead>Email</TableHead>
                <TableHead>Joined</TableHead>
                <TableHead>Status</TableHead>
                <TableHead>Role</TableHead>
                <TableHead className="text-right">Actions</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {users.map((u) => (
                <TableRow key={u.id}>
                  <TableCell className="font-medium">{u.name}</TableCell>
                  <TableCell className="text-muted-foreground">{u.email}</TableCell>
                  <TableCell className="text-muted-foreground">
                    {u.createdAt ? new Date(u.createdAt).toLocaleDateString() : "—"}
                  </TableCell>
                  <TableCell>
                    <Badge variant={u.active !== false ? "default" : "destructive"}>
                      {u.active !== false ? "Active" : "Suspended"}
                    </Badge>
                  </TableCell>
                  <TableCell>
                    <Select value={u.role} onValueChange={(v) => handleRoleChange(u, v as "USER" | "ADMIN")}>
                      <SelectTrigger className="w-28">
                        <SelectValue />
                      </SelectTrigger>
                      <SelectContent>
                        <SelectItem value="USER">User</SelectItem>
                        <SelectItem value="ADMIN">Admin</SelectItem>
                      </SelectContent>
                    </Select>
                  </TableCell>
                  <TableCell className="text-right space-x-2">
                    <Button size="sm" variant="outline" onClick={() => handleViewBookings(u)}>
                      Bookings
                    </Button>
                    <Button
                      size="sm"
                      variant={u.active !== false ? "destructive" : "default"}
                      onClick={() => handleToggleActive(u)}
                    >
                      {u.active !== false ? "Suspend" : "Activate"}
                    </Button>
                  </TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        </div>
      )}

      <Dialog open={bookingsOpen} onOpenChange={setBookingsOpen}>
        <DialogContent className="max-w-2xl">
          <DialogHeader>
            <DialogTitle className="font-display text-2xl">
              Bookings — {bookingsUser?.name}
            </DialogTitle>
          </DialogHeader>
          {bookingsLoading ? (
            <div className="py-8 text-center text-muted-foreground">Loading…</div>
          ) : bookings.length === 0 ? (
            <div className="py-8 text-center text-muted-foreground">No bookings found</div>
          ) : (
            <div className="max-h-96 overflow-y-auto">
              <Table>
                <TableHeader>
                  <TableRow>
                    <TableHead>Movie</TableHead>
                    <TableHead>Date</TableHead>
                    <TableHead>Seats</TableHead>
                    <TableHead>Amount</TableHead>
                    <TableHead>Status</TableHead>
                  </TableRow>
                </TableHeader>
                <TableBody>
                  {bookings.map((b) => (
                    <TableRow key={b.id}>
                      <TableCell className="font-medium">{b.movieTitle}</TableCell>
                      <TableCell>{b.showDate}</TableCell>
                      <TableCell>{b.seats.map((s) => `${s.rowLabel}${s.seatNumber}`).join(", ")}</TableCell>
                      <TableCell>${Number(b.totalAmount).toFixed(2)}</TableCell>
                      <TableCell>
                        <Badge variant={b.status === "CONFIRMED" ? "default" : "secondary"}>
                          {b.status}
                        </Badge>
                      </TableCell>
                    </TableRow>
                  ))}
                </TableBody>
              </Table>
            </div>
          )}
        </DialogContent>
      </Dialog>
    </div>
  );
}
