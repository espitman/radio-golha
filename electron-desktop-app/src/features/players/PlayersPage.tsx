import { useEffect, useState } from "react";
import { ArtistCard } from "../../components/artist/ArtistCard";
import { PageHeader } from "../../components/layout/PageHeader";
import { getMusicians, type CoreHomeMusician } from "../../lib/coreApi";

const instruments = ["همه", "تار", "سه تار", "سنتور", "کمانچه", "ویلن", "نی", "تنبک"];




export function PlayersPage() {
  const [players, setPlayers] = useState<CoreHomeMusician[] | null>(null);
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

  return (
    <div className="mx-auto max-w-5xl px-12 pb-32 pt-8">
      <PageHeader title="نوازندگان" subtitle="فهرست مشاهیر موسیقی اصیل ایرانی و نوازندگان برجسته برنامه‌های گلها به تفکیک تخصص و ساز." />

      <div className="mb-12 flex flex-wrap items-center justify-start gap-2">
        {instruments.map((instrument) => (
          <button
            key={instrument}
            className={
              instrument === "تار"
                ? "rounded-full bg-primary px-4 py-2 text-sm font-bold text-on-primary shadow-md"
                : "rounded-full bg-surface-container px-4 py-2 text-sm font-bold text-primary transition-colors hover:bg-secondary-container"
            }
          >
            {instrument}
          </button>
        ))}
      </div>

      <div className="grid grid-cols-1 gap-8 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4">
        {players.map((player) => (
          <ArtistCard key={player.name} artist={{ id: player.id, name: player.name, subtitle: `${player.instrument} • ${player.programCount.toLocaleString("fa-IR")} برنامه ثبت شده`, image: player.avatar || "", alt: player.name }} />
        ))}
      </div>
    </div>
  );
}
