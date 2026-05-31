import { createFileRoute } from "@tanstack/react-router";
import { useEffect, useState } from "react";
import { Plus, X } from "lucide-react";
import { toast } from "sonner";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { admin, genres as genresApi, ApiError, type Genre } from "@/lib/api";

export const Route = createFileRoute("/admin/genres")({ component: AdminGenres });

function AdminGenres() {
  const [list, setList] = useState<Genre[]>([]);
  const [loading, setLoading] = useState(true);
  const [name, setName] = useState("");
  const [adding, setAdding] = useState(false);

  const refresh = async () => {
    try { setList(await genresApi.list()); }
    finally { setLoading(false); }
  };
  useEffect(() => { refresh(); }, []);

  const handleAdd = async () => {
    if (!name.trim()) return;
    setAdding(true);
    try {
      await admin.genres.create(name.trim());
      toast.success(`Genre "${name.trim()}" created`);
      setName("");
      refresh();
    } catch (e) { toast.error(e instanceof ApiError ? e.message : "Failed"); }
    finally { setAdding(false); }
  };

  const handleDelete = async (g: Genre) => {
    try {
      await admin.genres.delete(g.id);
      toast.success(`"${g.name}" removed`);
      refresh();
    } catch (e) { toast.error(e instanceof ApiError ? e.message : "Failed"); }
  };

  return (
    <div>
      <div className="mb-6">
        <h1 className="font-display text-4xl tracking-wider">Genres</h1>
        <p className="text-sm text-muted-foreground">Manage movie genres</p>
      </div>

      <div className="mb-6 flex gap-2">
        <Input
          placeholder="New genre name…"
          value={name}
          onChange={(e) => setName(e.target.value)}
          onKeyDown={(e) => e.key === "Enter" && handleAdd()}
          className="max-w-xs"
        />
        <Button onClick={handleAdd} disabled={adding || !name.trim()}>
          <Plus className="mr-2 h-4 w-4" /> Add
        </Button>
      </div>

      {loading ? (
        <div className="py-12 text-center text-muted-foreground">Loading…</div>
      ) : (
        <div className="flex flex-wrap gap-2">
          {list.map((g) => (
            <div
              key={g.id}
              className="group flex items-center gap-2 rounded-full border border-border bg-card/60 px-4 py-2 text-sm"
            >
              <span>{g.name}</span>
              <button
                onClick={() => handleDelete(g)}
                className="grid h-5 w-5 place-items-center rounded-full text-muted-foreground opacity-0 transition-all hover:bg-destructive/20 hover:text-destructive group-hover:opacity-100"
              >
                <X className="h-3 w-3" />
              </button>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
