type ArtistStat = {
  label: string;
  value: string;
};

type TrackRow = {
  title: string;
  meta: string;
  duration: string;
};

type RelatedArtist = {
  name: string;
  role: string;
  image: string;
};

const artistPortrait =
  "https://lh3.googleusercontent.com/aida-public/AB6AXuAF5WgzuaEUZj4f-e-PngyibHGJTzBu5VdpLC7ajzZ4ds-xQ6_HN7ieEN_EziiFGxNDO-PQrcF5tqN1i_Dlub0LkGYVgK_4X2KClWQHylJbKUofwPLvUJa149HBp1ES3Y7jxYt421ApCRo37wzBR1Zo3olO4aA1DggH6kN1nN4sZvXigDsEO_ddXCJ4dnwLonRmawQTEs4NP5HoIMnHhlWYgma9ak5OpNahlZcH_jwI4gW7bXZxZ7DpKceWdoW3TEznW0PuoiBkyOO2";

const stats: ArtistStat[] = [
  { value: "۴۸", label: "گلهای تازه" },
  { value: "۶۲", label: "گلهای رنگارنگ" },
  { value: "۱۰", label: "یک شاخه گل" },
];

const tracks: TrackRow[] = [
  { title: "گلهای تازه، شماره ۲۵ - آواز شور", meta: "برنامه گلهای تازه • فرامرز پایور", duration: "۴۲:۱۵" },
  { title: "یک شاخه گل، شماره ۴۰۲", meta: "برنامه یک شاخه گل • اسدالله ملک", duration: "۲۸:۴۰" },
  { title: "گلهای رنگارنگ، شماره ۵۸۰", meta: "برنامه گلهای رنگارنگ • جلیل شهناز", duration: "۳۵:۱۰" },
  { title: "گلهای تازه، شماره ۱۰ - ماهور", meta: "برنامه گلهای تازه • پرویز یاحقی", duration: "۳۸:۲۰" },
  { title: "یک شاخه گل، شماره ۴۱۲", meta: "برنامه یک شاخه گل • فرهنگ شریف", duration: "۲۵:۱۵" },
];

const relatedArtists: RelatedArtist[] = [
  {
    name: "جلیل شهناز",
    role: "استاد تار",
    image:
      "https://lh3.googleusercontent.com/aida-public/AB6AXuApBy3h3tvM_8zoiSOpKAVBrvz18FtfEruo4fravG9v7kvVDIlDeXPVl2IMscU_y3AXaJH7LGNBM8wDlyaktALxCAoepY-Ediyj-xmtkaodZSZ8fK_bJ3SExfRz7J9jnaiKsvJ1JnaG7MuHLrFWCwGbqQbp6aJ3AcdJZmRkZZMXKGixlZGRDlWxf7Pku3ItvZt5s-g7Aqomo3jTf_U0GaTnhfvpAIVhvXVGjgXMAHxkOEAvOP012-CH9tC33P2RCbOLtqbbYjsgiGXt",
  },
  {
    name: "فرامرز پایور",
    role: "استاد سنتور",
    image:
      "https://lh3.googleusercontent.com/aida-public/AB6AXuCsgRdW1B1M3YHnSlBE06hqQ5_l9p20aV1JucoLFUnGdzEPkGVtcQr16mCinUSNpV0_pQ0Xk-DbM0AHf_Cn3qxniTZ6tOQ0Sbt0TACcjITHEDCOcPuPZK3y1zftcfs9ZtoNTdm9TPn_2gzxC7M8rGaor3l-J8gKrvg9WlF0GnClqtWJOwW0rYuEvuUjZ18Z5xY7yZVLJozpjHXtFzN_JsFwHCoZF7JoVm3St5JZFLwoYa7Ny-OS9eohFbhIj_qYQDyMtXZa-Z5M5NIn",
  },
  {
    name: "حسن کسایی",
    role: "استاد نی",
    image:
      "https://lh3.googleusercontent.com/aida-public/AB6AXuAn8m90D_5A_M4oM7JvP1S-A_0H9s9YJp8m_9V-0W_K_K_L_G_H_I_J_K_L_M_N_O_P_Q_R_S_T_U_V_W_X_Y_Z",
  },
  {
    name: "اسدالله ملک",
    role: "استاد ویولن",
    image:
      "https://lh3.googleusercontent.com/aida-public/AB6AXuB12PkP3YeTLfT9n1_14FZsWkNXHZ6wh8QJKo-HiIww-UqBytZNgmhz5VZ5gPhhFKEzj2DME1rWk9MCpJWg7_bP4HRSCM0Uj_17p_oOEV7e6-I0qFoGkusoP_pxxp5cNZMgbPyBxQ8lkSmutqSOxckrCw3BwUGr0kU7ZJMVSD9ORFYJS6EfgNIKhYenVlgBRglV0cjWoN-8cFd3bLi_yKkSGOR-C-Xt9muu87oaOhQDAS_2douYADV2uRLnJ6jyAlV8QG2G7SFgHLvy",
  },
];

