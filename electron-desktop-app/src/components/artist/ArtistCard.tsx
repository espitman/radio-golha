import { Link } from "@tanstack/react-router";
import { useEffect, useState } from "react";
import {
  FAVORITE_ARTISTS_CHANGED_EVENT,
  isFavoriteArtist,
  toggleFavoriteArtist,
  type FavoriteArtistKind,
} from "../../lib/favoriteArtists";

export type ArtistCardData = {
  name: string;
  subtitle: string;
  image: string;
  alt?: string;
  id?: number | string;
  favoriteKind?: FavoriteArtistKind;
};

type ArtistCardProps = {
  artist: ArtistCardData;
  align?: "right" | "center";
};

export function ArtistCard({ artist, align = "right" }: ArtistCardProps) {
  const hasImage = Boolean(artist.image?.trim());
  const [menuPosition, setMenuPosition] = useState<{ x: number; y: number } | null>(null);
  const [favorite, setFavorite] = useState(() =>
    artist.favoriteKind ? isFavoriteArtist(artist.id ?? artist.name, artist.favoriteKind) : false,
  );

  useEffect(() => {
    if (!artist.favoriteKind) return;

    const refresh = () => setFavorite(isFavoriteArtist(artist.id ?? artist.name, artist.favoriteKind!));
    refresh();
    window.addEventListener(FAVORITE_ARTISTS_CHANGED_EVENT, refresh);
    window.addEventListener("storage", refresh);
    return () => {
      window.removeEventListener(FAVORITE_ARTISTS_CHANGED_EVENT, refresh);
      window.removeEventListener("storage", refresh);
    };
  }, [artist.favoriteKind, artist.id, artist.name]);

  useEffect(() => {
    if (!menuPosition) return;

    function closeMenu() {
      setMenuPosition(null);
    }

    window.addEventListener("click", closeMenu);
    window.addEventListener("contextmenu", closeMenu);
    return () => {
      window.removeEventListener("click", closeMenu);
      window.removeEventListener("contextmenu", closeMenu);
    };
  }, [menuPosition]);

  function handleToggleFavorite() {
    if (!artist.favoriteKind) return;
    const nextState = toggleFavoriteArtist({
      id: artist.id ?? artist.name,
      kind: artist.favoriteKind,
      name: artist.name,
      subtitle: artist.subtitle,
      image: artist.image,
      alt: artist.alt,
    });
    setFavorite(nextState);
    setMenuPosition(null);
  }

  return (
    <>
      <Link
        to="/artists/$artistId"
        params={{ artistId: String(artist.id ?? artist.name) }}
        className="group relative block overflow-hidden rounded-xl border border-outline-variant/10 bg-surface-container-lowest transition-all duration-500 hover:shadow-xl"
        onContextMenu={(event) => {
          if (!artist.favoriteKind) return;
          event.preventDefault();
          setMenuPosition({ x: event.clientX, y: event.clientY });
        }}
      >
        <div className="relative aspect-square overflow-hidden">
          {hasImage ? (
            <img
              alt={artist.alt ?? artist.name}
              className="h-full w-full object-cover grayscale transition-all duration-700 group-hover:scale-105 group-hover:grayscale-0"
              src={artist.image}
            />
          ) : (
            <div className="flex h-full w-full items-center justify-center bg-[radial-gradient(circle_at_50%_35%,rgba(150,112,32,0.18),transparent_38%),linear-gradient(135deg,rgba(242,239,229,1),rgba(224,220,209,1))] text-primary/55">
              <div className="grid h-20 w-20 place-items-center rounded-full bg-white/45 shadow-inner">
                <span className="material-symbols-outlined text-5xl">person</span>
              </div>
            </div>
          )}
          <div className="absolute inset-0 flex items-end bg-gradient-to-t from-primary/80 via-transparent to-transparent p-6 opacity-0 transition-opacity duration-500 group-hover:opacity-100">
            <span className="block w-full rounded-full bg-secondary-container px-6 py-2 text-center text-xs font-bold text-on-secondary-container">مشاهده آثار</span>
          </div>
        </div>
        <div className={align === "center" ? "p-5 text-center" : "p-5 text-right"}>
          <h3 className="mb-1 text-xl font-bold text-primary">{artist.name}</h3>
          <p className="text-sm font-medium text-on-surface-variant">{artist.subtitle}</p>
        </div>
      </Link>

      {menuPosition && artist.favoriteKind ? (
        <div
          className="fixed z-[1200] min-w-[220px] overflow-hidden rounded-2xl border border-outline-variant/25 bg-white p-2 shadow-[0_18px_40px_rgba(0,0,0,0.18)]"
          style={{ top: menuPosition.y + 8, left: menuPosition.x - 190 }}
          dir="rtl"
          onClick={(event) => event.stopPropagation()}
        >
          <button
            className="flex w-full items-center justify-between rounded-xl px-4 py-3 text-right text-sm font-bold text-primary transition-colors hover:bg-surface-container-low"
            onClick={handleToggleFavorite}
            type="button"
          >
            <span>{favorite ? "حذف از علاقه‌مندی‌ها" : "افزودن به علاقه‌مندی‌ها"}</span>
            <span
              className="material-symbols-outlined text-[18px] text-secondary"
              style={{ fontVariationSettings: favorite ? "'FILL' 1" : "'FILL' 0" }}
            >
              {favorite ? "favorite" : "favorite"}
            </span>
          </button>
        </div>
      ) : null}
    </>
  );
}
