import poster1 from "@/assets/poster-1.jpg";
import poster2 from "@/assets/poster-2.jpg";
import poster3 from "@/assets/poster-3.jpg";
import poster4 from "@/assets/poster-4.jpg";
import poster5 from "@/assets/poster-5.jpg";
import poster6 from "@/assets/poster-6.jpg";

export type Movie = {
  id: string;
  title: string;
  tagline: string;
  poster: string;
  genres: string[];
  rating: number;
  duration: number; // minutes
  year: number;
  language: string;
  synopsis: string;
  status: "now" | "soon";
  releaseDate?: string;
  showtimes: { id: string; date: string; time: string; hall: string }[];
};

const mkShowtimes = (id: string) => [
  { id: `${id}-1`, date: "Today", time: "14:30", hall: "Hall 1 · IMAX" },
  { id: `${id}-2`, date: "Today", time: "18:00", hall: "Hall 3 · Dolby" },
  { id: `${id}-3`, date: "Today", time: "21:15", hall: "Hall 1 · IMAX" },
  { id: `${id}-4`, date: "Tomorrow", time: "16:00", hall: "Hall 2" },
  { id: `${id}-5`, date: "Tomorrow", time: "20:30", hall: "Hall 3 · Dolby" },
];

export const MOVIES: Movie[] = [
  {
    id: "midnight-rain",
    title: "Midnight Rain",
    tagline: "Every secret has a sound.",
    poster: poster1,
    genres: ["Noir", "Thriller"],
    rating: 8.7,
    duration: 128,
    year: 2026,
    language: "English",
    synopsis:
      "A weary detective chases a ghost through a city that never dries. When the rain falls, the truth follows.",
    status: "now",
    showtimes: mkShowtimes("midnight-rain"),
  },
  {
    id: "solaris-drift",
    title: "Solaris Drift",
    tagline: "Beyond the orbit, beyond the self.",
    poster: poster2,
    genres: ["Sci-Fi", "Drama"],
    rating: 9.1,
    duration: 142,
    year: 2026,
    language: "English",
    synopsis:
      "Stranded between worlds, an astronaut must decide what's worth coming home for.",
    status: "now",
    showtimes: mkShowtimes("solaris-drift"),
  },
  {
    id: "paris-after-eight",
    title: "Paris, After Eight",
    tagline: "A love letter, posted late.",
    poster: poster3,
    genres: ["Romance", "Drama"],
    rating: 8.2,
    duration: 112,
    year: 2026,
    language: "French",
    synopsis:
      "Two strangers, one rooftop, and a city that refuses to let them go their separate ways.",
    status: "now",
    showtimes: mkShowtimes("paris-after-eight"),
  },
  {
    id: "red-horizon",
    title: "Red Horizon",
    tagline: "The west still bleeds.",
    poster: poster4,
    genres: ["Western", "Action"],
    rating: 8.4,
    duration: 134,
    year: 2026,
    language: "English",
    synopsis:
      "An outlaw rides one last sundown to settle a debt older than the desert itself.",
    status: "now",
    showtimes: mkShowtimes("red-horizon"),
  },
  {
    id: "hollow-house",
    title: "The Hollow House",
    tagline: "It remembers everyone.",
    poster: poster5,
    genres: ["Horror", "Mystery"],
    rating: 7.9,
    duration: 119,
    year: 2026,
    language: "English",
    synopsis:
      "A grieving family inherits a manor that wasn't quite empty when they signed the papers.",
    status: "soon",
    releaseDate: "Dec 12",
    showtimes: mkShowtimes("hollow-house"),
  },
  {
    id: "lantern-grove",
    title: "Lantern Grove",
    tagline: "Make a wish. Make it big.",
    poster: poster6,
    genres: ["Animation", "Family"],
    rating: 9.0,
    duration: 96,
    year: 2026,
    language: "English",
    synopsis:
      "Two unlikely friends light up a forgotten forest — and remind it how to dream again.",
    status: "soon",
    releaseDate: "Dec 24",
    showtimes: mkShowtimes("lantern-grove"),
  },
];

export const findMovie = (id: string) => MOVIES.find((m) => m.id === id);
export const findShowtime = (id: string) => {
  for (const m of MOVIES) {
    const s = m.showtimes.find((x) => x.id === id);
    if (s) return { movie: m, showtime: s };
  }
  return null;
};
