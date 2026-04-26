import { useParams } from "@tanstack/react-router";
import { useEffect, useMemo, useState } from "react";
import { ArtistCard } from "../../components/artist/ArtistCard";
import { getTrackDetail, type CoreTimelineSegment, type CoreTrackDetail } from "../../lib/coreApi";

function MetaBlock({ label, value, bordered = false }: { label: string; value: string; bordered?: boolean }) {
  return (
    <div className={bordered ? "flex flex-1 flex-col items-center border-x border-surface-container-highest px-8" : "flex flex-1 flex-col items-center"}>
      <span className="mb-1 text-sm font-black text-on-surface-variant/60">{label}</span>
      <span className="text-xl font-black text-primary">{value}</span>
    </div>
  );
}

function TimelineRow({ item }: { item: CoreTimelineSegment }) {
  return (
    <button className="group relative flex w-full cursor-pointer flex-col gap-4 rounded-xl border border-transparent bg-surface-container-low p-6 text-right shadow-sm transition-all hover:border-outline-variant/30 hover:bg-white md:flex-row-reverse">
      <div className="flex items-center justify-between gap-2 md:min-w-[100px] md:flex-col md:items-end md:justify-center">
        <span className="text-lg font-black text-secondary">{item.startTime || "۰۰:۰۰"}</span>
        <span className="material-symbols-outlined text-3xl text-stone-300 group-hover:text-secondary">play_arrow</span>
      </div>
      <div className="grid flex-grow grid-cols-1 items-center gap-4 text-right md:grid-cols-4">
        <div className="md:col-span-1">
          <h4 className="text-lg font-black text-primary">{item.modeName || "بخش برنامه"}</h4>
          <p className="text-[10px] font-black uppercase tracking-wide text-on-surface-variant/60">تایم‌لاین</p>
        </div>
        <div className="flex flex-col">
          <span className="text-sm font-bold text-on-surface">{item.singers.join(" و ") || "—"}</span>
          <span className="text-[10px] text-on-surface-variant/60">خواننده</span>
        </div>
        <div className="flex flex-col">
          <span className="text-sm font-bold text-on-surface">{item.performers.map((artist) => artist.name).join(" و ") || "—"}</span>
          <span className="text-[10px] text-on-surface-variant/60">نوازنده</span>
        </div>
        <div className="flex flex-col">
          <span className="text-sm font-bold text-on-surface">{item.poets.join(" و ") || "—"}</span>
          <span className="text-[10px] text-on-surface-variant/60">شاعر</span>
        </div>
      </div>
    </button>
  );
}

export function TrackDetailsPage() {
  const { trackId } = useParams({ strict: false }) as { trackId?: string };
  const numericTrackId = Number(trackId);
  const [detail, setDetail] = useState<CoreTrackDetail | null>(null);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (!Number.isFinite(numericTrackId)) {
      setError("شناسه ترک معتبر نیست.");
      return;
    }

    let isMounted = true;
    getTrackDetail(numericTrackId)
      .then((payload) => {
        if (isMounted) setDetail(payload);
      })
      .catch((reason) => {
        if (isMounted) setError(String(reason));
      });
    return () => {
      isMounted = false;
    };
  }, [numericTrackId]);

  const artists = useMemo(() => {
    if (!detail) return [];
    const credits = [
      ...detail.singers.map((artist) => ({ ...artist, role: "خواننده" })),
      ...detail.performers.map((artist) => ({ ...artist, role: artist.instrument ? `نوازنده ${artist.instrument}` : "نوازنده" })),
    ];
    const seen = new Set<string>();
    return credits.filter((artist) => {
      const key = `${artist.artistId ?? artist.name}`;
      if (seen.has(key)) return false;
      seen.add(key);
      return true;
    });
  }, [detail]);

  if (error) return <div className="mx-auto max-w-5xl px-12 py-12 text-right text-sm font-bold text-on-error-container">{error}</div>;
  if (!detail) return <div className="mx-auto max-w-5xl px-12 py-12 text-right text-sm font-bold text-on-surface-variant">در حال بارگذاری...</div>;

  const portrait = detail.singers.find((artist) => artist.avatar)?.avatar || artists.find((artist) => artist.avatar)?.avatar;

  return (
    <main className="mx-auto max-w-5xl px-12 pb-32 pt-12">
      <section className="mb-20 flex flex-col items-stretch gap-12 md:flex-row-reverse">
        <div className="w-full md:w-1/3">
          <div className="relative aspect-square overflow-hidden rounded-xl bg-surface-container-high shadow-2xl">
            {portrait ? <img alt={detail.title} className="h-full w-full object-cover" src={portrait} /> : <div className="grid h-full w-full place-items-center text-primary/50"><span className="material-symbols-outlined text-7xl">music_note</span></div>}
          </div>
        </div>

        <div className="flex w-full flex-col justify-between text-right md:w-2/3">
          <h1 className="mb-3 text-4xl font-bold leading-tight text-primary">{detail.title}</h1>
          <div className="flex flex-grow flex-col justify-center py-8">
            <div className="flex w-full flex-row-reverse items-center justify-between gap-8 border-y border-surface-container-highest py-4">
              <MetaBlock label="دستگاه / آواز" value={detail.modes.join("، ") || "—"} />
              <MetaBlock label="زمان کل" value={detail.duration || "نامشخص"} bordered />
              <MetaBlock label="ارکستر" value={detail.orchestras.map((item) => item.name).join("، ") || "—"} />
            </div>
          </div>
        </div>
      </section>

      <section className="mb-16">
        <h2 className="mb-8 border-b border-surface-container-highest pb-4 text-right text-2xl font-black text-primary">هنرمندان این برنامه</h2>
        <div className="grid grid-cols-2 gap-10 md:grid-cols-4">
          {artists.map((artist) => (
            <ArtistCard key={`${artist.artistId ?? artist.name}`} align="center" artist={{ id: artist.artistId ?? artist.name, name: artist.name, subtitle: artist.role, image: artist.avatar || "" }} />
          ))}
        </div>
      </section>

      <section className="mb-16">
        <div className="mb-8 flex items-center justify-between border-b border-surface-container-highest">
          <div className="flex gap-8">
            <button className="border-b-4 border-secondary px-2 pb-4 text-xl font-black text-primary transition-all">تایم‌لاین</button>
            <button className="px-2 pb-4 text-xl font-bold text-on-surface-variant/60 transition-all hover:text-primary">اشعار</button>
          </div>
        </div>
        <div className="space-y-6">
          {detail.timeline.map((item) => <TimelineRow key={item.id} item={item} />)}
        </div>
      </section>
    </main>
  );
}
