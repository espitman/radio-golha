import { Link } from "@tanstack/react-router";

export type ArtistCardData = {
  name: string;
  subtitle: string;
  image: string;
  alt?: string;
  id?: number | string;
};

type ArtistCardProps = {
  artist: ArtistCardData;
  align?: "right" | "center";
};

export function ArtistCard({ artist, align = "right" }: ArtistCardProps) {
  const hasImage = Boolean(artist.image?.trim());

  return (
    <Link
      to="/artists/$artistId"
      params={{ artistId: String(artist.id ?? artist.name) }}
      className="group relative block overflow-hidden rounded-xl border border-outline-variant/10 bg-surface-container-lowest transition-all duration-500 hover:shadow-xl"
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
  );
}