const modes = ["شور", "افشاری", "همایون", "بیات ترک", "سه‌گاه"];

function RelatedArtistCard({ artist }: { artist: RelatedArtist }) {
  return (
    <button className="group overflow-hidden rounded-xl bg-surface-container-lowest text-right shadow-sm transition-all duration-300 hover:-translate-y-1 hover:shadow-xl">
      <div className="aspect-square overflow-hidden">
        <img
          alt={artist.name}
          className="h-full w-full object-cover grayscale transition-all duration-500 group-hover:scale-105 group-hover:grayscale-0"
          src={artist.image}
        />
      </div>
      <div className="p-3">
        <h3 className="truncate text-sm font-black text-primary">{artist.name}</h3>
        <p className="mt-1 truncate text-[11px] font-bold text-on-surface-variant">{artist.role}</p>
      </div>
    </button>
  );
}

export function ArtistDetailsPage() {
  return (
    <div className="pb-32 text-right">
      <section className="grid grid-cols-12 items-end gap-12 px-12 py-8">
        <div className="group relative col-span-4 aspect-square overflow-hidden rounded-lg">
          <img
            alt="محمدرضا شجریان"
            className="h-full w-full object-cover grayscale transition-all duration-700 group-hover:grayscale-0"
            src={artistPortrait}
          />
          <div className="absolute inset-0 bg-gradient-to-t from-primary/80 to-transparent" />
        </div>

        <div className="col-span-8 flex h-full flex-col justify-end pb-8">
          <div className="mb-10 max-w-2xl">
            <h1 className="mb-6 text-6xl font-black tracking-[-0.06em] text-secondary">محمدرضا شجریان</h1>
            <button className="inline-flex items-center gap-2 rounded-full bg-secondary px-6 py-2 text-sm font-black text-white transition-all hover:bg-on-secondary-container">
              <span className="material-symbols-outlined text-lg">favorite</span>
              <span>افزودن به علاقه‌مندی‌ها</span>
            </button>
          </div>

          <div className="flex justify-start gap-12">
            {stats.map((stat) => (
              <div key={stat.label} className="border-r-2 border-secondary/30 pr-4 first:border-r-0 first:pr-0">
                <p className="text-4xl font-black tracking-[-0.04em] text-primary">{stat.value}</p>
                <p className="mt-1 text-sm font-bold text-on-surface-variant">{stat.label}</p>
              </div>
            ))}
          </div>
        </div>
      </section>

      <section className="grid grid-cols-12 gap-8 px-12 py-12">
        <main className="col-span-9">
          <div className="mb-6 flex items-end justify-between">
            <h2 className="text-3xl font-black text-primary">برنامه‌ها</h2>
            <span className="text-xs font-bold text-secondary">۵ برنامه منتخب</span>
          </div>

          <div className="overflow-hidden rounded-2xl border border-outline-variant/20 bg-surface-container-low shadow-sm">
            {tracks.map((track, index) => (
              <button
                key={track.title}
                className="group grid w-full grid-cols-[1fr_auto] items-center gap-6 border-b border-outline-variant/20 px-6 py-5 text-right transition-colors last:border-b-0 hover:bg-surface-container"
              >
                <span>
                  <span className="block text-lg font-black text-primary transition-colors group-hover:text-secondary">{track.title}</span>
                  <span className="mt-1 block text-sm font-bold text-on-surface-variant">{track.meta}</span>
                </span>
                <span className="flex items-center gap-4">
                  <span className="text-sm font-bold text-on-surface-variant">{track.duration}</span>
                  <span className="grid h-10 w-10 place-items-center rounded-full bg-secondary-container text-on-secondary-container transition-colors group-hover:bg-secondary group-hover:text-white">
                    <span className="material-symbols-outlined text-[20px]">{index === 0 ? "pause" : "play_arrow"}</span>
                  </span>
                </span>
              </button>
            ))}
          </div>
        </main>

        <aside className="col-span-3 space-y-10">
          <section>
            <h2 className="mb-4 text-2xl font-black text-primary">همکاران</h2>
            <div className="grid grid-cols-2 gap-4">
              {relatedArtists.map((artist) => (
                <RelatedArtistCard key={artist.name} artist={artist} />
              ))}
            </div>
          </section>

          <section>
            <h2 className="mb-4 text-2xl font-black text-primary">دستگاه‌های شاخص</h2>
            <div className="flex flex-wrap justify-end gap-3">
              {modes.map((mode) => (
                <span key={mode} className="rounded-full bg-secondary-container px-4 py-2 text-sm font-black text-on-secondary-container">
                  {mode}
                </span>
              ))}
            </div>
          </section>
        </aside>
      </section>
    </div>
  );
}
