import { Link } from "@tanstack/react-router";

type PlayerCard = {
  name: string;
  instrument: string;
  count: string;
  image: string;
  alt: string;
};

const instruments = ["همه", "تار", "سه تار", "سنتور", "کمانچه", "ویلن", "نی", "تنبک"];

const players: PlayerCard[] = [
  {
    name: "جلیل شهناز",
    instrument: "تار",
    count: "۸۹ برنامه ثبت شده",
    alt: "جلیل شهناز",
    image:
      "https://lh3.googleusercontent.com/aida-public/AB6AXuDpmECMtqxU9603efb-gprCyzSy0zv_Jb2bOlP-25OY9BKroEEJmvLMUbd11QHZuv4U1LbXVl-hRP4gEe17I-8-j1usqc5lWbE_Ywm3sGBpgcNTzwMAFtRGeEOi2mr5-OcaCt_u_NIAvJA35OKX4e1-oY1bgDrwQICxdEK26U-ec_oKxY4A5gQRVW5pxvQlP3M5bPCZMvMxmABAZbJHKKgWfcRjrICr2o5ZLdHczvQnhncxdDslWRnO5Xzi4wbySQXpux8rC_neZMSJ",
  },
  {
    name: "فرهنگ شریف",
    instrument: "تار",
    count: "۶۷ برنامه ثبت شده",
    alt: "فرهنگ شریف",
    image:
      "https://lh3.googleusercontent.com/aida-public/AB6AXuAuJhidSr6FLd6UzCY8dVJvxLJL8b7K9DeTaa5wEoT_LyTJYxxl4beINxqu4g5Tfeh4SEEWmfkr09vFPbmxxGz47i8Fw1AWR8A-cKW3hjaStKWzCGzOW7sbg_KdPFp6J3wR02FVXrND9bkcIlk-_istTxmeadQclStqcbWeoZ1pRfevRBlmnAz9J-3OPEymq8ZQ-8ccnceMiMGHcwezZercttJJns56so948APCW4jKxr0FpCvIaqm1HuioJTBr1KaxC6ErckapJXHq",
  },
  {
    name: "محمدرضا لطفی",
    instrument: "تار و سه تار",
    count: "۴۵ برنامه ثبت شده",
    alt: "محمدرضا لطفی",
    image:
      "https://lh3.googleusercontent.com/aida-public/AB6AXuCxRzn7wWlNERIy2Jv9mKKDC0dmCOipXaXN-UNYvYjKO1n5_mCy1D4OgywBuB7gCN_F9wUkQSt0qk1sy-uRu5FdsQngSMOacrg3dp-qO1yy6BrFLpzouqYAsMmfQgFgnj4f7tA56I0tZ0dbcerZvw3-aDwrNC2e7ov9gcvNTslVQJfQBRMl9Ct-WRxIVTrVRpB3eDpyeNufQoz7lb7O96dh_LYgEXZExFZLTW8VitB2adWpfwIwOWetkmdxyoeTyw96342odLIrVF-_",
  },
  {
    name: "بیگجه‌خانی",
    instrument: "تار",
    count: "۳۸ برنامه ثبت شده",
    alt: "بیگجه‌خانی",
    image:
      "https://lh3.googleusercontent.com/aida-public/AB6AXuC0RRKQQz6esXbZFZL_s8XaEPC62wOsuxOu1GV7TGS-Ue_a0yrKv2lc2mY-ydmSaqGjfzelXpw1s2V8fatmaLKX3dIpTdI7Pj1RnwY1jvDYmTAriVBe8X8HR5Hg9UAJvAkaPr8SxPAuSmpcvinw3iFfZMgRxBUKmlNcPeDAA5dkYiOHHgGuXiEyijDF4QrQm7SvtVD1kMQoEIGygsDQmaqvBoie9NwTrlPhB5isNvJU44-v9eJTIO9XcPoqb9-mt3eS0Ytt7i-G0Tb",
  },
  {
    name: "فریدون حافظی",
    instrument: "تار",
    count: "۵۲ برنامه ثبت شده",
    alt: "فریدون حافظی",
    image:
      "https://lh3.googleusercontent.com/aida-public/AB6AXuD6py-_wC3zsPTTjQhvKRiJr2GIrfIXgeTa8aVPbDlqXhVd3f5A7P3DfflRvUBjA9nVIGoPkJdMlxfxUjESyj1BLwunFbQV3Wb8K8DG1oJJa0G0XVFbQshzKci_8VlwHD7z2DshxSFZyapXovd4jVx4OqQC4mwLiOTT_4f5SwE5j5pI9vsxsdgU1xCtbbBM1O6Zq_lk7-BZg9OcWgjstlokkXDLghrq16qpOPeMm3uUmcu6EbMUJYUIS5cIFRuX_NeNx6JcK2x7Bl2x",
  },
  {
    name: "لطف‌الله مجد",
    instrument: "تار",
    count: "۸۴ برنامه ثبت شده",
    alt: "لطف‌الله مجد",
    image:
      "https://lh3.googleusercontent.com/aida-public/AB6AXuDHFMfjabhNXj3wncSaY5MSF9BuFEl_4tHrE-vbyf8vXkYKuE8cL0yTrG8R1t1b4P__Ga05ZCYgxDhALxbWdpe3GH4ce8xhfEVMYz3PYzytjoaXQ5R1TJDWI2J-MJ0Wl752vvAUwrkGgx0MyUX90ZYuFCVCVFy8-qF5_wYh63ejryMt5FkP24Z4SlBjD9V3soIXFIG1XtHSPbb0Sjjm7SwoKUCj_wwi7jty2eZb-Ox9ypnhcYV82twvLgymhLGXZFtrO23aOXlnDUu7",
  },
];

