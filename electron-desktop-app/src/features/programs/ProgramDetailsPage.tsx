import { useParams } from "@tanstack/react-router";
import { useEffect, useState } from "react";
import { BannerTracksPageSkeleton } from "../../components/skeleton/Skeletons";
import { TrackList } from "../../components/track/TrackRow";
import { getProgramTracks, type CoreProgramTracks } from "../../lib/coreApi";

const heroImage =
  "https://lh3.googleusercontent.com/aida-public/AB6AXuD-pM_C2R5U8iY_z-B1L5m9t8p7jK1l2m3n4o5p6q7r8s9t0u1v2w3x4y5z";

export function ProgramDetailsPage() {
  const { programId } = useParams({ strict: false }) as { programId?: string };
  const numericProgramId = Number(programId);
  const [program, setProgram] = useState<CoreProgramTracks | null>(null);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (!Number.isFinite(numericProgramId)) {
      setError("شناسه برنامه معتبر نیست.");
      return;
    }

    let isMounted = true;
    getProgramTracks(numericProgramId)
      .then((payload) => {
        if (isMounted) setProgram(payload);
      })
      .catch((reason) => {
        if (isMounted) setError(String(reason));
      });
    return () => {
      isMounted = false;
    };
  }, [numericProgramId]);

  if (error) return <div className="mx-auto max-w-5xl px-12 py-12 text-right text-sm font-bold text-on-error-container">{error}</div>;
  if (!program) return <BannerTracksPageSkeleton />;

  const tracks = program.tracks.map((track) => ({
    id: track.id,
    title: track.title,
    subtitle: track.artist,
    duration: track.duration,
    audioUrl: track.audioUrl,
    artworkUrls: track.singerAvatars,
  }));

  return (
    <main className="program-detail-content pb-[144px] pt-12">
      <div className="mx-auto max-w-5xl px-12">
        <section className="hero-pattern relative flex h-[360px] w-full items-end justify-center overflow-hidden rounded-3xl bg-primary">
          <div className="absolute inset-0">
            <img alt={program.title} className="h-full w-full object-cover opacity-30 mix-blend-overlay" src={heroImage} />
          </div>
          <div className="relative z-10 w-full px-12 pb-16 text-center">
            <div className="mb-6 inline-block rounded-full bg-secondary/80 px-5 py-1.5 text-[10px] font-black uppercase tracking-widest text-white backdrop-blur-md">
              {program.categoryName}
            </div>
            <h1 className="mb-6 text-5xl font-bold leading-tight tracking-tight text-white">{program.title}</h1>
            <div className="flex items-center justify-center gap-2 text-white/70">
              <span className="material-symbols-outlined text-lg text-secondary">music_note</span>
              <span className="text-base font-bold">{tracks.length.toLocaleString("fa-IR")} قطعه موسیقی</span>
            </div>
          </div>
        </section>
      </div>

      <section className="mx-auto max-w-5xl px-12 py-12">
        <div className="mb-8 flex items-center justify-between border-b border-surface-variant pb-4">
          <h2 className="text-2xl font-black text-primary">فهرست قطعات</h2>
        </div>

        <TrackList tracks={tracks} linkMode="title" />
      </section>
    </main>
  );
}
