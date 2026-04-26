import { useEffect, useState } from "react";
import { ArtistCard } from "../../components/artist/ArtistCard";
import { PageHeader } from "../../components/layout/PageHeader";
import { getMusicians, type CoreHomeMusician } from "../../lib/coreApi";

function normalizeInstrument(value: string) {
  return value.trim().replace(/ك/g, "ک").replace(/ي/g, "ی");
}

export function PlayersPage() {
  const [players, setPlayers] = useState<CoreHomeMusician[] | null>(null);
  const [activeInstrument, setActiveInstrument] = useState("همه");
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    let isMounted = true;
    getMusicians()
      .then((payload) => {
        if (isMounted) setPlayers(payload);
      })
      .catch((reason) => {
        if (isMounted) setError(String(reason));
      });
    return () => {
      isMounted = false;
    };
  }, []);

  if (error) return <div className="mx-auto max-w-5xl px-12 py-12 text-right text-sm font-bold text-on-error-container">{error}</div>;
  if (!players) return <div className="mx-auto max-w-5xl px-12 py-12 text-right text-sm font-bold text-on-surface-variant">در حال بارگذاری...</div>;

  const instruments = [
    "همه",
    ...Array.from(new Set(players.map((player) => normalizeInstrument(player.instrument)).filter(Boolean))).sort((left, right) => left.localeCompare(right, "fa")),
  ];
  const visiblePlayers =
    activeInstrument === "همه"
      ? players
      : players.filter((player) => normalizeInstrument(player.instrument) === activeInstrument);

  return (
    <div className="mx-auto max-w-5xl px-12 pb-[144px] pt-8">
      <PageHeader title="نوازندگان" subtitle="فهرست مشاهیر موسیقی اصیل ایرانی و نوازندگان برجسته برنامه‌های گلها به تفکیک تخصص و ساز." />

      <div className="no-scrollbar mb-12 overflow-x-auto">
        <div className="flex min-w-max justify-start gap-2 pb-4">
        {instruments.map((instrument) => (
          <button
            key={instrument}
            onClick={() => setActiveInstrument(instrument)}
            type="button"
            className={
              activeInstrument === instrument
                ? "rounded-full bg-primary px-4 py-2 text-sm font-bold text-on-primary shadow-md"
                : "rounded-full bg-surface-container px-4 py-2 text-sm font-bold text-primary transition-colors hover:bg-secondary-container"
            }
          >
            {instrument}
          </button>
        ))}
        </div>
      </div>

      {visiblePlayers.length > 0 ? (
        <div className="grid grid-cols-1 gap-8 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4">
          {visiblePlayers.map((player) => (
            <ArtistCard key={player.id} artist={{ id: player.id, name: player.name, subtitle: `${player.instrument} • ${player.programCount.toLocaleString("fa-IR")} برنامه ثبت شده`, image: player.avatar || "", alt: player.name }} />
          ))}
        </div>
      ) : (
        <div className="rounded-3xl border border-outline-variant/20 bg-surface-container-lowest px-8 py-12 text-center text-sm font-bold text-on-surface-variant">
          نوازنده‌ای برای «{activeInstrument}» پیدا نشد.
        </div>
      )}
    </div>
  );
}
