import { createFileRoute } from "@tanstack/react-router";
import { useEffect, useState } from "react";
import { motion } from "framer-motion";
import { Plus, Pencil, Trash2 } from "lucide-react";
import { toast } from "sonner";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Badge } from "@/components/ui/badge";
import {
  Table, TableBody, TableCell, TableHead, TableHeader, TableRow,
} from "@/components/ui/table";
import {
  Dialog, DialogContent, DialogHeader, DialogTitle, DialogFooter, DialogClose,
} from "@/components/ui/dialog";
import {
  AlertDialog, AlertDialogAction, AlertDialogCancel, AlertDialogContent,
  AlertDialogDescription, AlertDialogFooter, AlertDialogHeader, AlertDialogTitle, AlertDialogTrigger,
} from "@/components/ui/alert-dialog";
import {
  Select, SelectContent, SelectItem, SelectTrigger, SelectValue,
} from "@/components/ui/select";
import {
  admin, genres as genresApi, ApiError,
  type MovieDto, type Genre, type MovieFormPayload,
} from "@/lib/api";

export const Route = createFileRoute("/admin/movies")({ component: AdminMovies });

const emptyForm: MovieFormPayload = {
  title: "", tagline: "", description: "", posterUrl: "", durationMinutes: 120,
  rating: 7.0, year: 2026, language: "English", synopsis: "", status: "now",
  releaseDate: null, genreIds: [],
};

