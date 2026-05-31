import { motion, type HTMLMotionProps } from "framer-motion";
import { Loader2 } from "lucide-react";
import { forwardRef } from "react";
import { cn } from "@/lib/utils";

type Variant = "primary" | "secondary" | "ghost" | "gold" | "outline";
type Size = "sm" | "md" | "lg" | "xl";

interface HammerButtonProps extends Omit<HTMLMotionProps<"button">, "ref"> {
  variant?: Variant;
  size?: Size;
  loading?: boolean;
}

const variants: Record<Variant, string> = {
  primary:
    "bg-gradient-primary text-primary-foreground shadow-glow hover:shadow-[0_0_60px_-8px_var(--primary)]",
  secondary:
    "bg-secondary text-secondary-foreground border border-border hover:bg-secondary/80",
  ghost: "bg-transparent text-foreground hover:bg-secondary/60",
  gold: "bg-gradient-gold text-gold-foreground shadow-gold font-semibold",
  outline:
    "bg-transparent border border-foreground/30 text-foreground hover:bg-foreground/5",
};

const sizes: Record<Size, string> = {
  sm: "h-9 px-4 text-sm gap-1.5",
  md: "h-11 px-6 text-sm gap-2",
  lg: "h-13 px-8 text-base gap-2.5",
  xl: "h-16 px-10 text-lg gap-3",
};

export const HammerButton = forwardRef<HTMLButtonElement, HammerButtonProps>(
  (
    {
      variant = "primary",
      size = "md",
      loading,
      className,
      children,
      disabled,
      ...rest
    },
    ref,
  ) => {
    return (
      <motion.button
        ref={ref}
        whileHover={{ scale: 1.03, y: -2 }}
        whileTap={{ scale: 0.92, y: 1 }}
        transition={{ type: "spring", stiffness: 400, damping: 17 }}
        disabled={disabled || loading}
        className={cn(
          "inline-flex items-center justify-center rounded-full font-medium tracking-wide",
          "focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 focus-visible:ring-offset-background",
          "disabled:opacity-50 disabled:cursor-not-allowed disabled:pointer-events-none",
          "transition-shadow duration-300",
          variants[variant],
          sizes[size],
          className,
        )}
        {...rest}
      >
        {loading ? <Loader2 className="h-4 w-4 animate-spin" /> : children}
      </motion.button>
    );
  },
);
HammerButton.displayName = "HammerButton";
