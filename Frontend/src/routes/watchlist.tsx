import { createFileRoute, Link, useNavigate } from "@tanstack/react-router";
import { motion } from "framer-motion";
import { useEffect, useState } from "react";
import { BookMarked, Star, Trash2 } from "lucide-react";
import { toast } from "sonner";
import { ROUTES } from "@/constants/routes";
import { useAuth } from "@/hooks/use-auth";
import { watchlist, type WatchlistItemDto, ApiError, resolvePoster } from "@/lib/api";

export const Route = createFileRoute("/watchlist")({
  head: () => ({
    meta: [{ title: "My Watchlist — CineReserve" }],
  }),
  component: WatchlistPage,
});

function WatchlistPage() {
  const { user, hydrated } = useAuth();
  const navigate = useNavigate();
  const [items, setItems] = useState<WatchlistItemDto[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (hydrated && !user) navigate({ to: ROUTES.login });
  }, [hydrated, user, navigate]);

  useEffect(() => {
    if (!user) return;
    watchlist
      .list()
      .then(setItems)
      .catch(() => toast.error("Failed to load watchlist"))
      .finally(() => setLoading(false));
  }, [user]);

  const remove = async (movieId: number) => {
    try {
      await watchlist.remove(movieId);
      setItems((prev) => prev.filter((i) => i.movieId !== movieId));
      toast.success("Removed from watchlist");
    } catch (e) {
      toast.error(e instanceof ApiError ? e.message : "Failed to remove");
    }
  };

  if (!user || loading) {
    return (
      <div className="mx-auto max-w-3xl px-4 py-24 text-center font-display text-3xl text-muted-foreground">
        Loading…
      </div>
    );
  }

  return (
    <div className="mx-auto max-w-4xl px-4 pb-24 pt-12 sm:px-6">
      <motion.div
        initial={{ opacity: 0, y: 16 }}
        animate={{ opacity: 1, y: 0 }}
        className="mb-10"
      >
        <div className="mb-2 text-xs uppercase tracking-widest text-accent">Saved</div>
        <h1 className="font-display text-5xl md:text-7xl">My Watchlist</h1>
      </motion.div>

      {items.length === 0 ? (
        <div className="flex flex-col items-center py-20 text-center">
          <BookMarked className="mb-4 h-12 w-12 text-muted-foreground/30" />
          <h2 className="font-display text-3xl">Nothing saved yet</h2>
          <p className="mt-2 text-muted-foreground">
            Browse movies and click the bookmark icon to save them here.
          </p>
          <Link
            to={ROUTES.movies}
            className="mt-6 rounded-md bg-accent px-5 py-2.5 text-sm font-medium text-background hover:bg-accent/90"
          >
            Browse Movies
          </Link>
        </div>
      ) : (
        <div className="grid gap-4 sm:grid-cols-2 md:grid-cols-3">
          {items.map((item, i) => (
            <motion.div
              key={item.movieId}
              initial={{ opacity: 0, y: 16 }}
              animate={{ opacity: 1, y: 0 }}
              transition={{ delay: i * 0.05 }}
              className="group relative overflow-hidden rounded-2xl border border-border bg-card/80"
            >
              <Link to={`/movies/${item.movieId}`}>
                <img
                  src={resolvePoster(item.moviePosterUrl)}
                  alt={item.movieTitle}
                  className="aspect-[2/3] w-full object-cover transition-transform duration-300 group-hover:scale-105"
                />
              </Link>
              <div className="p-4">
                <Link to={`/movies/${item.movieId}`}>
                  <h3 className="font-display text-lg leading-tight hover:text-accent">
                    {item.movieTitle}
                  </h3>
                </Link>
                <div className="mt-2 flex items-center justify-between">
                  <span className="text-xs text-muted-foreground">
                    Added {new Date(item.addedAt).toLocaleDateString()}
                  </span>
                  <button
                    onClick={() => remove(item.movieId)}
                    className="rounded-lg p-1.5 text-muted-foreground transition-colors hover:bg-destructive/10 hover:text-destructive"
                    title="Remove from watchlist"
                  >
                    <Trash2 className="h-4 w-4" />
                  </button>
                </div>
              </div>
            </motion.div>
          ))}
        </div>
      )}
    </div>
  );
}
