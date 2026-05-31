import { createFileRoute } from "@tanstack/react-router";
import { useEffect, useState } from "react";
import { Plus, Pencil, Trash2 } from "lucide-react";
import { toast } from "sonner";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
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
import { admin, ApiError, type HallDto, type HallFormPayload } from "@/lib/api";

export const Route = createFileRoute("/admin/halls")({ component: AdminHalls });

const emptyForm: HallFormPayload = { name: "", totalRows: 10, seatsPerRow: 12 };

function AdminHalls() {
  const [list, setList] = useState<HallDto[]>([]);
  const [loading, setLoading] = useState(true);
  const [open, setOpen] = useState(false);
  const [editing, setEditing] = useState<HallDto | null>(null);
  const [form, setForm] = useState<HallFormPayload>(emptyForm);
  const [saving, setSaving] = useState(false);

  const refresh = async () => {
    try { setList(await admin.halls.list()); }
    finally { setLoading(false); }
  };
  useEffect(() => { refresh(); }, []);

  const openCreate = () => { setEditing(null); setForm(emptyForm); setOpen(true); };
  const openEdit = (h: HallDto) => {
    setEditing(h);
    setForm({ name: h.name, totalRows: h.totalRows, seatsPerRow: h.seatsPerRow });
    setOpen(true);
  };

  const handleSave = async () => {
    setSaving(true);
    try {
      if (editing) {
        await admin.halls.update(editing.id, form);
        toast.success("Hall updated");
      } else {
        await admin.halls.create(form);
        toast.success("Hall created");
      }
      setOpen(false);
      refresh();
    } catch (e) { toast.error(e instanceof ApiError ? e.message : "Failed"); }
    finally { setSaving(false); }
  };

  const handleDelete = async (id: number) => {
    try {
      await admin.halls.delete(id);
      toast.success("Hall deleted");
      refresh();
    } catch (e) { toast.error(e instanceof ApiError ? e.message : "Delete failed"); }
  };

  return (
    <div>
      <div className="mb-6 flex items-center justify-between">
        <div>
          <h1 className="font-display text-4xl tracking-wider">Halls</h1>
          <p className="text-sm text-muted-foreground">Manage theaters / halls</p>
        </div>
        <Button onClick={openCreate}><Plus className="mr-2 h-4 w-4" /> Add Hall</Button>
      </div>

      {loading ? (
        <div className="py-12 text-center text-muted-foreground">Loading…</div>
      ) : (
        <div className="rounded-xl border border-border bg-card/60">
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead>Name</TableHead>
                <TableHead>Rows</TableHead>
                <TableHead>Seats/Row</TableHead>
                <TableHead>Total Capacity</TableHead>
                <TableHead className="text-right">Actions</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {list.map((h) => (
                <TableRow key={h.id}>
                  <TableCell className="font-medium">{h.name}</TableCell>
                  <TableCell>{h.totalRows}</TableCell>
                  <TableCell>{h.seatsPerRow}</TableCell>
                  <TableCell>{h.totalRows * h.seatsPerRow}</TableCell>
                  <TableCell className="text-right">
                    <Button size="icon" variant="ghost" onClick={() => openEdit(h)}>
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
                          <AlertDialogTitle>Delete "{h.name}"?</AlertDialogTitle>
                          <AlertDialogDescription>Halls with scheduled showtimes cannot be deleted.</AlertDialogDescription>
                        </AlertDialogHeader>
                        <AlertDialogFooter>
                          <AlertDialogCancel>Cancel</AlertDialogCancel>
                          <AlertDialogAction onClick={() => handleDelete(h.id)}>Delete</AlertDialogAction>
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

      <Dialog open={open} onOpenChange={setOpen}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle className="font-display text-2xl">
              {editing ? "Edit Hall" : "Create Hall"}
            </DialogTitle>
          </DialogHeader>
          <div className="grid gap-4 py-4">
            <div><label className="text-xs text-muted-foreground">Name</label>
              <Input value={form.name} onChange={(e) => setForm({ ...form, name: e.target.value })} />
            </div>
            <div className="grid grid-cols-2 gap-4">
              <div><label className="text-xs text-muted-foreground">Rows</label>
                <Input type="number" value={form.totalRows} onChange={(e) => setForm({ ...form, totalRows: +e.target.value })} />
              </div>
              <div><label className="text-xs text-muted-foreground">Seats per Row</label>
                <Input type="number" value={form.seatsPerRow} onChange={(e) => setForm({ ...form, seatsPerRow: +e.target.value })} />
              </div>
            </div>
          </div>
          <DialogFooter>
            <DialogClose asChild><Button variant="ghost">Cancel</Button></DialogClose>
            <Button onClick={handleSave} disabled={saving || !form.name}>{saving ? "Saving…" : editing ? "Update" : "Create"}</Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </div>
  );
}
