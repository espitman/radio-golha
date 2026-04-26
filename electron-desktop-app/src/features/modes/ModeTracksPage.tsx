import { useParams } from "@tanstack/react-router";
import { useEffect, useState } from "react";
import { TrackList, type TrackRowData } from "../../components/track/TrackRow";
import { getModes, searchPrograms, type CoreMode, type CoreProgramListItem } from "../../lib/coreApi";

function toTrackRow(row: CoreProgramListItem): TrackRowData {
  return {
    id: row.id,
    title: row.title,
    subtitle: row.artist || row.categoryName,
    duration: row.duration || "نامشخص",
    audioUrl: row.audioUrl,
  };
}

export function ModeTracksPage() {
  const { modeId } = useParams({ strict: false }) as { modeId?: string };
  const numericModeId = Number(modeId);
  const [mode, setMode] = useState<CoreMode | null>(null);
  const [tracks, setTracks] = useState<TrackRowData[] | null>(null);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (!Number.isFinite(numericModeId)) {
      setError("شناسه دستگاه معتبر نیست.");
      return;
    }

    let isMounted = true;
    setError(null);
    setTracks(null);

    Promise.all([
      getModes(),
      searchPrograms({ modeIds: [numericModeId], modeMatch: "any", page: 1 }),
    ])
      .then(([modes, result]) => {
        if (!isMounted) return;
        setMode(modes.find((item) => item.id === numericModeId) ?? null);
        setTracks(result.rows.map(toTrackRow));
      })
      .catch((reason) => {
        if (isMounted) setError(String(reason));
      });

    return () => {
      isMounted = false;
    };
  }, [numericModeId]);

  if (error) return <div className="mx-auto max-w-5xl px-12 py-12 text-right text-sm font-bold text-on-error-container">{error}</div>;
  if (!tracks) return <div className="mx-auto max-w-5xl px-12 py-12 text-right text-sm font-bold text-on-surface-variant">در حال بارگذاری...</div>;

  const title = mode?.name ?? "دستگاه / آواز";

  return (
    <main className="pb-[144px] pt-12">
      <div className="mx-auto max-w-5xl px-12">
        <section className="hero-pattern relative flex h-[260px] w-full items-end justify-center overflow-hidden rounded-3xl bg-primary">
          <div className="absolute inset-0 bg-[radial-gradient(circle_at_top_right,rgba(210,167,56,0.25),transparent_35%),linear-gradient(135deg,rgba(9,47,83,0.95),rgba(6,32,58,0.98))]" />
          <div className="relative z-10 w-full px-12 pb-12 text-right">
            <div className="mb-4 inline-block rounded-full bg-secondary/80 px-5 py-1.5 text-[10px] font-black uppercase tracking-widest text-white backdrop-blur-md">
              دستگاه / آواز
            </div>
            <h1 className="mb-4 text-5xl font-bold leading-tight tracking-tight text-white">{title}</h1>
            <div className="flex items-center justify-start gap-2 text-white/70">
              <span className="material-symbols-outlined text-lg text-secondary">music_note</span>
              <span className="text-base font-bold">{tracks.length.toLocaleString("fa-IR")} برنامه</span>
            </div>
          </div>
        </section>
      </div>

      <section className="mx-auto max-w-5xl px-12 py-12">
        <div className="mb-8 flex items-center justify-between border-b border-surface-variant pb-4">
          <h2 className="text-2xl font-black text-primary">فهرست قطعات</h2>
        </div>

        {tracks.length > 0 ? (
          <TrackList tracks={tracks} linkMode="row" />
        ) : (
          <div className="rounded-3xl border border-outline-variant/20 bg-surface-container-lowest px-8 py-12 text-center text-sm font-bold text-on-surface-variant">
            قطعه‌ای برای این دستگاه پیدا نشد.
          </div>
        )}
      </section>
    </main>
  );
}
