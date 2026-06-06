import { createFileRoute, Link, notFound } from "@tanstack/react-router";
import { motion } from "framer-motion";
import { ArrowLeft, Bookmark, BookmarkCheck, Clock, Globe, Play, Star, Trash2, Users } from "lucide-react";
import { useEffect, useState } from "react";
import {
  movies as moviesApi,
  reviews as reviewsApi,
  watchlist as watchlistApi,
  resolvePoster,
  getToken,
  type MovieDto,
  type ShowtimeDto,
  type ReviewDto,
  ApiError,
} from "@/lib/api";
import { ROUTES } from "@/constants/routes";
import { HammerButton } from "@/components/HammerButton";
import { toast } from "sonner";

export const Route = createFileRoute("/movies/$id")({
  loader: async ({ params }) => {
    try {
      const [movie, showtimes, reviews] = await Promise.all([
        moviesApi.get(params.id),
        moviesApi.showtimes(params.id),
        reviewsApi.list(params.id).catch(() => [] as ReviewDto[]),
      ]);
      return { movie, showtimes, reviews };
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
    reviews: ReviewDto[];
  };
  const movie = loaderData.movie;
  const [showtimes, setShowtimes] = useState<ShowtimeDto[]>(
    loaderData.showtimes ?? [],
  );
  const [reviewsList, setReviewsList] = useState<ReviewDto[]>(
    loaderData.reviews ?? [],
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
  const isLoggedIn = !!getToken();

  const [inWatchlist, setInWatchlist] = useState(false);
  const [watchlistLoading, setWatchlistLoading] = useState(false);

  useEffect(() => {
    if (!isLoggedIn) return;
    watchlistApi.status(movie.id).then((r) => setInWatchlist(r.inWatchlist)).catch(() => {});
  }, [movie.id, isLoggedIn]);

  const toggleWatchlist = async () => {
    if (!isLoggedIn) {
      toast.error("Sign in to manage your watchlist");
      return;
    }
    setWatchlistLoading(true);
    try {
      if (inWatchlist) {
        await watchlistApi.remove(movie.id);
        setInWatchlist(false);
        toast.success("Removed from watchlist");
      } else {
        await watchlistApi.add(movie.id);
        setInWatchlist(true);
        toast.success("Added to watchlist");
      }
    } catch (e) {
      toast.error(e instanceof ApiError ? e.message : "Failed to update watchlist");
    } finally {
      setWatchlistLoading(false);
    }
  };

  return (
    <div>
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
            <div className="mt-2 flex items-center gap-3">
              <p className="text-lg italic text-accent">"{movie.tagline}"</p>
              <button
                onClick={toggleWatchlist}
                disabled={watchlistLoading}
                title={inWatchlist ? "Remove from watchlist" : "Add to watchlist"}
                className="ml-auto rounded-full border border-border p-2 transition-colors hover:bg-accent/10 disabled:opacity-50"
              >
                {inWatchlist ? (
                  <BookmarkCheck className="h-5 w-5 text-accent" />
                ) : (
                  <Bookmark className="h-5 w-5 text-muted-foreground" />
                )}
              </button>
            </div>

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

            <div className="mt-4 flex flex-wrap items-center gap-3 text-sm text-muted-foreground">
              {movie.censorRating && (
                <span className="rounded border border-border px-2 py-0.5 text-xs font-semibold uppercase tracking-widest">
                  {movie.censorRating}
                </span>
              )}
              {movie.director && (
                <span>Dir. <span className="text-foreground">{movie.director}</span></span>
              )}
              {movie.cast && (
                <span className="flex items-center gap-1">
                  <Users className="h-3.5 w-3.5" />
                  {movie.cast}
                </span>
              )}
            </div>

            <p className="mt-6 max-w-2xl text-base leading-relaxed text-muted-foreground">
              {movie.synopsis}
            </p>

            {movie.trailerUrl && (
              <div className="mt-5">
                <a href={movie.trailerUrl} target="_blank" rel="noopener noreferrer">
                  <HammerButton variant="outline" size="sm">
                    <Play className="h-4 w-4 fill-current" /> Watch Trailer
                  </HammerButton>
                </a>
              </div>
            )}

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

      <ReviewsSection movieId={movie.id} reviews={reviewsList} setReviews={setReviewsList} />
    </div>
  );
}

function ReviewsSection({
  movieId,
  reviews,
  setReviews,
}: {
  movieId: number;
  reviews: ReviewDto[];
  setReviews: React.Dispatch<React.SetStateAction<ReviewDto[]>>;
}) {
  const isLoggedIn = !!getToken();
  const [rating, setRating] = useState(0);
  const [hoverRating, setHoverRating] = useState(0);
  const [comment, setComment] = useState("");
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState("");

  const avgRating =
    reviews.length > 0
      ? reviews.reduce((sum, r) => sum + r.rating, 0) / reviews.length
      : 0;

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    if (rating === 0) {
      setError("Please select a rating");
      return;
    }
    setSubmitting(true);
    setError("");
    try {
      const newReview = await reviewsApi.create(movieId, rating, comment);
      setReviews((prev) => [newReview, ...prev]);
      setRating(0);
      setComment("");
    } catch (err: any) {
      setError(err.message || "Failed to submit review");
    } finally {
      setSubmitting(false);
    }
  }

  async function handleDelete(reviewId: number) {
    try {
      await reviewsApi.delete(movieId, reviewId);
      setReviews((prev) => prev.filter((r) => r.id !== reviewId));
    } catch {
      // ignore
    }
  }

  return (
    <section className="mx-auto max-w-7xl px-4 py-16 sm:px-6 lg:px-8">
      <div className="grid gap-10 lg:grid-cols-[1fr_360px]">
        <div>
          <h2 className="font-display text-3xl tracking-wider">
            Reviews
            {reviews.length > 0 && (
              <span className="ml-3 text-lg text-muted-foreground">
                {avgRating.toFixed(1)} <Star className="mb-0.5 inline h-4 w-4 fill-accent text-accent" /> · {reviews.length} review{reviews.length !== 1 ? "s" : ""}
              </span>
            )}
          </h2>

          {reviews.length === 0 ? (
            <p className="mt-4 text-muted-foreground">
              No reviews yet. Be the first to share your thoughts!
            </p>
          ) : (
            <div className="mt-6 space-y-4">
              {reviews.map((r) => (
                <motion.div
                  key={r.id}
                  initial={{ opacity: 0, y: 10 }}
                  animate={{ opacity: 1, y: 0 }}
                  className="rounded-xl border border-border bg-card/60 p-4"
                >
                  <div className="flex items-start justify-between">
                    <div>
                      <div className="flex items-center gap-2">
                        <span className="font-medium">{r.userName}</span>
                        <span className="flex items-center gap-0.5 text-sm text-accent">
                          {Array.from({ length: 5 }, (_, i) => (
                            <Star
                              key={i}
                              className={`h-3.5 w-3.5 ${i < r.rating ? "fill-accent" : "fill-none text-muted-foreground/40"}`}
                            />
                          ))}
                        </span>
                      </div>
                      <p className="mt-1 text-sm text-muted-foreground">
                        {new Date(r.createdAt).toLocaleDateString()}
                      </p>
                    </div>
                    {isLoggedIn && (
                      <button
                        onClick={() => handleDelete(r.id)}
                        className="text-muted-foreground/50 transition-colors hover:text-red-400"
                        title="Delete review"
                      >
                        <Trash2 className="h-4 w-4" />
                      </button>
                    )}
                  </div>
                  {r.comment && (
                    <p className="mt-2 text-sm leading-relaxed text-muted-foreground">
                      {r.comment}
                    </p>
                  )}
                </motion.div>
              ))}
            </div>
          )}
        </div>

        <div>
          {isLoggedIn ? (
            <form
              onSubmit={handleSubmit}
              className="sticky top-24 rounded-xl border border-border bg-card/60 p-6"
            >
              <h3 className="font-display text-xl tracking-wider">Write a review</h3>

              <div className="mt-4 flex gap-1">
                {Array.from({ length: 5 }, (_, i) => (
                  <button
                    type="button"
                    key={i}
                    onClick={() => setRating(i + 1)}
                    onMouseEnter={() => setHoverRating(i + 1)}
                    onMouseLeave={() => setHoverRating(0)}
                    className="transition-transform hover:scale-110"
                  >
                    <Star
                      className={`h-7 w-7 ${
                        i < (hoverRating || rating)
                          ? "fill-accent text-accent"
                          : "fill-none text-muted-foreground/40"
                      }`}
                    />
                  </button>
                ))}
              </div>

              <textarea
                value={comment}
                onChange={(e) => setComment(e.target.value)}
                placeholder="Share your thoughts (optional)"
                rows={4}
                className="mt-4 w-full resize-none rounded-lg border border-border bg-background/60 px-3 py-2 text-sm placeholder:text-muted-foreground/50 focus:border-accent focus:outline-none"
              />

              {error && (
                <p className="mt-2 text-sm text-red-400">{error}</p>
              )}

              <HammerButton
                type="submit"
                variant="gold"
                size="sm"
                disabled={submitting}
                className="mt-4 w-full"
              >
                {submitting ? "Submitting…" : "Submit Review"}
              </HammerButton>
            </form>
          ) : (
            <div className="rounded-xl border border-border bg-card/60 p-6 text-center">
              <p className="text-muted-foreground">
                <Link to={ROUTES.login} className="text-accent hover:underline">
                  Sign in
                </Link>{" "}
                to leave a review
              </p>
            </div>
          )}
        </div>
      </div>
    </section>
  );
}
