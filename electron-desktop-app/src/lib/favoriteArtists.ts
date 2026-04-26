export type FavoriteArtistKind = "singer" | "player";

export type FavoriteArtistRecord = {
  id: number | string;
  kind: FavoriteArtistKind;
  name: string;
  subtitle: string;
  image: string;
  alt?: string;
  addedAt: number;
};

export const FAVORITE_ARTISTS_STORAGE_KEY = "radioGolha.favoriteArtists";
export const FAVORITE_ARTISTS_CHANGED_EVENT = "radioGolha:favoriteArtistsChanged";

function readRawFavoriteArtists(): FavoriteArtistRecord[] {
  try {
    const raw = localStorage.getItem(FAVORITE_ARTISTS_STORAGE_KEY);
    if (!raw) return [];
    const parsed = JSON.parse(raw);
    if (!Array.isArray(parsed)) return [];
    return parsed.filter((item): item is FavoriteArtistRecord => Boolean(item?.id != null && item?.kind && item?.name));
  } catch {
    return [];
  }
}

function dispatchFavoriteArtistsChanged() {
  window.dispatchEvent(new CustomEvent(FAVORITE_ARTISTS_CHANGED_EVENT));
}

export function readFavoriteArtists(kind?: FavoriteArtistKind): FavoriteArtistRecord[] {
  const items = readRawFavoriteArtists().sort((left, right) => right.addedAt - left.addedAt);
  return kind ? items.filter((item) => item.kind === kind) : items;
}

export function writeFavoriteArtists(items: FavoriteArtistRecord[]) {
  localStorage.setItem(FAVORITE_ARTISTS_STORAGE_KEY, JSON.stringify(items));
  dispatchFavoriteArtistsChanged();
}

export function isFavoriteArtist(id: number | string, kind: FavoriteArtistKind) {
  return readRawFavoriteArtists().some((item) => String(item.id) === String(id) && item.kind === kind);
}

export function toggleFavoriteArtist(artist: Omit<FavoriteArtistRecord, "addedAt">) {
  const current = readRawFavoriteArtists();
  const exists = current.some((item) => String(item.id) === String(artist.id) && item.kind === artist.kind);

  if (exists) {
    writeFavoriteArtists(current.filter((item) => !(String(item.id) === String(artist.id) && item.kind === artist.kind)));
    return false;
  }

  writeFavoriteArtists([
    {
      ...artist,
      addedAt: Date.now(),
    },
    ...current,
  ]);
  return true;
}
