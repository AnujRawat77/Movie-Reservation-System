import { createFileRoute } from "@tanstack/react-router";
import { useEffect, useState } from "react";
import { Plus, XCircle, Copy } from "lucide-react";
import { toast } from "sonner";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Badge } from "@/components/ui/badge";
import { Checkbox } from "@/components/ui/checkbox";
import {
  Table, TableBody, TableCell, TableHead, TableHeader, TableRow,
} from "@/components/ui/table";
import {
  Dialog, DialogContent, DialogHeader, DialogTitle, DialogFooter, DialogClose,
} from "@/components/ui/dialog";
import {
  Select, SelectContent, SelectItem, SelectTrigger, SelectValue,
} from "@/components/ui/select";
import {
  admin, movies as moviesApi, ApiError,
  type ShowtimeDto, type MovieDto, type HallDto, type ShowtimeFormPayload, type BulkShowtimePayload,
} from "@/lib/api";

export const Route = createFileRoute("/admin/showtimes")({ component: AdminShowtimes });

function AdminShowtimes() {
  const [list, setList] = useState<ShowtimeDto[]>([]);
  const [moviesList, setMoviesList] = useState<MovieDto[]>([]);
  const [hallsList, setHallsList] = useState<HallDto[]>([]);
  const [loading, setLoading] = useState(true);
  const [open, setOpen] = useState(false);
  const [bulkOpen, setBulkOpen] = useState(false);
  const [form, setForm] = useState<ShowtimeFormPayload>({
    movieId: 0, hallId: 0, startTime: "", price: 12,
  });
  const [bulkForm, setBulkForm] = useState<BulkShowtimePayload>({
    movieId: 0, hallId: 0, startDate: "", endDate: "", times: ["18:00"], price: 12, daysOfWeek: [],
  });
  const [saving, setSaving] = useState(false);
  const [filterMovie, setFilterMovie] = useState<string>("all");

  const refresh = async () => {
    try {
      const [s, m, h] = await Promise.all([
        admin.showtimes.list(),
        moviesApi.list(),
        admin.halls.list(),
      ]);
      setList(s);
      setMoviesList(m);
      setHallsList(h);
    } finally { setLoading(false); }
  };
  useEffect(() => { refresh(); }, []);

  const filtered = filterMovie === "all"
    ? list
    : list.filter((s) => String(s.movieId) === filterMovie);

  const handleCreate = async () => {
    setSaving(true);
    try {
      await admin.showtimes.create(form);
      toast.success("Showtime created");
      setOpen(false);
      refresh();
    } catch (e) { toast.error(e instanceof ApiError ? e.message : "Failed"); }
    finally { setSaving(false); }
  };

  const handleBulkCreate = async () => {
    setSaving(true);
    try {
      const result = await admin.showtimes.bulk(bulkForm);
      toast.success(`${result.length} showtimes created`);
      setBulkOpen(false);
      refresh();
    } catch (e) { toast.error(e instanceof ApiError ? e.message : "Failed"); }
    finally { setSaving(false); }
  };

  const handleCancel = async (id: number) => {
    try {
      await admin.showtimes.cancel(id);
      toast.success("Showtime cancelled");
      refresh();
    } catch (e) { toast.error(e instanceof ApiError ? e.message : "Failed"); }
  };

  const movieName = (id: number) => moviesList.find((m) => m.id === id)?.title ?? `#${id}`;

  return (
    <div>
      <div className="mb-6 flex items-center justify-between">
        <div>
          <h1 className="font-display text-4xl tracking-wider">Showtimes</h1>
          <p className="text-sm text-muted-foreground">Schedule and manage screenings</p>
        </div>
        <div className="flex gap-2">
          <Button variant="outline" onClick={() => { setBulkForm({ movieId: moviesList[0]?.id ?? 0, hallId: hallsList[0]?.id ?? 0, startDate: "", endDate: "", times: ["18:00"], price: 12, daysOfWeek: [] }); setBulkOpen(true); }}>
            <Copy className="mr-2 h-4 w-4" /> Bulk Create
          </Button>
          <Button onClick={() => { setForm({ movieId: moviesList[0]?.id ?? 0, hallId: hallsList[0]?.id ?? 0, startTime: "", price: 12 }); setOpen(true); }}>
            <Plus className="mr-2 h-4 w-4" /> Add Showtime
          </Button>
        </div>
      </div>

      <div className="mb-4">
        <Select value={filterMovie} onValueChange={setFilterMovie}>
          <SelectTrigger className="w-48"><SelectValue placeholder="Filter by movie" /></SelectTrigger>
          <SelectContent>
            <SelectItem value="all">All Movies</SelectItem>
            {moviesList.map((m) => (
              <SelectItem key={m.id} value={String(m.id)}>{m.title}</SelectItem>
            ))}
          </SelectContent>
        </Select>
      </div>

      {loading ? (
        <div className="py-12 text-center text-muted-foreground">Loading…</div>
      ) : (
        <div className="rounded-xl border border-border bg-card/60">
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead>Movie</TableHead>
                <TableHead>Hall</TableHead>
                <TableHead>Date</TableHead>
                <TableHead>Time</TableHead>
                <TableHead>Price</TableHead>
                <TableHead>Status</TableHead>
                <TableHead className="text-right">Actions</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {filtered.slice(0, 50).map((s) => (
                <TableRow key={s.id}>
                  <TableCell className="font-medium">{movieName(s.movieId)}</TableCell>
                  <TableCell>{s.hallName}</TableCell>
                  <TableCell>{s.date}</TableCell>
                  <TableCell>{s.time}</TableCell>
                  <TableCell>${Number(s.price).toFixed(2)}</TableCell>
                  <TableCell><Badge variant={s.status === "SCHEDULED" ? "default" : "secondary"}>{s.status}</Badge></TableCell>
                  <TableCell className="text-right">
                    {s.status === "SCHEDULED" && (
                      <Button size="sm" variant="ghost" className="text-destructive" onClick={() => handleCancel(s.id)}>
                        <XCircle className="mr-1 h-4 w-4" /> Cancel
                      </Button>
                    )}
                  </TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        </div>
      )}

      <Dialog open={open} onOpenChange={setOpen}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle className="font-display text-2xl">Create Showtime</DialogTitle>
          </DialogHeader>
          <div className="grid gap-4 py-4">
            <div>
              <label className="text-xs text-muted-foreground">Movie</label>
              <Select value={String(form.movieId)} onValueChange={(v) => setForm({ ...form, movieId: +v })}>
                <SelectTrigger><SelectValue /></SelectTrigger>
                <SelectContent>
                  {moviesList.map((m) => <SelectItem key={m.id} value={String(m.id)}>{m.title}</SelectItem>)}
                </SelectContent>
              </Select>
            </div>
            <div>
              <label className="text-xs text-muted-foreground">Hall</label>
              <Select value={String(form.hallId)} onValueChange={(v) => setForm({ ...form, hallId: +v })}>
                <SelectTrigger><SelectValue /></SelectTrigger>
                <SelectContent>
                  {hallsList.map((h) => <SelectItem key={h.id} value={String(h.id)}>{h.name}</SelectItem>)}
                </SelectContent>
              </Select>
            </div>
            <div>
              <label className="text-xs text-muted-foreground">Start Time (ISO)</label>
              <Input type="datetime-local" value={form.startTime.slice(0, 16)} onChange={(e) => setForm({ ...form, startTime: e.target.value + ":00" })} />
            </div>
            <div>
              <label className="text-xs text-muted-foreground">Price ($)</label>
              <Input type="number" step="0.01" value={form.price} onChange={(e) => setForm({ ...form, price: +e.target.value })} />
            </div>
          </div>
          <DialogFooter>
            <DialogClose asChild><Button variant="ghost">Cancel</Button></DialogClose>
            <Button onClick={handleCreate} disabled={saving || !form.startTime}>{saving ? "Creating…" : "Create"}</Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>

      <Dialog open={bulkOpen} onOpenChange={setBulkOpen}>
        <DialogContent className="max-w-lg">
          <DialogHeader>
            <DialogTitle className="font-display text-2xl">Bulk Create Showtimes</DialogTitle>
          </DialogHeader>
          <div className="grid gap-4 py-4">
            <div>
              <label className="text-xs text-muted-foreground">Movie</label>
              <Select value={String(bulkForm.movieId)} onValueChange={(v) => setBulkForm({ ...bulkForm, movieId: +v })}>
                <SelectTrigger><SelectValue /></SelectTrigger>
                <SelectContent>
                  {moviesList.map((m) => <SelectItem key={m.id} value={String(m.id)}>{m.title}</SelectItem>)}
                </SelectContent>
              </Select>
            </div>
            <div>
              <label className="text-xs text-muted-foreground">Hall</label>
              <Select value={String(bulkForm.hallId)} onValueChange={(v) => setBulkForm({ ...bulkForm, hallId: +v })}>
                <SelectTrigger><SelectValue /></SelectTrigger>
                <SelectContent>
                  {hallsList.map((h) => <SelectItem key={h.id} value={String(h.id)}>{h.name}</SelectItem>)}
                </SelectContent>
              </Select>
            </div>
            <div className="grid grid-cols-2 gap-2">
              <div>
                <label className="text-xs text-muted-foreground">Start Date</label>
                <Input type="date" value={bulkForm.startDate} onChange={(e) => setBulkForm({ ...bulkForm, startDate: e.target.value })} />
              </div>
              <div>
                <label className="text-xs text-muted-foreground">End Date</label>
                <Input type="date" value={bulkForm.endDate} onChange={(e) => setBulkForm({ ...bulkForm, endDate: e.target.value })} />
              </div>
            </div>
            <div>
              <label className="text-xs text-muted-foreground">Show Times (comma-separated, e.g. 14:00,18:00,21:00)</label>
              <Input
                value={bulkForm.times.join(",")}
                onChange={(e) => setBulkForm({ ...bulkForm, times: e.target.value.split(",").map((t) => t.trim()).filter(Boolean) })}
                placeholder="18:00,21:00"
              />
            </div>
            <div>
              <label className="text-xs text-muted-foreground">Days of Week</label>
              <div className="flex flex-wrap gap-3 pt-1">
                {["MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY", "SUNDAY"].map((day) => (
                  <label key={day} className="flex items-center gap-1.5 text-xs">
                    <Checkbox
                      checked={bulkForm.daysOfWeek.includes(day)}
                      onCheckedChange={(checked) => {
                        setBulkForm({
                          ...bulkForm,
                          daysOfWeek: checked
                            ? [...bulkForm.daysOfWeek, day]
                            : bulkForm.daysOfWeek.filter((d) => d !== day),
                        });
                      }}
                    />
                    {day.slice(0, 3)}
                  </label>
                ))}
              </div>
            </div>
            <div>
              <label className="text-xs text-muted-foreground">Price ($)</label>
              <Input type="number" step="0.01" value={bulkForm.price} onChange={(e) => setBulkForm({ ...bulkForm, price: +e.target.value })} />
            </div>
          </div>
          <DialogFooter>
            <DialogClose asChild><Button variant="ghost">Cancel</Button></DialogClose>
            <Button
              onClick={handleBulkCreate}
              disabled={saving || !bulkForm.startDate || !bulkForm.endDate || bulkForm.daysOfWeek.length === 0 || bulkForm.times.length === 0}
            >
              {saving ? "Creating…" : "Create Bulk"}
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </div>
  );
}
