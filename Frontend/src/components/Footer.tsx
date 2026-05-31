import { Film, Instagram, Twitter, Youtube } from "lucide-react";
import { STRINGS } from "@/constants/strings";

export function Footer() {
  return (
    <footer className="relative mt-32 border-t border-border bg-card/40">
      <div className="absolute inset-x-0 top-0 h-px bg-gradient-to-r from-transparent via-accent/60 to-transparent" />
      <div className="mx-auto grid max-w-7xl gap-12 px-4 py-16 sm:px-6 lg:grid-cols-4 lg:px-8">
        <div className="space-y-4">
          <div className="flex items-center gap-2">
            <span className="grid h-9 w-9 place-items-center rounded-full bg-gradient-primary">
              <Film className="h-4 w-4 text-primary-foreground" />
            </span>
            <span className="font-display text-xl tracking-widest">
              {STRINGS.app.name}
            </span>
          </div>
          <p className="max-w-xs text-sm text-muted-foreground">
            {STRINGS.app.tagline}
          </p>
          <div className="flex gap-2">
            {[Twitter, Instagram, Youtube].map((Icon, i) => (
              <a
                key={i}
                href="#"
                aria-label="Social"
                className="grid h-9 w-9 place-items-center rounded-full border border-border text-muted-foreground transition-all hover:-translate-y-0.5 hover:border-accent hover:text-accent"
              >
                <Icon className="h-4 w-4" />
              </a>
            ))}
          </div>
        </div>

        <FooterCol
          title={STRINGS.footer.sections.explore}
          items={["Now Showing", "Coming Soon", "Halls", "Gift Cards"]}
        />
        <FooterCol
          title={STRINGS.footer.sections.support}
          items={["Help Center", "Contact", "Refunds", "Accessibility"]}
        />
        <FooterCol
          title={STRINGS.footer.sections.legal}
          items={["Privacy", "Terms", "Cookies", "Imprint"]}
        />
      </div>
      <div className="border-t border-border">
        <div className="mx-auto flex max-w-7xl flex-col items-center justify-between gap-2 px-4 py-6 text-xs text-muted-foreground sm:flex-row sm:px-6 lg:px-8">
          <p>
            © {new Date().getFullYear()} {STRINGS.app.name}. {STRINGS.footer.rights}
          </p>
          <p>{STRINGS.footer.tagline}</p>
        </div>
      </div>
    </footer>
  );
}

function FooterCol({ title, items }: { title: string; items: string[] }) {
  return (
    <div>
      <h4 className="mb-4 font-display text-sm tracking-widest text-accent">
        {title}
      </h4>
      <ul className="space-y-2 text-sm text-muted-foreground">
        {items.map((it) => (
          <li key={it}>
            <a href="#" className="transition-colors hover:text-foreground">
              {it}
            </a>
          </li>
        ))}
      </ul>
    </div>
  );
}
