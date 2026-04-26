import { Link } from "@tanstack/react-router";
import { useEffect, useMemo, useState } from "react";
import { usePlayer } from "../../components/player/PlayerContext";
import { TrackListSkeleton } from "../../components/skeleton/Skeletons";
import { searchPrograms, type CoreSearchPayload, type CoreSearchResponse } from "../../lib/coreApi";

type SearchChip = {
  id: string;
  fieldId: string;
  valueId?: string;
  label: string;
};

type StoredSearchRequest = {
  payload: CoreSearchPayload;
  chips: SearchChip[];
};

const fieldPayloadKeys: Record<string, keyof CoreSearchPayload> = {
  category: "categoryIds",
  singer: "singerIds",
  mode: "modeIds",
  orchestra: "orchestraIds",
  instrument: "instrumentIds",
  performer: "performerIds",
  poet: "poetIds",
  announcer: "announcerIds",
  composer: "composerIds",
  arranger: "arrangerIds",
  orchestraLeader: "orchestraLeaderIds",
};

function readStoredRequest(): StoredSearchRequest | null {
  try {
    const raw = sessionStorage.getItem("radioGolha.searchRequest");
    return raw ? (JSON.parse(raw) as StoredSearchRequest) : null;
  } catch {
    return null;
  }
}

function storeRequest(request: StoredSearchRequest) {
  sessionStorage.setItem("radioGolha.searchRequest", JSON.stringify(request));
}

function removeChipFromPayload(payload: CoreSearchPayload, chip: SearchChip): CoreSearchPayload {
  if (chip.fieldId === "transcript") {
    const { transcriptQuery: _removed, ...rest } = payload;
    return rest;
  }

  const payloadKey = fieldPayloadKeys[chip.fieldId];
  if (!payloadKey || !chip.valueId) return payload;

  const current = payload[payloadKey];
  if (!Array.isArray(current)) return payload;

  return {
    ...payload,
    [payloadKey]: current.filter((id) => id !== Number(chip.valueId)),
  };
}

