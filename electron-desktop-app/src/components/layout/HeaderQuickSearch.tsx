import { useEffect, useRef, useState } from "react";
import { useNavigate } from "@tanstack/react-router";
import clsx from "clsx";
import { topBarSearch, type CoreTopBarSearchResult } from "../../lib/coreApi";

function ArtistAvatar({ result }: { result: CoreTopBarSearchResult }) {
  if (result.kind === "artist" && result.avatar) {
    return <img alt={result.title} className="h-6 w-6 shrink-0 rounded-full object-cover" src={result.avatar} />;
  }

  if (result.kind === "artist") {
    return (
      <span className="grid h-6 w-6 shrink-0 place-items-center rounded-full bg-primary/8 text-primary/55">
        <span className="material-symbols-outlined text-[14px] leading-none">person</span>
      </span>
    );
  }

  return (
    <span className="grid h-6 w-6 shrink-0 place-items-center rounded-full bg-primary/8 text-primary/70">
      <span className="material-symbols-outlined text-[12px] leading-none">music_note</span>
    </span>
  );
}

export function HeaderQuickSearch() {
  const navigate = useNavigate();
  const rootRef = useRef<HTMLDivElement | null>(null);
  const [text, setText] = useState("");
  const [results, setResults] = useState<CoreTopBarSearchResult[]>([]);
  const [isLoading, setIsLoading] = useState(false);
  const [isFocused, setIsFocused] = useState(false);
  const [isPresented, setIsPresented] = useState(false);
  const [highlightedIndex, setHighlightedIndex] = useState<number | null>(null);

  useEffect(() => {
    const trimmed = text.trim();
    if (!trimmed) {
      setResults([]);
      setIsLoading(false);
      setHighlightedIndex(null);
      setIsPresented(false);
      return;
    }

    if (isFocused) {
      setIsPresented(true);
    }

    setIsLoading(true);
    const timeoutId = window.setTimeout(() => {
      topBarSearch(trimmed, 10)
        .then((payload) => {
          if (trimmed !== text.trim()) return;
          setResults(payload);
          setIsLoading(false);
          setHighlightedIndex(payload.length ? 0 : null);
        })
        .catch(() => {
          setResults([]);
          setIsLoading(false);
          setHighlightedIndex(null);
        });
    }, 250);

    return () => window.clearTimeout(timeoutId);
  }, [isFocused, text]);

  useEffect(() => {
    function handlePointerDown(event: MouseEvent) {
      if (!rootRef.current?.contains(event.target as Node)) {
        setIsPresented(false);
      }
    }

    document.addEventListener("mousedown", handlePointerDown);
    return () => document.removeEventListener("mousedown", handlePointerDown);
  }, []);

  function selectResult(result: CoreTopBarSearchResult) {
    if (result.kind === "artist") {
      void navigate({ to: "/artists/$artistId", params: { artistId: String(result.id) } });
    } else {
      void navigate({ to: "/tracks/$trackId", params: { trackId: String(result.trackId ?? result.id) } });
    }

    setText("");
    setResults([]);
    setHighlightedIndex(null);
    setIsPresented(false);
  }

  function moveSelection(step: number) {
    if (!results.length) return;
    if (!isPresented) {
      setIsPresented(true);
    }
    const current = highlightedIndex ?? (step > 0 ? -1 : results.length);
    const next = Math.max(0, Math.min(results.length - 1, current + step));
    setHighlightedIndex(next);
  }

  function selectHighlightedOrFirst() {
    if (!results.length) return;
    const index = Math.max(0, Math.min(results.length - 1, highlightedIndex ?? 0));
    selectResult(results[index]);
  }

  return (
    <div ref={rootRef} className="relative">
      <div
        className="flex h-9 w-[260px] items-center gap-2 rounded-full bg-surface-container-low px-3"
        dir="rtl"
      >
        <input
          className="w-full border-none bg-transparent text-right text-[9.75px] text-primary/75 outline-none placeholder:text-primary/45"
          dir="rtl"
          placeholder="جستجو در آرشیو..."
          type="text"
          value={text}
          onChange={(event) => setText(event.target.value)}
          onFocus={() => {
            setIsFocused(true);
            if (text.trim()) {
              setIsPresented(true);
            }
          }}
          onBlur={() => {
            setIsFocused(false);
          }}
          onKeyDown={(event) => {
            if (event.key === "ArrowDown") {
              event.preventDefault();
              moveSelection(1);
            } else if (event.key === "ArrowUp") {
              event.preventDefault();
              moveSelection(-1);
            } else if (event.key === "Enter") {
              event.preventDefault();
              selectHighlightedOrFirst();
            } else if (event.key === "Escape") {
              event.preventDefault();
              setIsPresented(false);
            }
          }}
        />
        <span className="material-symbols-outlined shrink-0 text-[12px] text-primary/45">search</span>
      </div>

      {isPresented ? (
        <div className="absolute right-0 top-11 z-[1001] w-[360px] overflow-hidden rounded-xl border border-outline-variant bg-white shadow-[0_8px_16px_rgba(0,0,0,0.08)]">
          {isLoading ? (
            <div className="flex h-11 items-center justify-start gap-2 px-3 text-left">
              <span className="material-symbols-outlined animate-spin text-[14px] text-secondary">progress_activity</span>
              <span className="text-[9.5px] text-on-surface-variant/80">در حال جستجو...</span>
            </div>
          ) : results.length === 0 ? (
            <div className="flex h-11 items-center justify-start px-3 text-left text-[9.5px] text-on-surface-variant/80">
              نتیجه‌ای پیدا نشد
            </div>
          ) : (
            <div>
              {results.map((result, index) => (
                <button
                  key={`${result.kind}-${result.id}`}
                  className={clsx(
                    "flex h-12 w-full items-center justify-start gap-2.5 px-3 text-left",
                    index === highlightedIndex ? "bg-primary/8" : "bg-white",
                  )}
                  onClick={() => selectResult(result)}
                  type="button"
                  dir="ltr"
                >
                  <ArtistAvatar result={result} />
                  <div className="min-w-0 flex-1">
                    <div className="truncate text-right text-[10px] font-bold text-primary/88" dir="rtl">
                      {result.title}
                    </div>
                    <div className="mt-0.5 truncate text-right text-[8.75px] text-on-surface-variant/80" dir="rtl">
                      {result.subtitle}
                    </div>
                  </div>
                </button>
              ))}
            </div>
          )}
        </div>
      ) : null}
    </div>
  );
}
