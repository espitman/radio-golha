import { useEffect, useState } from "react";
import { BannerTracksPageSkeleton } from "../../components/skeleton/Skeletons";
import { TrackList, type TrackRowData } from "../../components/track/TrackRow";
import { readPopularTracks, RECENT_TRACKS_CHANGED_EVENT } from "../../lib/recentTracks";

function loadPopularRows(limit = 20): TrackRowData[] {
  return readPopularTracks(limit).map((track) => ({
    id: track.id,
    title: track.title,
    subtitle: track.subtitle,
    duration: track.duration,
    audioUrl: track.audioUrl,
    artworkUrls: track.artworkUrls,
  }));
}

export function PopularTracksPage() {
  const [tracks, setTracks] = useState<TrackRowData[] | null>(null);
  const [totalPlays, setTotalPlays] = useState(0);

  useEffect(() => {
    const refresh = () => {
      const popularTracks = readPopularTracks(20);
      setTracks(loadPopularRows(20));
      setTotalPlays(popularTracks.reduce((sum, track) => sum + (track.playCount || 0), 0));
    };
    refresh();
    window.addEventListener(RECENT_TRACKS_CHANGED_EVENT, refresh);
    window.addEventListener("storage", refresh);
    return () => {
      window.removeEventListener(RECENT_TRACKS_CHANGED_EVENT, refresh);
      window.removeEventListener("storage", refresh);
    };
  }, []);

  if (!tracks) return <BannerTracksPageSkeleton titleWidth="w-72" />;

  return (
    <main className="pb-[144px] pt-12">
      <div className="mx-auto max-w-5xl px-12">
        <section className="hero-pattern relative flex h-[260px] w-full items-end justify-center overflow-hidden rounded-3xl bg-primary">
          <div className="absolute inset-0 bg-[radial-gradient(circle_at_top_right,rgba(210,167,56,0.25),transparent_35%),linear-gradient(135deg,rgba(9,47,83,0.95),rgba(6,32,58,0.98))]" />
          <div className="relative z-10 w-full px-12 pb-12 text-right">
            <div className="mb-4 inline-block rounded-full bg-secondary/80 px-5 py-1.5 text-[10px] font-black uppercase tracking-widest text-white backdrop-blur-md">
              بیشترین پخش
            </div>
            <h1 className="mb-4 text-5xl font-bold leading-tight tracking-tight text-white">محبوب‌ترین برنامه‌ها</h1>
            <div className="flex items-center justify-start gap-2 text-white/70">
              <span className="material-symbols-outlined text-lg text-secondary">equalizer</span>
              <span className="text-base font-bold">{totalPlays.toLocaleString("fa-IR")} بار پخش</span>
            </div>
          </div>
        </section>
      </div>

      <section className="mx-auto max-w-5xl px-12 py-12">
        <div className="mb-8 flex items-center justify-between border-b border-surface-variant pb-4">
          <h2 className="text-2xl font-black text-primary">فهرست برنامه‌ها</h2>
        </div>

        {tracks.length > 0 ? (
          <TrackList tracks={tracks} linkMode="none" />
        ) : (
          <div className="rounded-3xl border border-outline-variant/20 bg-surface-container-lowest px-8 py-12 text-center text-sm font-bold text-on-surface-variant">
            هنوز برنامه‌ای پخش نشده است.
          </div>
        )}
      </section>
    </main>
  );
}
