import { createFileRoute } from "@tanstack/react-router";
import { motion } from "framer-motion";
import { Search } from "lucide-react";
import { useEffect, useMemo, useRef, useState } from "react";
import { STRINGS } from "@/constants/strings";
import { movies as moviesApi, type MovieDto } from "@/lib/api";
import { MovieCard } from "@/components/MovieCard";

export const Route = createFileRoute("/movies/")({
  head: () => ({
    meta: [
      { title: "Movies — CineReserve" },
      {
        name: "description",
        content: "Browse all films now playing and coming soon at CineReserve.",
      },
    ],
  }),
  component: MoviesPage,
});

type StatusFilter = "all" | "now" | "soon";

function MoviesPage() {
  const [q, setQ] = useState("");
  const [debouncedQ, setDebouncedQ] = useState("");
  const [genre, setGenre] = useState<string | null>(null);
  const [statusFilter, setStatusFilter] = useState<StatusFilter>("all");
  const [list, setList] = useState<MovieDto[]>([]);
  const [allMovies, setAllMovies] = useState<MovieDto[]>([]);
  const [loading, setLoading] = useState(true);
  const debounceRef = useRef<ReturnType<typeof setTimeout>>();

  useEffect(() => {
    debounceRef.current = setTimeout(() => setDebouncedQ(q), 300);
    return () => clearTimeout(debounceRef.current);
  }, [q]);

  useEffect(() => {
    let cancelled = false;
    const params: { status?: string; genre?: string; search?: string } = {};
    if (statusFilter !== "all") params.status = statusFilter;
    if (genre) params.genre = genre;
    if (debouncedQ.trim()) params.search = debouncedQ.trim();

    setLoading(true);
    moviesApi
      .list(params)
      .then((data) => { if (!cancelled) { setList(data); if (!debouncedQ && !genre && statusFilter === "all") setAllMovies(data); } })
      .finally(() => { if (!cancelled) setLoading(false); });
    return () => { cancelled = true; };
  }, [debouncedQ, genre, statusFilter]);

  const genres = useMemo(
    () => Array.from(new Set(allMovies.flatMap((m) => m.genres.map((g) => g.name)))),
    [allMovies],
  );

  return (
    <div className="mx-auto max-w-7xl px-4 pb-24 pt-12 sm:px-6 lg:px-8">
      <motion.div
        initial={{ opacity: 0, y: 16 }}
        animate={{ opacity: 1, y: 0 }}
        className="mb-10"
      >
        <div className="mb-2 text-xs uppercase tracking-widest text-accent">
          The lineup
        </div>
        <h1 className="font-display text-5xl md:text-7xl">
          {STRINGS.movies.pageTitle}
        </h1>
        <p className="mt-2 text-muted-foreground">{STRINGS.movies.pageSub}</p>
      </motion.div>

      <div className="mb-8 flex flex-col gap-4">
        <div className="relative">
          <Search className="absolute left-4 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground" />
          <input
            value={q}
            onChange={(e) => setQ(e.target.value)}
            placeholder={STRINGS.movies.searchPlaceholder}
            aria-label={STRINGS.movies.searchPlaceholder}
            className="h-12 w-full rounded-full border border-border bg-card/40 pl-11 pr-4 text-sm outline-none transition-all focus:border-accent focus:bg-card"
          />
        </div>

        {/* Status filter */}
        <div className="no-scrollbar flex gap-2 overflow-x-auto pb-1">
          {(["all", "now", "soon"] as StatusFilter[]).map((s) => (
            <Chip key={s} active={statusFilter === s} onClick={() => setStatusFilter(s)}>
              {s === "all" ? "All" : s === "now" ? "Now Showing" : "Coming Soon"}
            </Chip>
          ))}
        </div>

        {/* Genre filter */}
        <div className="no-scrollbar flex gap-2 overflow-x-auto pb-1">
          <Chip active={!genre} onClick={() => setGenre(null)}>
            All Genres
          </Chip>
          {genres.map((g) => (
            <Chip key={g} active={genre === g} onClick={() => setGenre(g)}>
              {g}
            </Chip>
          ))}
        </div>
      </div>

      {loading ? (
        <div className="grid grid-cols-2 gap-6 sm:grid-cols-3 lg:grid-cols-4">
          {Array.from({ length: 8 }).map((_, i) => (
            <div
              key={i}
              className="aspect-[2/3] animate-pulse rounded-2xl border border-border bg-card"
            />
          ))}
        </div>
      ) : list.length === 0 ? (
        <div className="rounded-2xl border border-border bg-card p-16 text-center text-muted-foreground">
          {STRINGS.movies.noResults}
        </div>
      ) : (
        <div className="grid grid-cols-2 gap-6 sm:grid-cols-3 lg:grid-cols-4">
          {list.map((m, i) => (
            <MovieCard key={m.id} movie={m} index={i} />
          ))}
        </div>
      )}
    </div>
  );
}

function Chip({
  active,
  children,
  onClick,
}: {
  active: boolean;
  children: React.ReactNode;
  onClick: () => void;
}) {
  return (
    <button
      onClick={onClick}
      className={`shrink-0 rounded-full border px-4 py-1.5 text-xs uppercase tracking-widest transition-all ${
        active
          ? "border-accent bg-accent text-accent-foreground"
          : "border-border bg-card/40 text-muted-foreground hover:border-accent/40 hover:text-foreground"
      }`}
    >
      {children}
    </button>
  );
}
