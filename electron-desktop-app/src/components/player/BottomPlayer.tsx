import { useEffect, useState } from "react";
import { usePlayer } from "./PlayerContext";

function formatTime(seconds: number) {
  const safe = Math.max(0, Math.floor(seconds));
  const minutes = Math.floor(safe / 60).toString().padStart(2, "0");
  const rest = (safe % 60).toString().padStart(2, "0");
  return `${minutes}:${rest}`;
}

function Equalizer({ active }: { active: boolean }) {
  const [tick, setTick] = useState(0);
  const bars = [8, 20, 32, 16, 24, 12];

  useEffect(() => {
    if (!active) {
      setTick(0);
      return;
    }
    const id = window.setInterval(() => setTick((value) => value + 1), 160);
    return () => window.clearInterval(id);
  }, [active]);

  return (
    <div className="flex h-8 w-11 items-center gap-1">
      {bars.map((height, index) => (
        <span
          key={index}
          className="w-1 rounded-full bg-secondary transition-all duration-300"
          style={{
            height: active ? `${10 + Math.abs(Math.sin(tick * 0.8 + index)) * 18}px` : `${height}px`,
            opacity: active ? 1 : 0.35,
          }}
        />
      ))}
    </div>
  );
}

function Artwork({ urls }: { urls: string[] }) {
  const cleanUrls = urls.filter(Boolean);
  const [index, setIndex] = useState(0);

  useEffect(() => {
    setIndex(0);
    if (cleanUrls.length <= 1) return;
    const id = window.setInterval(() => setIndex((value) => (value + 1) % cleanUrls.length), 10_000);
    return () => window.clearInterval(id);
  }, [cleanUrls.join("|")]);

  if (!cleanUrls.length) {
    return (
      <div className="grid h-full w-full place-items-center text-white/85">
        <span className="material-symbols-outlined text-2xl">music_note</span>
      </div>
    );
  }

  return <img key={cleanUrls[index]} alt="تصویر ترک" className="h-full w-full object-cover transition-opacity duration-500" src={cleanUrls[index]} />;
}

export function BottomPlayer() {
  const player = usePlayer();
  const canSeek = Boolean(player.currentTrack?.audioUrl) && !player.isLoading;
  const timeLabel = `${formatTime(player.currentTime)} / ${player.duration > 0 ? formatTime(player.duration) : player.currentTrack?.duration || "00:00"}`;
  const seekProgress = Math.max(0, Math.min(1, player.progress));

  return (
    <footer className="fixed bottom-0 left-0 z-50 h-24 w-full bg-primary shadow-[0_-4px_40px_rgba(28,28,23,0.08)]">
      <div
        className="absolute left-0 right-0 top-0 h-1.5 cursor-pointer overflow-hidden bg-white/10"
        dir="ltr"
        onClick={(event) => {
          const rect = event.currentTarget.getBoundingClientRect();
          const clickX = Math.max(0, Math.min(rect.width, event.clientX - rect.left));
          player.seekToProgress(clickX / rect.width);
        }}
      >
        <div className="h-full origin-left bg-secondary" style={{ width: `${seekProgress * 100}%` }} />
      </div>

      <div className="flex h-full w-full items-center justify-between px-12" dir="ltr">
        <div className="flex flex-1 items-center gap-4">
          <Equalizer active={player.isPlaying} />
          <div className="h-9 border-l border-white/10 pl-6" />
          <span className="text-sm font-bold text-white/90">{timeLabel}</span>
        </div>

        <div className="flex w-[320px] items-center justify-center gap-6">
          <span className="material-symbols-outlined text-white/35">shuffle</span>
          <button className="text-white/60 transition hover:text-white disabled:opacity-35" disabled={!canSeek} onClick={() => player.seekBy(-10)} type="button">
            <span className="material-symbols-outlined text-2xl">replay_10</span>
          </button>
          <button className="grid h-12 w-12 place-items-center rounded-xl bg-white text-primary transition hover:scale-105 active:scale-95" onClick={player.togglePlayPause} type="button">
            {player.isLoading ? (
              <span className="material-symbols-outlined animate-spin text-2xl">progress_activity</span>
            ) : (
              <span className="material-symbols-outlined text-3xl" style={{ fontVariationSettings: "'FILL' 1" }}>{player.isPlaying ? "pause" : "play_arrow"}</span>
            )}
          </button>
          <button className="text-white/60 transition hover:text-white disabled:opacity-35" disabled={!canSeek} onClick={() => player.seekBy(10)} type="button">
            <span className="material-symbols-outlined text-2xl">forward_10</span>
          </button>
          <span className="material-symbols-outlined text-white/35">repeat</span>
        </div>

        <div className="flex flex-1 items-center justify-end gap-4" dir="ltr">
          <div className="w-[250px] text-right">
            <h5 className="truncate text-sm font-bold text-white">{player.currentTrack?.title ?? "—"}</h5>
            <p className="mt-1 truncate text-[10px] text-white/60">{player.currentTrack?.subtitle ?? "—"}</p>
          </div>
          <div className="h-12 w-12 shrink-0 overflow-hidden rounded bg-white/10">
            <Artwork urls={player.currentTrack?.artworkUrls ?? []} />
          </div>
        </div>
      </div>
    </footer>
  );
}
