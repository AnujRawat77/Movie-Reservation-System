import { createFileRoute, Link, notFound } from "@tanstack/react-router";
import { motion } from "framer-motion";
import { ArrowLeft, Clock, Globe, Star } from "lucide-react";
import { useEffect, useState } from "react";
import {
  movies as moviesApi,
  resolvePoster,
  type MovieDto,
  type ShowtimeDto,
} from "@/lib/api";
import { ROUTES } from "@/constants/routes";
import { HammerButton } from "@/components/HammerButton";

export const Route = createFileRoute("/movies/$id")({
  loader: async ({ params }) => {
    try {
      const movie = await moviesApi.get(params.id);
      const showtimes = await moviesApi.showtimes(params.id);
      return { movie, showtimes };
    } catch {
      throw notFound();
    }
  },
  head: ({ loaderData }) => ({
    meta: loaderData
      ? [
          { title: `${loaderData.movie.title} — CineReserve` },
          { name: "description", content: loaderData.movie.synopsis },
          { property: "og:title", content: loaderData.movie.title },
          { property: "og:description", content: loaderData.movie.synopsis },
        ]
      : [],
  }),
  component: MovieDetail,
  notFoundComponent: () => (
    <div className="mx-auto max-w-2xl px-4 py-32 text-center">
      <h1 className="font-display text-5xl">Movie not found</h1>
      <Link to={ROUTES.movies} className="mt-6 inline-block text-accent">
        ← Back to movies
      </Link>
    </div>
  ),
});

function MovieDetail() {
  const loaderData = Route.useLoaderData() as {
    movie: MovieDto;
    showtimes: ShowtimeDto[];
  };
  const movie = loaderData.movie;
  const [showtimes, setShowtimes] = useState<ShowtimeDto[]>(
    loaderData.showtimes ?? [],
  );

  useEffect(() => {
    // Refresh on client to ensure fresh data
    moviesApi
      .showtimes(movie.id)
      .then(setShowtimes)
      .catch(() => {});
  }, [movie.id]);

  const dates = Array.from(new Set(showtimes.map((s) => s.date)));
  const [selectedDate, setSelectedDate] = useState(dates[0] ?? "Today");
  const [selectedShowtime, setSelectedShowtime] = useState<number | null>(null);

  useEffect(() => {
    if (!selectedDate && dates.length) setSelectedDate(dates[0]);
  }, [dates, selectedDate]);

  const slots = showtimes.filter((s) => s.date === selectedDate);
  const poster = resolvePoster(movie.posterUrl);

  return (
    <>
      <section className="relative -mt-16 overflow-hidden pt-16">
        <div className="absolute inset-0 -z-20">
          <img
            src={poster}
            alt=""
            className="h-full w-full object-cover opacity-30 blur-2xl"
          />
        </div>
        <div className="absolute inset-0 -z-10 bg-gradient-to-b from-background/40 via-background/80 to-background" />

        <div className="mx-auto grid max-w-7xl gap-10 px-4 py-16 sm:px-6 lg:grid-cols-[360px_1fr] lg:px-8">
          <motion.div
            initial={{ opacity: 0, x: -20 }}
            animate={{ opacity: 1, x: 0 }}
            className="overflow-hidden rounded-2xl border border-border shadow-elegant"
          >
            <img
              src={poster}
              alt={movie.title}
              width={768}
              height={1152}
              className="aspect-[2/3] w-full object-cover"
            />
          </motion.div>

          <motion.div
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ delay: 0.1 }}
          >
            <Link
              to={ROUTES.movies}
              className="mb-4 inline-flex items-center gap-2 text-sm text-muted-foreground hover:text-foreground"
            >
              <ArrowLeft className="h-4 w-4" /> All movies
            </Link>

            <h1 className="font-display text-6xl leading-none md:text-8xl">
              {movie.title}
            </h1>
            <p className="mt-3 text-lg italic text-accent">"{movie.tagline}"</p>

            <div className="mt-6 flex flex-wrap items-center gap-4 text-sm text-muted-foreground">
              <span className="flex items-center gap-1.5">
                <Star className="h-4 w-4 fill-accent text-accent" />
                <span className="font-medium text-foreground">
                  {movie.rating.toFixed(1)}
                </span>
                /10
              </span>
              <span className="flex items-center gap-1.5">
                <Clock className="h-4 w-4" /> {movie.durationMinutes} min
              </span>
              <span className="flex items-center gap-1.5">
                <Globe className="h-4 w-4" /> {movie.language}
              </span>
              <span>· {movie.year}</span>
            </div>

            <div className="mt-4 flex flex-wrap gap-2">
              {movie.genres.map((g) => (
                <span
                  key={g.id}
                  className="rounded-full border border-border bg-card/60 px-3 py-1 text-xs uppercase tracking-widest"
                >
                  {g.name}
                </span>
              ))}
            </div>

            <p className="mt-8 max-w-2xl text-base leading-relaxed text-muted-foreground">
              {movie.synopsis}
            </p>

            <div className="mt-10 rounded-2xl border border-border bg-card/60 p-6">
              <h2 className="mb-4 font-display text-2xl tracking-wider">
                Choose a showtime
              </h2>

              {showtimes.length === 0 ? (
                <p className="text-sm text-muted-foreground">
                  No showtimes scheduled yet. Check back soon.
                </p>
              ) : (
                <>
                  <div className="mb-4 flex flex-wrap gap-2">
                    {dates.map((d) => (
                      <button
                        key={d}
                        onClick={() => setSelectedDate(d)}
                        className={`relative rounded-full px-4 py-2 text-sm transition-colors ${
                          selectedDate === d
                            ? "text-foreground"
                            : "text-muted-foreground hover:text-foreground"
                        }`}
                      >
                        {selectedDate === d && (
                          <motion.span
                            layoutId="date-pill"
                            className="absolute inset-0 -z-10 rounded-full bg-secondary"
                          />
                        )}
                        {d}
                      </button>
                    ))}
                  </div>

                  <div className="flex flex-wrap gap-3">
                    {slots.map((s) => (
                      <button
                        key={s.id}
                        onClick={() => setSelectedShowtime(s.id)}
                        className={`group rounded-xl border px-4 py-3 text-left transition-all ${
                          selectedShowtime === s.id
                            ? "border-accent bg-accent/10"
                            : "border-border bg-background/60 hover:border-accent/40"
                        }`}
                      >
                        <div className="font-display text-2xl tracking-wider">
                          {s.time}
                        </div>
                        <div className="text-[10px] uppercase tracking-widest text-muted-foreground">
                          {s.hallName}
                        </div>
                      </button>
                    ))}
                  </div>

                  <div className="mt-6">
                    <Link
                      to={ROUTES.booking(
                        String(selectedShowtime ?? slots[0]?.id ?? ""),
                      )}
                    >
                      <HammerButton variant="gold" size="lg">
                        Book Tickets
                      </HammerButton>
                    </Link>
                  </div>
                </>
              )}
            </div>
          </motion.div>
        </div>
      </section>
    </>
  );
}
