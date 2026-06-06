import { createFileRoute, Link, useNavigate } from "@tanstack/react-router";
import { motion } from "framer-motion";
import { Award, BookMarked, LogOut, Mail, Ticket, User as UserIcon, Save, Phone, MapPin, Camera } from "lucide-react";
import { useEffect, useState } from "react";
import { HammerButton } from "@/components/HammerButton";
import { useAuth } from "@/hooks/use-auth";
import { ROUTES } from "@/constants/routes";
import { toast } from "sonner";
import { users, type UserDto, ApiError } from "@/lib/api";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Button } from "@/components/ui/button";
import { signIn } from "@/hooks/use-auth";

export const Route = createFileRoute("/profile")({
  head: () => ({
    meta: [
      { title: "My Profile — CineReserve" },
      { name: "description", content: "Manage your CineReserve profile and preferences." },
    ],
  }),
  component: ProfilePage,
});

function initials(name: string) {
  return name
    .split(" ")
    .map((s) => s[0])
    .filter(Boolean)
    .slice(0, 2)
    .join("")
    .toUpperCase();
}

function ProfilePage() {
  const { user, hydrated, logout } = useAuth();
  const navigate = useNavigate();
  const [profile, setProfile] = useState<UserDto | null>(null);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [editing, setEditing] = useState(false);
  const [form, setForm] = useState({ name: "", phone: "", address: "", profilePictureUrl: "" });

  useEffect(() => {
    if (hydrated && !user) navigate({ to: ROUTES.login });
  }, [hydrated, user, navigate]);

  useEffect(() => {
    if (user) {
      users.me().then((data) => {
        setProfile(data);
        setForm({
          name: data.name || "",
          phone: data.phone || "",
          address: data.address || "",
          profilePictureUrl: data.profilePictureUrl || "",
        });
      }).catch(() => {
        toast.error("Failed to load profile");
      }).finally(() => setLoading(false));
    }
  }, [user]);

  if (!user) return null;

  const handleLogout = () => {
    logout();
    toast.success("Signed out");
    navigate({ to: ROUTES.home });
  };

  const handleSave = async () => {
    setSaving(true);
    try {
      const updated = await users.updateProfile({
        name: form.name || undefined,
        phone: form.phone || undefined,
        address: form.address || undefined,
        profilePictureUrl: form.profilePictureUrl || undefined,
      });
      setProfile(updated);
      setEditing(false);
      toast.success("Profile updated");
      if (updated.name !== user.name) {
        signIn({ ...user, name: updated.name }, undefined);
      }
    } catch (e) {
      toast.error(e instanceof ApiError ? e.message : "Failed to update profile");
    } finally {
      setSaving(false);
    }
  };

  const displayName = profile?.name || user.name;
  const avatarUrl = profile?.profilePictureUrl;

  return (
    <div className="relative min-h-[calc(100dvh-4rem)] overflow-hidden px-4 py-16">
      <div className="pointer-events-none absolute inset-0">
        <div className="absolute -top-40 left-1/2 h-[500px] w-[500px] -translate-x-1/2 rounded-full bg-primary/10 blur-[120px]" />
      </div>

      <motion.div
        initial={{ opacity: 0, y: 24 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ duration: 0.5, ease: "easeOut" }}
        className="relative z-10 mx-auto max-w-2xl"
      >
        <div className="rounded-2xl border border-border bg-card/80 p-8 shadow-elegant backdrop-blur-xl">
          <div className="flex items-center gap-5">
            {avatarUrl ? (
              <img
                src={avatarUrl}
                alt={displayName}
                className="h-20 w-20 rounded-full object-cover shadow-glow"
              />
            ) : (
              <div className="grid h-20 w-20 place-items-center rounded-full bg-gradient-primary text-2xl font-bold text-primary-foreground shadow-glow">
                {initials(displayName)}
              </div>
            )}
            <div className="min-w-0 flex-1">
              <h1 className="font-display text-3xl tracking-wider text-foreground">
                {displayName}
              </h1>
              <p className="mt-1 flex items-center gap-2 text-sm text-muted-foreground">
                <Mail className="h-3.5 w-3.5" />
                <span className="truncate">{user.email}</span>
              </p>
              {profile?.phone && !editing && (
                <p className="mt-1 flex items-center gap-2 text-sm text-muted-foreground">
                  <Phone className="h-3.5 w-3.5" />
                  <span>{profile.phone}</span>
                </p>
              )}
            </div>
          </div>

          {profile && !loading && (
            <>
              {editing ? (
                <div className="mt-6 space-y-4">
                  <div>
                    <Label htmlFor="name">Name</Label>
                    <Input
                      id="name"
                      value={form.name}
                      onChange={(e) => setForm({ ...form, name: e.target.value })}
                      placeholder="Your name"
                    />
                  </div>
                  <div>
                    <Label htmlFor="phone">Phone</Label>
                    <Input
                      id="phone"
                      value={form.phone}
                      onChange={(e) => setForm({ ...form, phone: e.target.value })}
                      placeholder="Phone number"
                    />
                  </div>
                  <div>
                    <Label htmlFor="address">Address</Label>
                    <Input
                      id="address"
                      value={form.address}
                      onChange={(e) => setForm({ ...form, address: e.target.value })}
                      placeholder="Your address"
                    />
                  </div>
                  <div>
                    <Label htmlFor="profilePictureUrl">Profile Picture URL</Label>
                    <Input
                      id="profilePictureUrl"
                      value={form.profilePictureUrl}
                      onChange={(e) => setForm({ ...form, profilePictureUrl: e.target.value })}
                      placeholder="https://example.com/avatar.jpg"
                    />
                  </div>
                  <div className="flex gap-2">
                    <Button onClick={handleSave} disabled={saving}>
                      <Save className="mr-2 h-4 w-4" />
                      {saving ? "Saving..." : "Save"}
                    </Button>
                    <Button variant="ghost" onClick={() => setEditing(false)} disabled={saving}>
                      Cancel
                    </Button>
                  </div>
                </div>
              ) : (
                <div className="mt-6">
                  {profile.address && (
                    <p className="flex items-center gap-2 text-sm text-muted-foreground">
                      <MapPin className="h-3.5 w-3.5" />
                      <span>{profile.address}</span>
                    </p>
                  )}
                  <Button
                    variant="outline"
                    size="sm"
                    className="mt-4"
                    onClick={() => setEditing(true)}
                  >
                    <UserIcon className="mr-2 h-4 w-4" />
                    Edit Profile
                  </Button>
                </div>
              )}
            </>
          )}

          {loading && (
            <div className="mt-6 text-center text-sm text-muted-foreground">Loading profile...</div>
          )}

          <div className="mt-8 grid gap-3 sm:grid-cols-2">
            <Link
              to={ROUTES.myBookings}
              className="group flex items-center gap-3 rounded-xl border border-border bg-secondary/40 p-4 transition-colors hover:bg-secondary"
            >
              <div className="grid h-10 w-10 place-items-center rounded-lg bg-primary/15 text-primary">
                <Ticket className="h-5 w-5" />
              </div>
              <div>
                <div className="text-sm font-medium text-foreground">My Bookings</div>
                <div className="text-xs text-muted-foreground">View past & upcoming</div>
              </div>
            </Link>

            <Link
              to="/loyalty"
              className="group flex items-center gap-3 rounded-xl border border-border bg-secondary/40 p-4 transition-colors hover:bg-secondary"
            >
              <div className="grid h-10 w-10 place-items-center rounded-lg bg-gold/15 text-gold">
                <Award className="h-5 w-5" />
              </div>
              <div>
                <div className="text-sm font-medium text-foreground">
                  Loyalty Points
                  <span className="ml-2 rounded-full bg-accent/20 px-2 py-0.5 text-xs text-accent">
                    {profile?.loyaltyPoints ?? 0}
                  </span>
                </div>
                <div className="text-xs text-muted-foreground">Earn & redeem rewards</div>
              </div>
            </Link>

            <Link
              to="/watchlist"
              className="group flex items-center gap-3 rounded-xl border border-border bg-secondary/40 p-4 transition-colors hover:bg-secondary"
            >
              <div className="grid h-10 w-10 place-items-center rounded-lg bg-primary/15 text-primary">
                <BookMarked className="h-5 w-5" />
              </div>
              <div>
                <div className="text-sm font-medium text-foreground">My Watchlist</div>
                <div className="text-xs text-muted-foreground">Saved movies</div>
              </div>
            </Link>
          </div>

          <div className="mt-8 flex justify-end">
            <HammerButton variant="ghost" onClick={handleLogout}>
              <LogOut className="h-4 w-4" />
              Sign Out
            </HammerButton>
          </div>
        </div>
      </motion.div>
    </div>
  );
}
