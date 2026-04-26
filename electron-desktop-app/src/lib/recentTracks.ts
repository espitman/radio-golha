import type { PlayerTrack } from "../components/player/PlayerContext";

export const RECENT_TRACKS_STORAGE_KEY = "radioGolha.recentTracks";
export const RECENT_TRACKS_CHANGED_EVENT = "radioGolha:recentTracksChanged";

export type RecentTrack = PlayerTrack & {
  playedAt: number;
  playCount: number;
};

function readRawRecentTracks(): RecentTrack[] {
  try {
    const raw = localStorage.getItem(RECENT_TRACKS_STORAGE_KEY);
    if (!raw) return [];
    const parsed = JSON.parse(raw);
    if (!Array.isArray(parsed)) return [];
    return parsed.filter((item): item is RecentTrack => Boolean(item?.title && item?.playedAt));
  } catch {
    return [];
  }
}

function dispatchRecentTracksChanged() {
  window.dispatchEvent(new CustomEvent(RECENT_TRACKS_CHANGED_EVENT));
}

export function readRecentTracks(limit?: number): RecentTrack[] {
  const tracks = readRawRecentTracks().sort((left, right) => right.playedAt - left.playedAt);
  return typeof limit === "number" ? tracks.slice(0, limit) : tracks;
}

export function readPopularTracks(limit?: number): RecentTrack[] {
  const tracks = readRawRecentTracks().sort((left, right) => {
    if (right.playCount !== left.playCount) return right.playCount - left.playCount;
    return right.playedAt - left.playedAt;
  });
  return typeof limit === "number" ? tracks.slice(0, limit) : tracks;
}

export function writeRecentTracks(tracks: RecentTrack[]) {
  localStorage.setItem(RECENT_TRACKS_STORAGE_KEY, JSON.stringify(tracks));
  dispatchRecentTracksChanged();
}

export function pushRecentTrack(track: PlayerTrack) {
  const trackKey = String(track.id ?? `${track.title}:${track.subtitle}:${track.duration}`);
  const currentTracks = readRawRecentTracks();
  const existingTrack = currentTracks.find((item) => String(item.id ?? `${item.title}:${item.subtitle}:${item.duration}`) === trackKey);
  const nextTrack: RecentTrack = {
    ...track,
    playedAt: Date.now(),
    playCount: (existingTrack?.playCount ?? 0) + 1,
  };

  const nextTracks = [
    nextTrack,
    ...currentTracks.filter((item) => String(item.id ?? `${item.title}:${item.subtitle}:${item.duration}`) !== trackKey),
  ].slice(0, 100);

  writeRecentTracks(nextTracks);
}
