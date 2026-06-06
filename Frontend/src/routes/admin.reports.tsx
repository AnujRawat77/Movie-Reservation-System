import { createFileRoute } from "@tanstack/react-router";
import { useEffect, useState } from "react";
import { toast } from "sonner";
import { Download } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { Progress } from "@/components/ui/progress";
import {
  Table, TableBody, TableCell, TableHead, TableHeader, TableRow,
} from "@/components/ui/table";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs";
import {
  admin, ApiError,
  type RevenueReport, type CapacityReport, type TopMovie,
  type DashboardSummary, type DailySale, type GenreRevenue, type HallStats,
} from "@/lib/api";

export const Route = createFileRoute("/admin/reports")({ component: AdminReports });

function triggerDownload(content: string, filename: string, mime: string) {
  const blob = new Blob([content], { type: mime });
  const url = URL.createObjectURL(blob);
  const a = document.createElement("a");
  a.href = url;
  a.download = filename;
  document.body.appendChild(a);
  a.click();
  document.body.removeChild(a);
  URL.revokeObjectURL(url);
}

function AdminReports() {
  return (
    <div>
      <div className="mb-6">
        <h1 className="font-display text-4xl tracking-wider">Reports</h1>
        <p className="text-sm text-muted-foreground">Revenue, capacity, and analytics</p>
      </div>

      <Tabs defaultValue="dashboard" className="w-full">
        <TabsList className="mb-6">
          <TabsTrigger value="dashboard">Dashboard</TabsTrigger>
          <TabsTrigger value="revenue">Revenue</TabsTrigger>
          <TabsTrigger value="capacity">Capacity</TabsTrigger>
          <TabsTrigger value="top">Top Movies</TabsTrigger>
        </TabsList>

        <TabsContent value="dashboard"><DashboardTab /></TabsContent>
        <TabsContent value="revenue"><RevenueTab /></TabsContent>
        <TabsContent value="capacity"><CapacityTab /></TabsContent>
        <TabsContent value="top"><TopMoviesTab /></TabsContent>
      </Tabs>
    </div>
  );
}

function RevenueTab() {
  const [from, setFrom] = useState("");
  const [to, setTo] = useState("");
  const [data, setData] = useState<RevenueReport | null>(null);
  const [loading, setLoading] = useState(false);

  const load = async () => {
    setLoading(true);
    try {
      const params: Record<string, string> = {};
      if (from) params.from = from;
      if (to) params.to = to;
      setData(await admin.reports.revenue(params));
    } catch (e) { toast.error(e instanceof ApiError ? e.message : "Failed"); }
    finally { setLoading(false); }
  };

  useEffect(() => { load(); }, []);

  return (
    <Card className="border-border bg-card/60">
      <CardHeader>
        <CardTitle className="font-display text-xl">Revenue Report</CardTitle>
        <div className="flex flex-wrap gap-2 pt-2">
          <Input type="date" value={from} onChange={(e) => setFrom(e.target.value)} className="w-40" placeholder="From" />
          <Input type="date" value={to} onChange={(e) => setTo(e.target.value)} className="w-40" placeholder="To" />
          <Button onClick={load} disabled={loading}>{loading ? "Loading…" : "Fetch"}</Button>
          {data && (
            <Button
              variant="outline"
              size="sm"
              onClick={() => {
                const rows = [
                  ["Movie", "Revenue"],
                  ...(data.byMovie ?? []).map((r) => [r.movieTitle, Number(r.revenue).toFixed(2)]),
                  ["", ""],
                  ["Total Revenue", Number(data.totalRevenue).toFixed(2)],
                ];
                triggerDownload(rows.map((r) => r.join(",")).join("\n"), "revenue-report.csv", "text/csv");
              }}
            >
              <Download className="mr-1 h-4 w-4" /> Download CSV
            </Button>
          )}
        </div>
      </CardHeader>
      <CardContent>
        {data && (
          <>
            <div className="mb-6 font-display text-4xl text-gradient-gold">
              ${Number(data.totalRevenue).toFixed(2)}
            </div>
            {data.byMovie && data.byMovie.length > 0 && (
              <Table>
                <TableHeader>
                  <TableRow>
                    <TableHead>Movie</TableHead>
                    <TableHead className="text-right">Revenue</TableHead>
                  </TableRow>
                </TableHeader>
                <TableBody>
                  {data.byMovie.map((row, i) => (
                    <TableRow key={i}>
                      <TableCell className="font-medium">{row.movieTitle}</TableCell>
                      <TableCell className="text-right text-accent">${Number(row.revenue).toFixed(2)}</TableCell>
                    </TableRow>
                  ))}
                </TableBody>
              </Table>
            )}
          </>
        )}
      </CardContent>
    </Card>
  );
}

