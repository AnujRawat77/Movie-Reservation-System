import { Link } from "@tanstack/react-router";
import { motion } from "framer-motion";
import { Clock, Star } from "lucide-react";
import { ROUTES } from "@/constants/routes";
import { resolvePoster, type MovieDto } from "@/lib/api";

export function MovieCard({
  movie,
  index = 0,
}: {
  movie: MovieDto;
  index?: number;
}) {
  return (
    <motion.div
      initial={{ opacity: 0, y: 24 }}
      whileInView={{ opacity: 1, y: 0 }}
      viewport={{ once: true, margin: "-50px" }}
      transition={{ duration: 0.5, delay: index * 0.05 }}
    >
      <Link
        to={ROUTES.movie(String(movie.id))}
        className="group block overflow-hidden rounded-2xl border border-border bg-card shadow-elegant transition-all duration-500 hover:-translate-y-2 hover:border-accent/40 hover:shadow-glow"
      >
        <div className="relative aspect-[2/3] overflow-hidden">
          <img
            src={resolvePoster(movie.posterUrl)}
            alt={movie.title}
            loading="lazy"
            className="h-full w-full object-cover transition-transform duration-700 group-hover:scale-110"
          />
          <div className="absolute inset-0 bg-gradient-to-t from-card via-card/30 to-transparent opacity-90" />
          <div className="absolute right-3 top-3 flex items-center gap-1 rounded-full bg-background/70 px-2.5 py-1 text-xs backdrop-blur">
            <Star className="h-3 w-3 fill-accent text-accent" />
            <span className="font-medium">{movie.rating.toFixed(1)}</span>
          </div>
          {movie.status === "soon" && movie.releaseDate && (
            <div className="absolute left-3 top-3 rounded-full bg-gradient-gold px-2.5 py-1 text-[10px] font-semibold uppercase tracking-wider text-gold-foreground">
              {movie.releaseDate}
            </div>
          )}
        </div>
        <div className="space-y-2 p-4">
          <h3 className="font-display text-xl leading-none tracking-wide">
            {movie.title}
          </h3>
          <p className="line-clamp-1 text-xs text-muted-foreground">
            {movie.tagline}
          </p>
          <div className="flex items-center gap-3 pt-2 text-xs text-muted-foreground">
            <span className="flex items-center gap-1">
              <Clock className="h-3 w-3" />
              {movie.durationMinutes}m
            </span>
            <span>·</span>
            <span>{movie.genres[0]?.name ?? ""}</span>
          </div>
        </div>
      </Link>
    </motion.div>
  );
}