function AdminMovies() {
  const [movies, setMovies] = useState<MovieDto[]>([]);
  const [allGenres, setAllGenres] = useState<Genre[]>([]);
  const [loading, setLoading] = useState(true);
  const [open, setOpen] = useState(false);
  const [editing, setEditing] = useState<MovieDto | null>(null);
  const [form, setForm] = useState<MovieFormPayload>(emptyForm);
  const [saving, setSaving] = useState(false);

  const refresh = async () => {
    try {
      const [m, g] = await Promise.all([admin.movies.list(true), genresApi.list()]);
      setMovies(m);
      setAllGenres(g);
    } finally { setLoading(false); }
  };
  useEffect(() => { refresh(); }, []);

  const openCreate = () => { setEditing(null); setForm(emptyForm); setOpen(true); };
  const openEdit = (m: MovieDto) => {
    setEditing(m);
    setForm({
      title: m.title, tagline: m.tagline || "", description: m.description || "",
      posterUrl: m.posterUrl || "", durationMinutes: m.durationMinutes,
      rating: m.rating, year: m.year, language: m.language, synopsis: m.synopsis,
      status: m.status, releaseDate: m.releaseDate || null,
      genreIds: m.genres.map((g) => g.id),
    });
    setOpen(true);
  };

  const handleSave = async () => {
    setSaving(true);
    try {
      if (editing) {
        await admin.movies.update(editing.id, form);
        toast.success("Movie updated");
      } else {
        await admin.movies.create(form);
        toast.success("Movie created");
      }
      setOpen(false);
      refresh();
    } catch (e) {
      toast.error(e instanceof ApiError ? e.message : "Failed");
    } finally { setSaving(false); }
  };

  const handleDelete = async (id: number) => {
    try {
      await admin.movies.delete(id);
      toast.success("Movie deleted");
      refresh();
    } catch (e) {
      toast.error(e instanceof ApiError ? e.message : "Delete failed");
    }
  };

  const toggleGenre = (id: number) => {
    setForm((f) => ({
      ...f,
      genreIds: f.genreIds.includes(id)
        ? f.genreIds.filter((g) => g !== id)
        : [...f.genreIds, id],
    }));
  };

  return (
    <div>
      <div className="mb-6 flex items-center justify-between">
        <div>
          <h1 className="font-display text-4xl tracking-wider">Movies</h1>
          <p className="text-sm text-muted-foreground">Manage movie catalog</p>
        </div>
        <Button onClick={openCreate}><Plus className="mr-2 h-4 w-4" /> Add Movie</Button>
      </div>

      {loading ? (
        <div className="py-12 text-center text-muted-foreground">Loading…</div>
      ) : (
        <div className="rounded-xl border border-border bg-card/60">
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead>Title</TableHead>
                <TableHead>Genres</TableHead>
                <TableHead>Duration</TableHead>
                <TableHead>Status</TableHead>
                <TableHead>Rating</TableHead>
                <TableHead className="text-right">Actions</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {movies.map((m) => (
                <TableRow key={m.id}>
                  <TableCell className="font-medium">{m.title}</TableCell>
                  <TableCell>
                    <div className="flex flex-wrap gap-1">
                      {m.genres.map((g) => (
                        <Badge key={g.id} variant="secondary" className="text-[10px]">{g.name}</Badge>
                      ))}
                    </div>
                  </TableCell>
                  <TableCell>{m.durationMinutes}m</TableCell>
                  <TableCell><Badge variant={m.status === "now" ? "default" : "outline"}>{m.status}</Badge></TableCell>
                  <TableCell>{m.rating}</TableCell>
                  <TableCell className="text-right">
                    <Button size="icon" variant="ghost" onClick={() => openEdit(m)}>
                      <Pencil className="h-4 w-4" />
                    </Button>
                    <AlertDialog>
                      <AlertDialogTrigger asChild>
                        <Button size="icon" variant="ghost" className="text-destructive">
                          <Trash2 className="h-4 w-4" />
                        </Button>
                      </AlertDialogTrigger>
                      <AlertDialogContent>
                        <AlertDialogHeader>
                          <AlertDialogTitle>Delete "{m.title}"?</AlertDialogTitle>
                          <AlertDialogDescription>This will soft-delete the movie.</AlertDialogDescription>
                        </AlertDialogHeader>
                        <AlertDialogFooter>
                          <AlertDialogCancel>Cancel</AlertDialogCancel>
                          <AlertDialogAction onClick={() => handleDelete(m.id)}>Delete</AlertDialogAction>
                        </AlertDialogFooter>
                      </AlertDialogContent>
                    </AlertDialog>
                  </TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        </div>
      )}

      {/* Create / Edit Dialog */}
      <Dialog open={open} onOpenChange={setOpen}>
        <DialogContent className="max-h-[90vh] max-w-2xl overflow-y-auto">
          <DialogHeader>
            <DialogTitle className="font-display text-2xl">
              {editing ? "Edit Movie" : "Create Movie"}
            </DialogTitle>
          </DialogHeader>
          <div className="grid gap-4 py-4 sm:grid-cols-2">
            <div className="sm:col-span-2">
              <label className="text-xs text-muted-foreground">Title</label>
              <Input value={form.title} onChange={(e) => setForm({ ...form, title: e.target.value })} />
            </div>
            <div className="sm:col-span-2">
              <label className="text-xs text-muted-foreground">Tagline</label>
              <Input value={form.tagline} onChange={(e) => setForm({ ...form, tagline: e.target.value })} />
            </div>
            <div>
              <label className="text-xs text-muted-foreground">Duration (min)</label>
              <Input type="number" value={form.durationMinutes} onChange={(e) => setForm({ ...form, durationMinutes: +e.target.value })} />
            </div>
            <div>
              <label className="text-xs text-muted-foreground">Rating</label>
              <Input type="number" step="0.1" value={form.rating} onChange={(e) => setForm({ ...form, rating: +e.target.value })} />
            </div>
            <div>
              <label className="text-xs text-muted-foreground">Year</label>
              <Input type="number" value={form.year} onChange={(e) => setForm({ ...form, year: +e.target.value })} />
            </div>
            <div>
              <label className="text-xs text-muted-foreground">Language</label>
              <Input value={form.language} onChange={(e) => setForm({ ...form, language: e.target.value })} />
            </div>
            <div>
              <label className="text-xs text-muted-foreground">Status</label>
              <Select value={form.status} onValueChange={(v) => setForm({ ...form, status: v as "now" | "soon" })}>
                <SelectTrigger><SelectValue /></SelectTrigger>
                <SelectContent>
                  <SelectItem value="now">Now Showing</SelectItem>
                  <SelectItem value="soon">Coming Soon</SelectItem>
                </SelectContent>
              </Select>
            </div>
            <div>
              <label className="text-xs text-muted-foreground">Release Date</label>
              <Input value={form.releaseDate || ""} onChange={(e) => setForm({ ...form, releaseDate: e.target.value || null })} placeholder="e.g. Dec 12" />
            </div>
            <div className="sm:col-span-2">
              <label className="text-xs text-muted-foreground">Poster URL</label>
              <Input value={form.posterUrl} onChange={(e) => setForm({ ...form, posterUrl: e.target.value })} />
            </div>
            <div className="sm:col-span-2">
              <label className="text-xs text-muted-foreground">Synopsis</label>
              <textarea className="w-full rounded-md border border-border bg-secondary/40 p-3 text-sm" rows={3} value={form.synopsis} onChange={(e) => setForm({ ...form, synopsis: e.target.value })} />
            </div>
            <div className="sm:col-span-2">
              <label className="text-xs text-muted-foreground">Genres</label>
              <div className="mt-2 flex flex-wrap gap-2">
                {allGenres.map((g) => (
                  <button key={g.id} type="button" onClick={() => toggleGenre(g.id)}
                    className={`rounded-full border px-3 py-1 text-xs transition-colors ${form.genreIds.includes(g.id) ? "border-accent bg-accent/15 text-accent" : "border-border text-muted-foreground hover:border-accent/40"}`}>
                    {g.name}
                  </button>
                ))}
              </div>
            </div>
          </div>
          <DialogFooter>
            <DialogClose asChild><Button variant="ghost">Cancel</Button></DialogClose>
            <Button onClick={handleSave} disabled={saving || !form.title}>
              {saving ? "Saving…" : editing ? "Update" : "Create"}
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </div>
  );
}