function CapacityTab() {
  const [showtimeId, setShowtimeId] = useState("");
  const [data, setData] = useState<CapacityReport | null>(null);
  const [loading, setLoading] = useState(false);

  const load = async () => {
    if (!showtimeId) { toast.warning("Enter a showtime ID"); return; }
    setLoading(true);
    try { setData(await admin.reports.capacity(Number(showtimeId))); }
    catch (e) { toast.error(e instanceof ApiError ? e.message : "Failed"); }
    finally { setLoading(false); }
  };

  return (
    <Card className="border-border bg-card/60">
      <CardHeader>
        <CardTitle className="font-display text-xl">Capacity Report</CardTitle>
        <div className="flex gap-2 pt-2">
          <Input value={showtimeId} onChange={(e) => setShowtimeId(e.target.value)} className="w-40" placeholder="Showtime ID" />
          <Button onClick={load} disabled={loading}>{loading ? "Loading…" : "Check"}</Button>
          {data && (
            <Button
              variant="outline"
              size="sm"
              onClick={() => {
                const rows = [
                  ["Field", "Value"],
                  ["Movie", data.movieTitle],
                  ["Hall", data.hallName],
                  ["Start Time", data.startTime],
                  ["Total Seats", String(data.totalSeats)],
                  ["Booked Seats", String(data.bookedSeats)],
                  ["Available Seats", String(data.availableSeats)],
                  ["Occupancy %", data.occupancyPercent.toFixed(1)],
                ];
                triggerDownload(rows.map((r) => r.join(",")).join("\n"), `capacity-showtime-${data.showtimeId}.csv`, "text/csv");
              }}
            >
              <Download className="mr-1 h-4 w-4" /> Download CSV
            </Button>
          )}
        </div>
      </CardHeader>
      <CardContent>
        {data && (
          <div className="space-y-4">
            <div className="text-sm text-muted-foreground">
              {data.movieTitle} — {data.hallName}
            </div>
            <Progress value={data.occupancyPercent} className="h-4" />
            <div className="flex flex-wrap gap-6 text-sm">
              <span>Total: <strong>{data.totalSeats}</strong></span>
              <span>Booked: <strong className="text-accent">{data.bookedSeats}</strong></span>
              <span>Available: <strong>{data.availableSeats}</strong></span>
              <Badge>{data.occupancyPercent.toFixed(1)}% occupied</Badge>
            </div>
          </div>
        )}
      </CardContent>
    </Card>
  );
}

