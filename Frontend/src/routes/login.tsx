import { useState, type FormEvent } from "react";
import { createFileRoute, Link, useNavigate } from "@tanstack/react-router";
import { motion } from "framer-motion";
import { Mail, Lock, ArrowRight, Clapperboard } from "lucide-react";
import { HammerButton } from "@/components/HammerButton";
import { STRINGS } from "@/constants/strings";
import { Input } from "@/components/ui/input";
import { toast } from "sonner";
import { signIn } from "@/hooks/use-auth";
import { auth, ApiError } from "@/lib/api";

export const Route = createFileRoute("/login")({
  head: () => ({
    meta: [
      { title: "Sign In — CineReserve" },
      { name: "description", content: "Sign in to your CineReserve account." },
      { property: "og:title", content: "Sign In — CineReserve" },
      { property: "og:description", content: "Sign in to your CineReserve account." },
    ],
  }),
  component: LoginPage,
});

function LoginPage() {
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [isLoading, setIsLoading] = useState(false);
  const navigate = useNavigate();

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault();
    setIsLoading(true);
    try {
      const result = await auth.login(email, password);
      signIn(
        {
          userId: result.userId,
          email: result.email,
          name: result.name,
          role: result.role,
        },
        result.token,
      );
      toast.success("Signed in successfully");
      await navigate({ to: "/" });
    } catch (err) {
      console.error("[Login] error:", err);
      const message =
        err instanceof ApiError
          ? err.message
          : err instanceof Error
            ? err.message
            : "Sign in failed";
      toast.error(message);
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="relative flex min-h-[calc(100dvh-4rem)] items-center justify-center overflow-hidden px-4">
      <div className="pointer-events-none absolute inset-0">
        <div className="absolute -top-40 left-1/2 h-[500px] w-[500px] -translate-x-1/2 rounded-full bg-primary/10 blur-[120px]" />
        <div className="absolute bottom-0 right-0 h-[300px] w-[300px] bg-gold/5 blur-[100px]" />
      </div>

      <motion.div
        initial={{ opacity: 0, y: 24 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ duration: 0.5, ease: "easeOut" }}
        className="relative z-10 w-full max-w-sm"
      >
        <div className="rounded-2xl border border-border bg-card/80 p-8 shadow-elegant backdrop-blur-xl">
          <div className="mb-8 text-center">
            <motion.div
              initial={{ scale: 0.8, opacity: 0 }}
              animate={{ scale: 1, opacity: 1 }}
              transition={{ delay: 0.1 }}
              className="mx-auto mb-4 grid h-14 w-14 place-items-center rounded-2xl bg-gradient-primary shadow-glow"
            >
              <Clapperboard className="h-7 w-7 text-primary-foreground" />
            </motion.div>
            <h1 className="font-display text-3xl tracking-wider text-foreground">
              {STRINGS.nav.login}
            </h1>
            <p className="mt-2 text-sm text-muted-foreground">
              Welcome back to the show
            </p>
          </div>

          <form onSubmit={handleSubmit} className="space-y-4">
            <div className="space-y-2">
              <label className="text-xs font-medium text-muted-foreground">
                Email
              </label>
              <div className="relative">
                <Mail className="absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground" />
                <Input
                  type="email"
                  placeholder="you@example.com"
                  value={email}
                  onChange={(e) => setEmail(e.target.value)}
                  required
                  className="h-11 rounded-xl border-border bg-secondary/40 pl-10 text-sm placeholder:text-muted-foreground/60 focus-visible:ring-primary"
                />
              </div>
            </div>

            <div className="space-y-2">
              <div className="flex items-center justify-between">
                <label className="text-xs font-medium text-muted-foreground">
                  Password
                </label>
              </div>
              <div className="relative">
                <Lock className="absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground" />
                <Input
                  type="password"
                  placeholder="••••••••"
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  required
                  className="h-11 rounded-xl border-border bg-secondary/40 pl-10 text-sm placeholder:text-muted-foreground/60 focus-visible:ring-primary"
                />
              </div>
            </div>

            <HammerButton
              type="submit"
              size="lg"
              className="w-full"
              loading={isLoading}
            >
              {STRINGS.nav.login}
              <ArrowRight className="h-4 w-4" />
            </HammerButton>
          </form>

          <p className="mt-6 text-center text-sm text-muted-foreground">
            New here?{" "}
            <Link
              to="/signup"
              className="font-medium text-primary hover:underline"
            >
              {STRINGS.nav.signup}
            </Link>
          </p>
        </div>
      </motion.div>
    </div>
  );
}