export function SearchResultsPage() {
  const player = usePlayer();
  const [request, setRequest] = useState<StoredSearchRequest | null>(() => readStoredRequest());
  const [result, setResult] = useState<CoreSearchResponse | null>(null);
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (!request) return;
    let isMounted = true;
    setIsLoading(true);
    setError(null);
    searchPrograms(request.payload)
      .then((payload) => {
        if (isMounted) setResult(payload);
      })
      .catch((reason) => {
        if (isMounted) setError(String(reason));
      })
      .finally(() => {
        if (isMounted) setIsLoading(false);
      });
    return () => {
      isMounted = false;
    };
  }, [request]);

  const chips = useMemo(() => request?.chips ?? [], [request]);

  function removeChip(chip: SearchChip) {
    if (!request) return;
    const nextRequest = {
      payload: removeChipFromPayload(request.payload, chip),
      chips: request.chips.filter((item) => item.id !== chip.id),
    };
    storeRequest(nextRequest);
    setRequest(nextRequest);
  }

  function playResult(row: CoreSearchResponse["rows"][number]) {
    const isCurrent = player.currentTrack?.id != null && String(player.currentTrack.id) === String(row.id);
    if (isCurrent && row.audioUrl) {
      player.togglePlayPause();
      return;
    }

    player.playTrack({
      id: row.id,
      title: row.title,
      subtitle: row.artist || row.categoryName,
      duration: row.duration || "نامشخص",
      audioUrl: row.audioUrl,
      artworkUrls: [],
    });
  }

  if (!request) {
    return (
      <div className="mx-auto max-w-5xl px-12 py-12 text-right">
        <div className="rounded-3xl border border-outline-variant/20 bg-surface-container-lowest px-8 py-12 text-center text-sm font-bold text-on-surface-variant">
          هنوز جستجویی انجام نشده است.
        </div>
      </div>
    );
  }

  if (error) return <div className="mx-auto max-w-5xl px-12 py-12 text-right text-sm font-bold text-on-error-container">{error}</div>;

  return (
    <main className="mx-auto max-w-5xl px-12 pb-[144px] pt-8">
      <section className="relative flex h-[232px] items-end justify-center overflow-hidden rounded-3xl bg-primary px-8 pb-12 text-center shadow-2xl shadow-primary/10">
        <div className="absolute inset-0 bg-[linear-gradient(135deg,#001A2F,#002E56,#001A2F)]" />
        <div className="absolute inset-0 opacity-20 shamseh-pattern" />
        <div className="relative z-10">
          <h1 className="text-[40px] font-black leading-tight text-white">نتایج جستجو</h1>
          <p className="mt-2 text-sm font-bold text-white/80">
            {isLoading ? "در حال جستجو..." : `${(result?.total ?? 0).toLocaleString("fa-IR")} برنامه`}
          </p>
        </div>

        {chips.length ? (
          <div className="absolute bottom-4 right-6 left-6 z-20 no-scrollbar overflow-x-auto">
            <div className="flex min-w-full justify-start gap-2">
              {chips.map((chip) => (
                <span key={chip.id} className="flex h-8 flex-row-reverse items-center gap-2 rounded-full bg-secondary px-3 text-[10px] font-bold text-white">
                  <span className="max-w-[180px] truncate">{chip.label}</span>
                  <button className="grid size-4 shrink-0 place-items-center rounded-full bg-white/10 text-white" type="button" onClick={() => removeChip(chip)} aria-label="حذف فیلتر">
                    <span className="material-symbols-outlined !block !text-[11px] !leading-none">close</span>
                  </button>
                </span>
              ))}
            </div>
          </div>
        ) : null}
      </section>

      <section className="mt-6">
        <div className="mb-5 flex items-center justify-between border-b border-surface-variant pb-4">
          <h2 className="text-2xl font-black text-primary">فهرست قطعات</h2>
          <button className="flex items-center gap-2 rounded-lg bg-secondary/10 px-3 py-2 text-[11px] font-bold text-secondary" type="button">
            <span className="material-symbols-outlined text-[16px]">download</span>
            ذخیره نتایج به‌عنوان پلی‌لیست
          </button>
        </div>

        {isLoading ? (
          <TrackListSkeleton count={7} />
        ) : result?.rows.length ? (
          <div className="overflow-hidden rounded-2xl border border-outline-variant/20 bg-surface-container-low shadow-sm">
            <div className="divide-y divide-on-surface/5">
              {result.rows.map((row) => (
                <div key={row.id} className="group flex h-[72px] items-center gap-4 px-4 text-right transition-colors hover:bg-white/50">
                  <button className="grid h-10 w-10 place-items-center rounded-full bg-primary/5 text-primary transition-all group-hover:bg-secondary group-hover:text-white" type="button" onClick={() => playResult(row)}>
                    <span
                      className={`material-symbols-outlined text-xl ${player.currentTrack?.id === row.id && player.isLoading ? "animate-spin" : ""}`}
                      style={{ fontVariationSettings: "'FILL' 1" }}
                    >
                      {player.currentTrack?.id === row.id && player.isLoading ? "progress_activity" : player.currentTrack?.id === row.id && player.isPlaying ? "pause" : "play_arrow"}
                    </span>
                  </button>
                  <div className="min-w-0 flex-1 text-right">
                    <Link to="/tracks/$trackId" params={{ trackId: String(row.id) }} className="block truncate text-sm font-bold text-primary transition-colors hover:text-secondary">
                      {row.title}
                    </Link>
                    <p className="mt-0.5 truncate text-xs text-stone-500">{row.artist || row.categoryName}</p>
                  </div>
                  <div className="w-16 text-left text-xs font-medium text-stone-400">{row.duration || "نامشخص"}</div>
                </div>
              ))}
            </div>
          </div>
        ) : (
          <div className="rounded-2xl border border-outline-variant/20 bg-surface-container-low px-8 py-12 text-center text-sm font-bold text-on-surface-variant">
            نتیجه‌ای پیدا نشد.
          </div>
        )}
      </section>
    </main>
  );
}
