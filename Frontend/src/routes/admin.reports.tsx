import { createFileRoute } from "@tanstack/react-router";
import { useEffect, useState } from "react";
import { toast } from "sonner";
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
} from "@/lib/api";

export const Route = createFileRoute("/admin/reports")({ component: AdminReports });

function AdminReports() {
  return (
    <div>
      <div className="mb-6">
        <h1 className="font-display text-4xl tracking-wider">Reports</h1>
        <p className="text-sm text-muted-foreground">Revenue, capacity, and analytics</p>
      </div>

      <Tabs defaultValue="revenue" className="w-full">
        <TabsList className="mb-6">
          <TabsTrigger value="revenue">Revenue</TabsTrigger>
          <TabsTrigger value="capacity">Capacity</TabsTrigger>
          <TabsTrigger value="top">Top Movies</TabsTrigger>
        </TabsList>

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
      <CardHeader>
        <CardTitle className="font-display text-xl">Top Grossing Movies</CardTitle>
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
