import { Link } from "@tanstack/react-router";
import type { MouseEvent } from "react";
import { usePlayer } from "../player/PlayerContext";

export type TrackRowData = {
  title: string;
  subtitle: string;
  duration: string;
  to?: string;
  id?: number | string;
  audioUrl?: string | null;
  artworkUrls?: string[];
};

type TrackRowProps = {
  track: TrackRowData;
  playShape?: "circle" | "square";
  linkMode?: "row" | "title" | "none";
  isPlaying?: boolean;
};

function PlayButton({
  shape = "circle",
  isPlaying = false,
  isLoading = false,
  onClick,
}: {
  shape?: "circle" | "square";
  isPlaying?: boolean;
  isLoading?: boolean;
  onClick: (event: MouseEvent<HTMLButtonElement>) => void;
}) {
  return (
    <button
      className={
        shape === "square"
          ? "flex h-10 w-10 items-center justify-center rounded-lg bg-primary/5 text-secondary transition-all group-hover:bg-secondary group-hover:text-white"
          : "flex h-10 w-10 items-center justify-center rounded-full bg-primary/5 text-primary transition-all group-hover:bg-secondary group-hover:text-white"
      }
      onClick={onClick}
      type="button"
    >
      <span className={`material-symbols-outlined text-xl ${isLoading ? "animate-spin" : ""}`} style={{ fontVariationSettings: "'FILL' 1" }}>
        {isLoading ? "progress_activity" : isPlaying ? "pause" : "play_arrow"}
      </span>
    </button>
  );
}

type TrackContentProps = Required<Pick<TrackRowProps, "track" | "playShape" | "isPlaying">> & {
  onPlay: () => void;
};

function TrackContent({ track, playShape, isPlaying, onPlay }: TrackContentProps) {
  const player = usePlayer();
  const isCurrentTrack = player.currentTrack?.id != null && String(player.currentTrack.id) === String(track.id ?? track.to ?? track.title);
  const displayPlaying = isCurrentTrack && player.isPlaying;
  const displayLoading = isCurrentTrack && player.isLoading;

  function handlePlay(event: MouseEvent<HTMLButtonElement>) {
    event.preventDefault();
    event.stopPropagation();
    onPlay();
  }

  return (
    <>
      <PlayButton shape={playShape} isPlaying={displayPlaying || isPlaying} isLoading={displayLoading} onClick={handlePlay} />
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
  const player = usePlayer();
  const className = "group flex items-center gap-4 p-4 text-right transition-colors hover:bg-white/50";
  const isCurrentTrack = player.currentTrack?.id != null && String(player.currentTrack.id) === String(track.id ?? track.to ?? track.title);

  function startPlayback() {
    if (isCurrentTrack && track.audioUrl) {
      player.togglePlayPause();
      return;
    }
    player.playTrack({
      id: track.id ?? track.to ?? track.title,
      title: track.title,
      subtitle: track.subtitle,
      duration: track.duration,
      audioUrl: track.audioUrl,
      artworkUrls: track.artworkUrls,
    });
  }

  if (linkMode === "row") {
    return (
      <Link to="/programs/$programId" params={{ programId: String(track.id ?? track.to ?? track.title) }} className={className}>
        <TrackContent track={track} playShape={playShape} isPlaying={isPlaying} onPlay={startPlayback} />
      </Link>
    );
  }

  return (
    <div className={`${className} cursor-pointer`} onClick={startPlayback}>
      <TrackContent track={track} playShape={playShape} isPlaying={isPlaying} onPlay={startPlayback} />
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