function PlayerCardView({ player }: { player: PlayerCard }) {
  return (
    <Link
      to="/artists/$artistId"
      params={{ artistId: player.name }}
      className="group block overflow-hidden rounded-xl bg-surface-container-lowest shadow-sm transition-all duration-300 hover:-translate-y-1 hover:shadow-xl"
    >
      <div className="relative aspect-square overflow-hidden">
        <img
          alt={player.alt}
          className="h-full w-full object-cover grayscale transition-all duration-500 group-hover:scale-105 group-hover:grayscale-0"
          src={player.image}
        />
        <div className="absolute inset-0 flex items-end bg-gradient-to-t from-primary/60 via-transparent to-transparent p-6 opacity-0 transition-opacity group-hover:opacity-100">
          <span className="rounded-full border border-white/40 px-3 py-1 text-xs font-bold text-white">مشاهده آثار</span>
        </div>
      </div>
      <div className="p-5 text-right">
        <h3 className="mb-1 text-xl font-bold text-primary">{player.name}</h3>
        <p className="mb-3 text-sm font-bold text-secondary">{player.instrument}</p>
        <div className="flex items-center gap-2 text-xs text-on-surface-variant">
          <span className="material-symbols-outlined text-[16px]">library_music</span>
          <span>{player.count}</span>
        </div>
      </div>
    </Link>
  );
}

export function PlayersPage() {
  return (
    <div className="p-12 pb-32">
      <div className="mb-10 text-right">
        <h1 className="mb-3 text-4xl font-bold text-primary">نوازندگان</h1>
        <p className="max-w-2xl text-right leading-relaxed text-on-surface-variant">
          فهرست مشاهیر موسیقی اصیل ایرانی و نوازندگان برجسته برنامه‌های گلها به تفکیک تخصص و ساز.
        </p>
      </div>

      <div className="mb-12 flex flex-wrap items-center justify-end gap-3">
        {instruments.map((instrument) => (
          <button
            key={instrument}
            className={
              instrument === "تار"
                ? "rounded-full bg-secondary px-6 py-2 text-sm font-bold text-white shadow-lg shadow-secondary/20"
                : "rounded-full bg-surface-container-high px-6 py-2 text-sm font-bold text-on-surface-variant transition-all hover:bg-secondary hover:text-white"
            }
          >
            {instrument}
          </button>
        ))}
      </div>

      <div className="grid grid-cols-1 gap-8 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4">
        {players.map((player) => (
          <PlayerCardView key={player.name} player={player} />
        ))}
      </div>
    </div>
  );
}
