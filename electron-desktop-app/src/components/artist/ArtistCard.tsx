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
  return (
    <Link
      to="/artists/$artistId"
      params={{ artistId: String(artist.id ?? artist.name) }}
      className="group relative block overflow-hidden rounded-xl border border-outline-variant/10 bg-surface-container-lowest transition-all duration-500 hover:shadow-xl"
    >
      <div className="relative aspect-square overflow-hidden">
        <img
          alt={artist.alt ?? artist.name}
          className="h-full w-full object-cover grayscale transition-all duration-700 group-hover:scale-105 group-hover:grayscale-0"
          src={artist.image}
        />
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
