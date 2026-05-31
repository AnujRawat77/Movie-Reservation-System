import { createFileRoute, Link } from "@tanstack/react-router";
import { motion } from "framer-motion";
import { ArrowRight, Play, Sparkles } from "lucide-react";
import { useEffect, useState } from "react";
import { STRINGS } from "@/constants/strings";
import { ROUTES } from "@/constants/routes";
import { movies as moviesApi, type MovieDto } from "@/lib/api";
import { HammerButton } from "@/components/HammerButton";
import { MovieCard } from "@/components/MovieCard";
import heroImg from "@/assets/hero-cinema.jpg";

export const Route = createFileRoute("/")({
  head: () => ({
    meta: [
      { title: "CineReserve — Your seat. Your story. Your cinema." },
      {
        name: "description",
        content:
          "Reserve premium cinema seats in seconds. Now-showing premieres, IMAX & Dolby halls, instant booking.",
      },
    ],
  }),
  component: Home,
});

function Home() {
  const [nowShowing, setNowShowing] = useState<MovieDto[]>([]);
  const [comingSoon, setComingSoon] = useState<MovieDto[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    let cancelled = false;
    (async () => {
      try {
        const all = await moviesApi.list();
        if (cancelled) return;
        setNowShowing(all.filter((m) => m.status === "now"));
        setComingSoon(all.filter((m) => m.status === "soon"));
      } catch (e) {
        // Silently fail on home page
      } finally {
        if (!cancelled) setLoading(false);
      }
    })();
    return () => {
      cancelled = true;
    };
  }, []);

  return (
    <>
      <section className="relative isolate -mt-16 flex min-h-[100dvh] items-center overflow-hidden pt-16">
        <img
          src={heroImg}
          alt=""
          width={1920}
          height={1280}
          className="absolute inset-0 -z-20 h-full w-full object-cover"
        />
        <div className="absolute inset-0 -z-10 bg-gradient-hero" />
        <div className="absolute inset-0 -z-10 bg-[radial-gradient(ellipse_at_center,_transparent_30%,_var(--background)_85%)]" />

        <div className="mx-auto w-full max-w-7xl px-4 sm:px-6 lg:px-8">
          <motion.div
            initial={{ opacity: 0, y: 30 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.8 }}
            className="max-w-3xl"
          >
            <div className="mb-6 inline-flex items-center gap-2 rounded-full border border-accent/30 bg-accent/10 px-4 py-1.5 text-xs uppercase tracking-widest text-accent backdrop-blur">
              <Sparkles className="h-3 w-3" />
              {STRINGS.home.heroEyebrow}
            </div>

            <h1 className="font-display text-6xl leading-[0.95] sm:text-7xl md:text-8xl lg:text-[8.5rem]">
              {STRINGS.home.heroTitle}
              <br />
              <span className="text-gradient-gold">
                {STRINGS.home.heroTitleAccent}
              </span>
            </h1>

            <p className="mt-8 max-w-xl text-lg text-muted-foreground sm:text-xl">
              {STRINGS.home.heroSubtitle}
            </p>

            <div className="mt-10 flex flex-wrap items-center gap-4">
              <Link to={ROUTES.movies}>
                <HammerButton size="lg" variant="gold">
                  {STRINGS.home.heroCta}
                  <ArrowRight className="h-4 w-4" />
                </HammerButton>
              </Link>
              <HammerButton size="lg" variant="outline">
                <Play className="h-4 w-4 fill-current" />
                {STRINGS.home.heroCtaSecondary}
              </HammerButton>
            </div>
          </motion.div>
        </div>

        <motion.div
          aria-hidden
          initial={{ x: "100%" }}
          animate={{ x: "-100%" }}
          transition={{ duration: 60, repeat: Infinity, ease: "linear" }}
          className="absolute bottom-12 left-0 flex w-max gap-12 whitespace-nowrap font-display text-[10rem] text-foreground/[0.04]"
        >
          {Array.from({ length: 6 }).map((_, i) => (
            <span key={i}>CINEMATIC · UNFORGETTABLE · IMMERSIVE ·</span>
          ))}
        </motion.div>
      </section>

      <section className="mx-auto max-w-7xl px-4 py-24 sm:px-6 lg:px-8">
        <div className="grid grid-cols-2 gap-px overflow-hidden rounded-3xl border border-border bg-border md:grid-cols-4">
          {[
            { v: "120+", l: STRINGS.home.stats.movies },
            { v: "12", l: STRINGS.home.stats.halls },
            { v: "850K", l: STRINGS.home.stats.guests },
            { v: "4.9", l: STRINGS.home.stats.rating },
          ].map((s, i) => (
            <motion.div
              key={s.l}
              initial={{ opacity: 0, y: 20 }}
              whileInView={{ opacity: 1, y: 0 }}
              viewport={{ once: true }}
              transition={{ delay: i * 0.08 }}
              className="bg-card p-8 text-center"
            >
              <div className="font-display text-5xl text-gradient-gold">
                {s.v}
              </div>
              <div className="mt-2 text-xs uppercase tracking-widest text-muted-foreground">
                {s.l}
              </div>
            </motion.div>
          ))}
        </div>
      </section>

      <Section
        eyebrow="Featured"
        title={STRINGS.home.nowShowing}
        subtitle={STRINGS.home.nowShowingSub}
      >
        {loading ? (
          <SkeletonGrid />
        ) : (
          <div className="grid grid-cols-2 gap-6 sm:grid-cols-3 lg:grid-cols-4">
            {nowShowing.map((m, i) => (
              <MovieCard key={m.id} movie={m} index={i} />
            ))}
          </div>
        )}
      </Section>

      <Section
        eyebrow="Premieres"
        title={STRINGS.home.comingSoon}
        subtitle={STRINGS.home.comingSoonSub}
      >
        {loading ? (
          <SkeletonGrid />
        ) : (
          <div className="grid grid-cols-2 gap-6 sm:grid-cols-3 lg:grid-cols-4">
            {comingSoon.map((m, i) => (
              <MovieCard key={m.id} movie={m} index={i} />
            ))}
          </div>
        )}
      </Section>

      <section className="mx-auto max-w-7xl px-4 pb-24 sm:px-6 lg:px-8">
        <div className="relative overflow-hidden rounded-3xl border border-accent/20 bg-card p-12 text-center shadow-elegant md:p-20">
          <div className="absolute inset-0 -z-10 bg-[radial-gradient(circle_at_top,_var(--primary)_0%,_transparent_50%)] opacity-30" />
          <h2 className="font-display text-5xl md:text-7xl">
            The lights are dimming.
          </h2>
          <p className="mx-auto mt-4 max-w-md text-muted-foreground">
            Don't be the one fumbling for tickets at the door.
          </p>
          <div className="mt-8 flex justify-center">
            <Link to={ROUTES.movies}>
              <HammerButton size="xl" variant="gold">
                Book your seat
                <ArrowRight className="h-5 w-5" />
              </HammerButton>
            </Link>
          </div>
        </div>
      </section>
    </>
  );
}

