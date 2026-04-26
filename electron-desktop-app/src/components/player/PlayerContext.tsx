import { createContext, useCallback, useContext, useEffect, useMemo, useRef, useState, type ReactNode } from "react";
import { getTrackDetail } from "../../lib/coreApi";

export type PlayerTrack = {
  id?: number | string;
  title: string;
  subtitle: string;
  duration: string;
  audioUrl?: string | null;
  artworkUrls?: string[];
};

type PlayerContextValue = {
  currentTrack: PlayerTrack | null;
  isPlaying: boolean;
  isLoading: boolean;
  currentTime: number;
  duration: number;
  progress: number;
  playTrack: (track: PlayerTrack) => void;
  togglePlayPause: () => void;
  seekBy: (seconds: number) => void;
  seekToProgress: (progress: number) => void;
};

const PlayerContext = createContext<PlayerContextValue | null>(null);
const PLAYER_STORAGE_KEY = "radioGolha.playerState";

type StoredPlayerState = {
  track: PlayerTrack;
  currentTime: number;
};

function toAbsoluteAudioUrl(value: string) {
  try {
    return new URL(value, window.location.href).href;
  } catch {
    return value;
  }
}

function parseDuration(value: string) {
  const normalized = value
    .replace(/[۰-۹]/g, (digit) => String("۰۱۲۳۴۵۶۷۸۹".indexOf(digit)))
    .replace(/\s/g, "");
  const parts = normalized.split(":").map((part) => Number(part));
  if (parts.some((part) => !Number.isFinite(part))) return 0;
  if (parts.length === 2) return parts[0] * 60 + parts[1];
  if (parts.length === 3) return parts[0] * 3600 + parts[1] * 60 + parts[2];
  return 0;
}

function readStoredPlayerState(): StoredPlayerState | null {
  try {
    const raw = localStorage.getItem(PLAYER_STORAGE_KEY);
    if (!raw) return null;
    const parsed = JSON.parse(raw) as StoredPlayerState;
    if (!parsed.track?.title) return null;
    return {
      track: parsed.track,
      currentTime: Math.max(0, Number(parsed.currentTime) || 0),
    };
  } catch {
    return null;
  }
}

function writeStoredPlayerState(track: PlayerTrack, currentTime: number) {
  localStorage.setItem(
    PLAYER_STORAGE_KEY,
    JSON.stringify({
      track,
      currentTime: Math.max(0, currentTime),
    } satisfies StoredPlayerState),
  );
}

function hasArtwork(track: PlayerTrack) {
  return Boolean(track.artworkUrls?.some(Boolean));
}

async function fetchTrackArtwork(trackId: PlayerTrack["id"]) {
  const numericTrackId = Number(trackId);
  if (!Number.isFinite(numericTrackId)) return [];

  const detail = await getTrackDetail(numericTrackId);
  return detail.singers.map((artist) => artist.avatar).filter((avatar): avatar is string => Boolean(avatar));
}

async function resolvePlayableTrack(track: PlayerTrack): Promise<PlayerTrack> {
  if (track.audioUrl) return track;

  const numericTrackId = Number(track.id);
  if (!Number.isFinite(numericTrackId)) return track;

  const detail = await getTrackDetail(numericTrackId);
  const artworkUrls = detail.singers.map((artist) => artist.avatar).filter((avatar): avatar is string => Boolean(avatar));

  return {
    ...track,
    title: track.title || detail.title,
    subtitle: track.subtitle || detail.singers.map((artist) => artist.name).join(" و ") || detail.categoryName,
    duration: track.duration || detail.duration || "نامشخص",
    audioUrl: detail.audioUrl,
    artworkUrls: track.artworkUrls?.length ? track.artworkUrls : artworkUrls,
  };
}

