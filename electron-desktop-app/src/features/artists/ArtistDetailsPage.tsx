import { useParams } from "@tanstack/react-router";
import { useEffect, useState } from "react";
import { TrackList } from "../../components/track/TrackRow";
import { getArtistDetail, type CoreArtistDetail } from "../../lib/coreApi";

export function ArtistDetailsPage() {
  const { artistId } = useParams({ strict: false }) as { artistId?: string };
  const numericArtistId = Number(artistId);
  const [artist, setArtist] = useState<CoreArtistDetail | null>(null);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (!Number.isFinite(numericArtistId)) {
      setError("شناسه هنرمند معتبر نیست.");
      return;
    }

    let isMounted = true;
    getArtistDetail(numericArtistId)
      .then((payload) => {
        if (isMounted) setArtist(payload);
      })
      .catch((reason) => {
        if (isMounted) setError(String(reason));
      });
    return () => {
      isMounted = false;
    };
  }, [numericArtistId]);

  if (error) return <div className="mx-auto max-w-5xl px-12 py-12 text-right text-sm font-bold text-on-error-container">{error}</div>;
  if (!artist) return <div className="mx-auto max-w-5xl px-12 py-12 text-right text-sm font-bold text-on-surface-variant">در حال بارگذاری...</div>;

  const tracks = artist.tracks.map((track) => ({
    id: track.id,
    title: track.title,
    subtitle: track.artist,
    duration: track.duration,
  }));

  return (
    <div className="mx-auto max-w-5xl px-12 pb-32 pt-12 text-right">
      <section className="grid grid-cols-12 items-end gap-10 pb-12">
        <div className="group relative col-span-4 aspect-square overflow-hidden rounded-lg bg-surface-container-high">
          {artist.avatar ? (
            <img alt={artist.name} className="h-full w-full object-cover grayscale transition-all duration-700 group-hover:grayscale-0" src={artist.avatar} />
          ) : (
            <div className="grid h-full w-full place-items-center text-primary/50">
              <span className="material-symbols-outlined text-7xl">person</span>
            </div>
          )}
          <div className="absolute inset-0 bg-gradient-to-t from-primary/80 to-transparent" />
        </div>

        <div className="col-span-8 flex h-full flex-col justify-end pb-8">
          <div className="mb-10 max-w-2xl">
            <h1 className="mb-3 text-4xl font-bold text-primary">{artist.name}</h1>
            {artist.instrument ? <p className="mb-4 text-sm font-bold text-on-surface-variant">{artist.instrument}</p> : null}
            <button className="inline-flex items-center gap-2 rounded-full bg-secondary px-6 py-2 text-sm font-black text-white transition-all hover:bg-on-secondary-container">
              <span className="material-symbols-outlined text-lg">favorite</span>
              <span>افزودن به علاقه‌مندی‌ها</span>
            </button>
          </div>

          <div className="flex justify-start gap-8">
            {artist.categoryCounts.slice(0, 4).map((stat) => (
              <div key={stat.categoryId} className="border-r-2 border-secondary/30 pr-4 first:border-r-0 first:pr-0">
                <p className="text-3xl font-black tracking-[-0.04em] text-primary">{stat.count.toLocaleString("fa-IR")}</p>
                <p className="mt-1 text-xs font-bold text-on-surface-variant">{stat.title}</p>
              </div>
            ))}
            <div className="border-r-2 border-secondary/30 pr-4">
              <p className="text-3xl font-black tracking-[-0.04em] text-primary">{artist.trackCount.toLocaleString("fa-IR")}</p>
              <p className="mt-1 text-xs font-bold text-on-surface-variant">کل برنامه‌ها</p>
            </div>
          </div>
        </div>
      </section>

      <section className="py-12">
        <div className="mb-6 flex items-end justify-between">
          <h2 className="text-2xl font-bold text-primary">برنامه‌ها</h2>
          <span className="text-xs font-bold text-secondary">{tracks.length.toLocaleString("fa-IR")} برنامه</span>
        </div>
        <TrackList tracks={tracks} linkMode="title" />
      </section>
    </div>
  );
}