function DashboardTab() {
  const [summary, setSummary] = useState<DashboardSummary | null>(null);
  const [dailySales, setDailySales] = useState<DailySale[]>([]);
  const [genreRevenue, setGenreRevenue] = useState<GenreRevenue[]>([]);
  const [hallStats, setHallStats] = useState<HallStats[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    (async () => {
      try {
        const [s, d, g, h] = await Promise.all([
          admin.reports.dashboard(),
          admin.reports.dailySales(30),
          admin.reports.revenueByGenre(),
          admin.reports.topHalls(),
        ]);
        setSummary(s);
        setDailySales(d);
        setGenreRevenue(g);
        setHallStats(h);
      } catch (e) {
        toast.error(e instanceof ApiError ? e.message : "Failed to load dashboard");
      } finally {
        setLoading(false);
      }
    })();
  }, []);

  if (loading) return <div className="py-8 text-center text-muted-foreground">Loading dashboard…</div>;

  return (
    <div className="space-y-6">
      {summary && (
        <div className="grid gap-4 sm:grid-cols-3">
          <Card className="border-border bg-card/60">
            <CardHeader className="pb-2"><CardTitle className="text-sm text-muted-foreground">Total Bookings</CardTitle></CardHeader>
            <CardContent><p className="font-display text-3xl">{summary.totalBookings}</p></CardContent>
          </Card>
          <Card className="border-border bg-card/60">
            <CardHeader className="pb-2"><CardTitle className="text-sm text-muted-foreground">Cancellations</CardTitle></CardHeader>
            <CardContent><p className="font-display text-3xl text-destructive">{summary.totalCancellations}</p></CardContent>
          </Card>
          <Card className="border-border bg-card/60">
            <CardHeader className="pb-2"><CardTitle className="text-sm text-muted-foreground">Total Revenue</CardTitle></CardHeader>
            <CardContent><p className="font-display text-3xl text-gradient-gold">${Number(summary.totalRevenue).toFixed(2)}</p></CardContent>
          </Card>
        </div>
      )}

      {dailySales.length > 0 && (
        <Card className="border-border bg-card/60">
          <CardHeader className="flex flex-row items-start justify-between">
            <CardTitle className="font-display text-xl">Daily Sales (Last 30 Days)</CardTitle>
            <Button
              variant="outline"
              size="sm"
              onClick={() => {
                const rows = [["Date", "Revenue", "Bookings"], ...dailySales.map((d) => [d.date, Number(d.revenue).toFixed(2), String(d.bookings)])];
                triggerDownload(rows.map((r) => r.join(",")).join("\n"), "daily-sales.csv", "text/csv");
              }}
            >
              <Download className="mr-1 h-4 w-4" /> CSV
            </Button>
          </CardHeader>
          <CardContent>
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>Date</TableHead>
                  <TableHead className="text-right">Revenue</TableHead>
                  <TableHead className="text-right">Bookings</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {dailySales.map((d) => (
                  <TableRow key={d.date}>
                    <TableCell>{d.date}</TableCell>
                    <TableCell className="text-right text-accent">${Number(d.revenue).toFixed(2)}</TableCell>
                    <TableCell className="text-right">{d.bookings}</TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </CardContent>
        </Card>
      )}

      {genreRevenue.length > 0 && (
        <Card className="border-border bg-card/60">
          <CardHeader className="flex flex-row items-start justify-between">
            <CardTitle className="font-display text-xl">Revenue by Genre</CardTitle>
            <Button
              variant="outline"
              size="sm"
              onClick={() => {
                const rows = [["Genre", "Revenue"], ...genreRevenue.map((g) => [g.genre, Number(g.revenue).toFixed(2)])];
                triggerDownload(rows.map((r) => r.join(",")).join("\n"), "revenue-by-genre.csv", "text/csv");
              }}
            >
              <Download className="mr-1 h-4 w-4" /> CSV
            </Button>
          </CardHeader>
          <CardContent>
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>Genre</TableHead>
                  <TableHead className="text-right">Revenue</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {genreRevenue.map((g) => (
                  <TableRow key={g.genre}>
                    <TableCell className="font-medium">{g.genre}</TableCell>
                    <TableCell className="text-right text-accent">${Number(g.revenue).toFixed(2)}</TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </CardContent>
        </Card>
      )}

      {hallStats.length > 0 && (
        <Card className="border-border bg-card/60">
          <CardHeader className="flex flex-row items-start justify-between">
            <CardTitle className="font-display text-xl">Top Halls</CardTitle>
            <Button
              variant="outline"
              size="sm"
              onClick={() => {
                const rows = [["Hall", "Showtimes"], ...hallStats.map((h) => [h.hallName, String(h.showtimeCount)])];
                triggerDownload(rows.map((r) => r.join(",")).join("\n"), "top-halls.csv", "text/csv");
              }}
            >
              <Download className="mr-1 h-4 w-4" /> CSV
            </Button>
          </CardHeader>
          <CardContent>
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>Hall</TableHead>
                  <TableHead className="text-right">Showtimes</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {hallStats.map((h) => (
                  <TableRow key={h.hallName}>
                    <TableCell className="font-medium">{h.hallName}</TableCell>
                    <TableCell className="text-right">{h.showtimeCount}</TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </CardContent>
        </Card>
      )}
    </div>
  );
}

function TopMoviesTab() {
  const [data, setData] = useState<TopMovie[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    (async () => {
      try { setData(await admin.reports.topMovies()); }
      finally { setLoading(false); }
    })();
  }, []);

  return (
    <Card className="border-border bg-card/60">
      <CardHeader className="flex flex-row items-start justify-between">
        <CardTitle className="font-display text-xl">Top Grossing Movies</CardTitle>
        {data.length > 0 && (
          <Button
            variant="outline"
            size="sm"
            onClick={() => {
              const rows = [
                ["Rank", "Movie", "Revenue"],
                ...data.map((m, i) => [String(i + 1), m.title, Number(m.revenue).toFixed(2)]),
              ];
              triggerDownload(rows.map((r) => r.join(",")).join("\n"), "top-movies-report.csv", "text/csv");
            }}
          >
            <Download className="mr-1 h-4 w-4" /> Download CSV
          </Button>
        )}
      </CardHeader>
      <CardContent>
        {loading ? (
          <div className="py-8 text-center text-muted-foreground">Loading…</div>
        ) : data.length === 0 ? (
          <div className="py-8 text-center text-muted-foreground">No revenue data yet</div>
        ) : (
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead>#</TableHead>
                <TableHead>Movie</TableHead>
                <TableHead className="text-right">Revenue</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {data.map((m, i) => (
                <TableRow key={m.movieId}>
                  <TableCell className="text-muted-foreground">{i + 1}</TableCell>
                  <TableCell className="font-medium">{m.title}</TableCell>
                  <TableCell className="text-right text-accent">${Number(m.revenue).toFixed(2)}</TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        )}
      </CardContent>
    </Card>
  );
}
