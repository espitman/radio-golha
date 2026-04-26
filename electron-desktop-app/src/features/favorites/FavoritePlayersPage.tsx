import { useEffect, useState } from "react";
import { ArtistCard } from "../../components/artist/ArtistCard";
import { ArtistsListPageSkeleton } from "../../components/skeleton/Skeletons";
import { FAVORITE_ARTISTS_CHANGED_EVENT, readFavoriteArtists } from "../../lib/favoriteArtists";

export function FavoritePlayersPage() {
  const [favorites, setFavorites] = useState<ReturnType<typeof readFavoriteArtists> | null>(null);

  useEffect(() => {
    const refresh = () => setFavorites(readFavoriteArtists("player"));
    refresh();
    window.addEventListener(FAVORITE_ARTISTS_CHANGED_EVENT, refresh);
    window.addEventListener("storage", refresh);
    return () => {
      window.removeEventListener(FAVORITE_ARTISTS_CHANGED_EVENT, refresh);
      window.removeEventListener("storage", refresh);
    };
  }, []);

  if (!favorites) return <ArtistsListPageSkeleton tabs="instruments" />;

  return (
    <main className="pb-[144px] pt-12">
      <div className="mx-auto max-w-5xl px-12">
        <section className="hero-pattern relative flex h-[260px] w-full items-end justify-center overflow-hidden rounded-3xl bg-primary">
          <div className="absolute inset-0 bg-[radial-gradient(circle_at_top_right,rgba(210,167,56,0.25),transparent_35%),linear-gradient(135deg,rgba(9,47,83,0.95),rgba(6,32,58,0.98))]" />
          <div className="relative z-10 w-full px-12 pb-12 text-right">
            <div className="mb-4 inline-block rounded-full bg-secondary/80 px-5 py-1.5 text-[10px] font-black uppercase tracking-widest text-white backdrop-blur-md">
              مجموعه شخصی
            </div>
            <h1 className="mb-4 text-5xl font-bold leading-tight tracking-tight text-white">نوازندگان مورد علاقه</h1>
            <div className="flex items-center justify-start gap-2 text-white/70">
              <span className="material-symbols-outlined text-lg text-secondary">favorite</span>
              <span className="text-base font-bold">{favorites.length.toLocaleString("fa-IR")} هنرمند</span>
            </div>
          </div>
        </section>
      </div>

      <section className="mx-auto max-w-5xl px-12 py-12">
        {favorites.length > 0 ? (
          <div className="grid grid-cols-1 gap-8 sm:grid-cols-2 lg:grid-cols-4">
            {favorites.map((artist) => (
              <ArtistCard
                key={`${artist.kind}-${artist.id}`}
                artist={{
                  id: artist.id,
                  name: artist.name,
                  subtitle: artist.subtitle,
                  image: artist.image,
                  alt: artist.alt,
                  favoriteKind: "player",
                }}
              />
            ))}
          </div>
        ) : (
          <div className="rounded-3xl border border-outline-variant/20 bg-surface-container-lowest px-8 py-12 text-center text-sm font-bold text-on-surface-variant">
            هنوز نوازنده‌ای به علاقه‌مندی‌ها اضافه نشده است.
          </div>
        )}
      </section>
    </main>
  );
}