export function PlayerProvider({ children }: { children: ReactNode }) {
  const audioRef = useRef<HTMLAudioElement | null>(null);
  const pendingRestoreTimeRef = useRef<number | null>(null);
  const artworkRequestIdRef = useRef(0);
  const [currentTrack, setCurrentTrack] = useState<PlayerTrack | null>(null);
  const [isPlaying, setIsPlaying] = useState(false);
  const [isLoading, setIsLoading] = useState(false);
  const [currentTime, setCurrentTime] = useState(0);
  const [duration, setDuration] = useState(0);

  useEffect(() => {
    const audio = new Audio();
    audio.preload = "metadata";
    audioRef.current = audio;

    const updateTime = () => setCurrentTime(audio.currentTime || 0);
    const restorePendingTime = () => {
      const pendingTime = pendingRestoreTimeRef.current;
      if (pendingTime == null) return;
      const safeDuration = Number.isFinite(audio.duration) && audio.duration > 0 ? audio.duration : Number.MAX_SAFE_INTEGER;
      audio.currentTime = Math.max(0, Math.min(pendingTime, safeDuration));
      setCurrentTime(audio.currentTime || pendingTime);
      pendingRestoreTimeRef.current = null;
    };
    const updateDuration = () => {
      setDuration(Number.isFinite(audio.duration) ? audio.duration : 0);
      restorePendingTime();
    };
    const markLoading = () => setIsLoading(true);
    const markReady = () => setIsLoading(false);
    const markPlaying = () => {
      setIsPlaying(true);
      setIsLoading(false);
    };
    const markPaused = () => setIsPlaying(false);
    const markEnded = () => setIsPlaying(false);

    audio.addEventListener("timeupdate", updateTime);
    audio.addEventListener("durationchange", updateDuration);
    audio.addEventListener("loadedmetadata", updateDuration);
    audio.addEventListener("loadstart", markLoading);
    audio.addEventListener("waiting", markLoading);
    audio.addEventListener("canplay", markReady);
    audio.addEventListener("playing", markPlaying);
    audio.addEventListener("pause", markPaused);
    audio.addEventListener("ended", markEnded);

    const storedState = readStoredPlayerState();
    if (storedState) {
      setCurrentTrack(storedState.track);
      setCurrentTime(storedState.currentTime);
      setDuration(parseDuration(storedState.track.duration));
      if (storedState.track.audioUrl) {
        pendingRestoreTimeRef.current = storedState.currentTime;
        audio.src = toAbsoluteAudioUrl(storedState.track.audioUrl);
        audio.load();
      }
    }

    return () => {
      audio.pause();
      audio.removeEventListener("timeupdate", updateTime);
      audio.removeEventListener("durationchange", updateDuration);
      audio.removeEventListener("loadedmetadata", updateDuration);
      audio.removeEventListener("loadstart", markLoading);
      audio.removeEventListener("waiting", markLoading);
      audio.removeEventListener("canplay", markReady);
      audio.removeEventListener("playing", markPlaying);
      audio.removeEventListener("pause", markPaused);
      audio.removeEventListener("ended", markEnded);
      audioRef.current = null;
    };
  }, []);

  useEffect(() => {
    if (!currentTrack) return;
    writeStoredPlayerState(currentTrack, currentTime);
  }, [currentTime, currentTrack]);

  useEffect(() => {
    const persistBeforeClose = () => {
      if (!currentTrack) return;
      writeStoredPlayerState(currentTrack, audioRef.current?.currentTime || currentTime);
    };
    window.addEventListener("beforeunload", persistBeforeClose);
    window.addEventListener("pagehide", persistBeforeClose);
    return () => {
      window.removeEventListener("beforeunload", persistBeforeClose);
      window.removeEventListener("pagehide", persistBeforeClose);
    };
  }, [currentTime, currentTrack]);

  useEffect(() => {
    if (!currentTrack || hasArtwork(currentTrack)) return;

    const requestId = artworkRequestIdRef.current + 1;
    artworkRequestIdRef.current = requestId;

    fetchTrackArtwork(currentTrack.id)
      .then((artworkUrls) => {
        if (artworkRequestIdRef.current !== requestId || !artworkUrls.length) return;
        setCurrentTrack((track) => {
          if (!track || hasArtwork(track) || String(track.id ?? "") !== String(currentTrack.id ?? "")) return track;
          return { ...track, artworkUrls };
        });
      })
      .catch(() => {
        // Artwork is nice-to-have; playback should never fail because an image lookup failed.
      });
  }, [currentTrack]);

  const playTrack = useCallback((track: PlayerTrack) => {
    const audio = audioRef.current;
    setCurrentTrack(track);
    setCurrentTime(0);
    setDuration(parseDuration(track.duration));
    setIsLoading(true);

    resolvePlayableTrack(track)
      .then((playableTrack) => {
        setCurrentTrack(playableTrack);
        setDuration(parseDuration(playableTrack.duration));

        if (!audio || !playableTrack.audioUrl) {
          setIsPlaying(false);
          setIsLoading(false);
          return;
        }

        const nextSource = toAbsoluteAudioUrl(playableTrack.audioUrl);
        const isSameSource = audio.src === nextSource;
        if (!isSameSource) {
          audio.src = nextSource;
          audio.currentTime = 0;
        }

        audio.play().catch(() => {
          setIsPlaying(false);
          setIsLoading(false);
        });
      })
      .catch(() => {
        setIsPlaying(false);
        setIsLoading(false);
      });
  }, []);

  const togglePlayPause = useCallback(() => {
    const audio = audioRef.current;
    if (!audio || !currentTrack?.audioUrl) return;
    if (audio.paused) {
      setIsLoading(true);
      audio.play().catch(() => {
        setIsPlaying(false);
        setIsLoading(false);
      });
    } else {
      audio.pause();
    }
  }, [currentTrack]);

  const seekToProgress = useCallback((nextProgress: number) => {
    const audio = audioRef.current;
    const safeDuration = audio?.duration && Number.isFinite(audio.duration) ? audio.duration : duration || parseDuration(currentTrack?.duration ?? "");
    if (!audio || !safeDuration) return;
    const clamped = Math.max(0, Math.min(1, nextProgress));
    audio.currentTime = clamped * safeDuration;
    setCurrentTime(audio.currentTime);
  }, [currentTrack?.duration, duration]);

  const seekBy = useCallback((seconds: number) => {
    const audio = audioRef.current;
    if (!audio) return;
    const safeDuration = Number.isFinite(audio.duration) ? audio.duration : duration;
    audio.currentTime = Math.max(0, Math.min(safeDuration || Number.MAX_SAFE_INTEGER, audio.currentTime + seconds));
    setCurrentTime(audio.currentTime);
  }, [duration]);

  const effectiveDuration = duration || parseDuration(currentTrack?.duration ?? "");
  const value = useMemo<PlayerContextValue>(() => ({
    currentTrack,
    isPlaying,
    isLoading,
    currentTime,
    duration: effectiveDuration,
    progress: effectiveDuration > 0 ? currentTime / effectiveDuration : 0,
    playTrack,
    togglePlayPause,
    seekBy,
    seekToProgress,
  }), [currentTrack, currentTime, effectiveDuration, isLoading, isPlaying, playTrack, seekBy, seekToProgress, togglePlayPause]);

  return <PlayerContext.Provider value={value}>{children}</PlayerContext.Provider>;
}

export function usePlayer() {
  const context = useContext(PlayerContext);
  if (!context) throw new Error("usePlayer must be used within PlayerProvider");
  return context;
}
