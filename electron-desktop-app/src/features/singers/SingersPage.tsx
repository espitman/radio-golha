import { useEffect, useState } from "react";
import { ArtistCard } from "../../components/artist/ArtistCard";
import { PageHeader } from "../../components/layout/PageHeader";
import { getSingers, type CoreHomeArtist } from "../../lib/coreApi";

const alphabet = ["همه", "الف", "ب", "پ", "ت", "ج", "چ", "ح", "خ", "د", "ر", "ز", "س", "ش", "ع", "ق", "م", "ن", "و", "ه", "ی"];




export function SingersPage() {
  const [singers, setSingers] = useState<CoreHomeArtist[] | null>(null);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    let isMounted = true;
    getSingers()
      .then((payload) => {
        if (isMounted) setSingers(payload);
      })
      .catch((reason) => {
        if (isMounted) setError(String(reason));
      });
    return () => {
      isMounted = false;
    };
  }, []);

  if (error) return <div className="mx-auto max-w-5xl px-12 py-12 text-right text-sm font-bold text-on-error-container">{error}</div>;
  if (!singers) return <div className="mx-auto max-w-5xl px-12 py-12 text-right text-sm font-bold text-on-surface-variant">در حال بارگذاری...</div>;

  return (
    <div className="mx-auto max-w-5xl px-12 pb-32 pt-8">
      <PageHeader title="خوانندگان" subtitle="فهرست جامع اساتید، خوانندگان و نوازندگان تاریخ رادیو گلها به ترتیب حروف الفبا." />

      <div className="no-scrollbar mb-12 overflow-x-auto">
        <div className="flex min-w-max justify-start gap-2 pb-4">
          {alphabet.map((letter, index) => (
            <button
              key={letter}
              className={
                index === 0
                  ? "rounded-full bg-primary px-4 py-2 text-sm font-bold text-on-primary shadow-md"
                  : "rounded-full bg-surface-container px-4 py-2 text-sm font-bold text-primary transition-colors hover:bg-secondary-container"
              }
            >
              {letter}
            </button>
          ))}
        </div>
      </div>

      <div className="grid grid-cols-1 gap-8 sm:grid-cols-2 lg:grid-cols-4">
        {singers.map((singer) => (
          <ArtistCard key={singer.name} artist={{ id: singer.id, name: singer.name, subtitle: `${singer.programCount.toLocaleString("fa-IR")} برنامه ثبت شده`, image: singer.avatar || "", alt: singer.name }} />
        ))}
      </div>
    </div>
  );
}