function SkeletonGrid() {
  return (
    <div className="grid grid-cols-2 gap-6 sm:grid-cols-3 lg:grid-cols-4">
      {Array.from({ length: 4 }).map((_, i) => (
        <div
          key={i}
          className="aspect-[2/3] animate-pulse rounded-2xl border border-border bg-card"
        />
      ))}
    </div>
  );
}

function Section({
  eyebrow,
  title,
  subtitle,
  children,
}: {
  eyebrow: string;
  title: string;
  subtitle: string;
  children: React.ReactNode;
}) {
  return (
    <section className="mx-auto max-w-7xl px-4 py-16 sm:px-6 lg:px-8">
      <div className="mb-10 flex flex-wrap items-end justify-between gap-4">
        <div>
          <div className="mb-2 text-xs uppercase tracking-widest text-accent">
            {eyebrow}
          </div>
          <h2 className="font-display text-4xl md:text-6xl">{title}</h2>
          <p className="mt-2 text-muted-foreground">{subtitle}</p>
        </div>
        <Link
          to={ROUTES.movies}
          className="group inline-flex items-center gap-2 text-sm text-muted-foreground hover:text-accent"
        >
          See all
          <ArrowRight className="h-4 w-4 transition-transform group-hover:translate-x-1" />
        </Link>
      </div>
      {children}
    </section>
  );
}
