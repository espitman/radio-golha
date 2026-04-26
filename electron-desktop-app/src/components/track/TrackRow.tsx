import { Link } from "@tanstack/react-router";

export type TrackRowData = {
  title: string;
  subtitle: string;
  duration: string;
  to?: string;
  id?: number | string;
};

type TrackRowProps = {
  track: TrackRowData;
  playShape?: "circle" | "square";
  linkMode?: "row" | "title" | "none";
  isPlaying?: boolean;
};

function PlayButton({ shape = "circle", isPlaying = false }: { shape?: "circle" | "square"; isPlaying?: boolean }) {
  return (
    <button
      className={
        shape === "square"
          ? "flex h-10 w-10 items-center justify-center rounded-lg bg-primary/5 text-secondary transition-all group-hover:bg-secondary group-hover:text-white"
          : "flex h-10 w-10 items-center justify-center rounded-full bg-primary/5 text-primary transition-all group-hover:bg-secondary group-hover:text-white"
      }
      type="button"
    >
      <span className="material-symbols-outlined text-xl" style={{ fontVariationSettings: "'FILL' 1" }}>{isPlaying ? "pause" : "play_arrow"}</span>
    </button>
  );
}

type TrackContentProps = Required<Pick<TrackRowProps, "track" | "playShape" | "isPlaying">>;

function TrackContent({ track, playShape, isPlaying }: TrackContentProps) {
  return (
    <>
      <PlayButton shape={playShape} isPlaying={isPlaying} />
      <div className="flex-1 text-right">
        <Link
          to="/tracks/$trackId"
          params={{ trackId: String(track.id ?? track.to ?? track.title) }}
          className="text-sm font-bold text-primary transition-colors hover:text-secondary"
          onClick={(event) => event.stopPropagation()}
        >
          {track.title}
        </Link>
        <p className="mt-0.5 text-xs text-stone-500">{track.subtitle}</p>
      </div>
      <div className="text-xs font-medium text-stone-400">{track.duration}</div>
    </>
  );
}

export function TrackRow({ track, playShape = "circle", linkMode = "row", isPlaying = false }: TrackRowProps) {
  const className = "group flex items-center gap-4 p-4 text-right transition-colors hover:bg-white/50";

  if (linkMode === "row") {
    return (
      <Link to="/programs/$programId" params={{ programId: String(track.id ?? track.to ?? track.title) }} className={className}>
        <TrackContent track={track} playShape={playShape} isPlaying={isPlaying} />
      </Link>
    );
  }

  return (
    <div className={className}>
      <TrackContent track={track} playShape={playShape} isPlaying={isPlaying} />
    </div>
  );
}

export function TrackList({ tracks, playShape = "circle", linkMode = "row" }: { tracks: TrackRowData[]; playShape?: "circle" | "square"; linkMode?: "row" | "title" | "none" }) {
  return (
    <div className="overflow-hidden rounded-2xl border border-outline-variant/20 bg-surface-container-low shadow-sm">
      <div className="divide-y divide-on-surface/5">
        {tracks.map((track, index) => (
          <TrackRow key={track.title} track={track} playShape={playShape} linkMode={linkMode} isPlaying={index === 0 && linkMode === "none"} />
        ))}
      </div>
    </div>
  );
}
