type ProgramCard = {
  title: string;
  count: string;
  icon: string;
};

type ArtistCard = {
  name: string;
  role: string;
  image: string;
  alt: string;
};

type TrackRow = {
  title: string;
  subtitle: string;
  duration: string;
  rounded?: boolean;
};

const heroImage =
  "https://lh3.googleusercontent.com/aida-public/AB6AXuBTRoCtbLy1Vpa3t_ez8WfRkhOFnnCGOnbhRCJ3Tw_GbsQa8OqeyyLL2ov1DPWrduyIkRYbX-OfQkwuqlVraQ8QJOLKS5xz0nnGbm6Xcew6EaIxSXymeWEKEzkuhnl0xcQXO5V7KIbFs1M5iwZVA0GNgsIljnkjQYe9AdbIOmQEm8ohOVd39E_qi-b-b39xQ0PVaqyEtPk83DXRnERRtsx7Xs_6iidmtYtqJkpETR82f97iPOnF3stP0rFiR0INpoCfMwIZfPGvTTV3";

const images = {
  shajarian:
    "https://lh3.googleusercontent.com/aida-public/AB6AXuBQlX0v-Lh1Lj6CxDQYOd-xCKyNArhNLKOXT1e6WR3j251g0iUPqfUXkoPd4gjIMgt1QeRMHZDCbQeXgk7e6hsvtIN7AJzWyjXu8BRxJdWLB1WDSgoyYfG2_3oT6a9fFzoQtYdeZe430yfJn_MdqX-yfmEECMgIIn8tLeQLf8RO78T8vzpr-c1wSqYYgXEUGfN4KzDYQ7vH5P4PMUb9Hd7ufSqPmAeJmC59z2G7fYemqLFnCjTVpN0pHuR295sHnCYO1LAERIc-5CI3",
  banan:
    "https://lh3.googleusercontent.com/aida-public/AB6AXuCdxuO-btnXsk1jNpqhy440lM0cy98lRfDi1JAptVTgEX2Fsd20qzP5qdPm4Uyu9rzx-xCJKzIIKMklM9eum8R1VtG4twX-3SPLQQUBnnXlPCetNsBS9VQc3tYQgP5sjejwOy4-lmGLkfbTz8936tpKD_B0OJPamCaYwMrOV8wuT0xTtpzprbbol2QJem2cYn7aRphjkJD3NvDSv2m_WSAYqqSfcL9czHeJl__fvDr074p20jojBew89tr1330zt2CEnUGhkGTXOAdO",
  delkash:
    "https://lh3.googleusercontent.com/aida-public/AB6AXuA2hVZWR2Z0j1a7SSEmfnLEm3SrIidS3j2EnFElicEuVkB32-M3iGqpEBw-59fsUzo6mazQq9Cw5_uhIc6q0Rcdp6R3-3JH6LDovVJpYKoD2OZgVp--I5kblaeCvWspbFKH_5Z31YKu4Jcn8VxJjtibB9JWPWf8dBh-hS--B2f_2L6VGqfVeAQhyaOYc4w5Ybd-9Q_-p0ZRKje4L0BeAxIdyCesXx1A73fXKJdowlExvTeMsSaXPGNKXqiRfR9qy7W8P0EFR629xeny",
  marzieh:
    "https://lh3.googleusercontent.com/aida-public/AB6AXuBySGVnBqhZRyJ-vngmyf-Hd5PQndePKpM0WCl__KpAYTrn0dnSIk9Zjgs-KnsmYUH_ysqpJT1nsIvWCyWYvBv-h1wnzw8n8uLbpN4EzN-J_1IZrcc4ug0Vg-m6fLZnssUMUGYpoB2ZUD7gokhNIF0CyOK4QWM8FX_n-W84HUim4eL-mi0deyVurRese2PR0YMglqNqAZgAaSOubIteZa9EM-CCZeS5IeGJqAURy2yjBe1y-oxNfjXddDA2AHAxlHuzkGgl69d2ANtM",
  sharif:
    "https://lh3.googleusercontent.com/aida-public/AB6AXuBGekEUXBcApNWh17_qhvE5zV19t7t5ysca3oZ9FEtdwVsgnWNzPE4paUYPvmlUTxB_uvpchE7NsLDop42Z2XcKwL3KuNH7xPuKmzxrW9WppCT6K3Ym293tsAh_vm9OS8Hzx6kAiAndqgLmOu222Rv2-SDcDfZmurPQhtjei3LmDPjmABFjBKvYHUWqvHv4YSbsYVrFKSdnEa6IlEZO0z3ZeNFnHgtk_e9PVNz6AVOaBCZJTcau2o2nG16iErn20SyVh-RV53NW5hsg",
};

const programs: ProgramCard[] = [
  { title: "گل‌های تازه", count: "۱۵۵ برنامه", icon: "filter_vintage" },
  { title: "برگ سبز", count: "۳۱۲ برنامه", icon: "eco" },
  { title: "یک شاخه گل", count: "۴۶۵ برنامه", icon: "local_florist" },
  { title: "گلهای جاویدان", count: "۱۰۱ برنامه", icon: "auto_awesome" },
];

const singers: ArtistCard[] = [
  { name: "محمدرضا شجریان", role: "خواننده", image: images.shajarian, alt: "استاد محمدرضا شجریان" },
  { name: "غلامحسین بنان", role: "خواننده", image: images.banan, alt: "استاد غلامحسین بنان" },
  { name: "بانو دلکش", role: "خواننده", image: images.delkash, alt: "بانو دلکش" },
  { name: "بانو مرضیه", role: "خواننده", image: images.marzieh, alt: "بانو مرضیه" },
];

const players: ArtistCard[] = [
  { name: "جلیل شهناز", role: "نوازنده تار", image: images.delkash, alt: "استاد جلیل شهناز" },
  { name: "حسن کسایی", role: "نوازنده نی", image: images.marzieh, alt: "استاد حسن کسایی" },
  { name: "فرهنگ شریف", role: "نوازنده تار", image: images.sharif, alt: "استاد فرهنگ شریف" },
  { name: "حبیب‌الله بدیعی", role: "نوازنده ویولن", image: images.marzieh, alt: "استاد حبیب‌الله بدیعی" },
];

const modes = ["شور", "ماهور", "همایون", "اصفهان", "سه‌گاه", "چهارگاه", "راست‌پنجگاه", "نوا", "ابوعطا", "دشتی"];

const topPrograms: TrackRow[] = [
  { title: "گلهای تازه شماره ۱۲۰", subtitle: "استاد شجریان - فرهنگ شریف", duration: "۱۵:۲۰", rounded: true },
  { title: "تکنوازان شماره ۲۵", subtitle: "جلیل شهناز - ناصر فرهنگ‌فر", duration: "۱۲:۴۵", rounded: true },
];

const recentTracks: TrackRow[] = [
  { title: "آستان جانان", subtitle: "آواز شور", duration: "۰۴:۱۵" },
  { title: "کاروان", subtitle: "غلامحسین بنان", duration: "۰۶:۳۰" },
];

function SectionHeader({ title, showAll = true, refresh = false }: { title: string; showAll?: boolean; refresh?: boolean }) {
  return (
    <div className="mb-8 flex items-end justify-between">
      <h3 className="text-3xl font-bold text-primary">{title}</h3>
      {refresh ? (
        <button className="text-secondary transition-transform duration-500 hover:rotate-180">
          <span className="material-symbols-outlined">refresh</span>
        </button>
      ) : showAll ? (
        <a className="flex items-center gap-1 font-medium text-secondary transition-all hover:gap-2" href="#">
          مشاهده همه
          <span className="material-symbols-outlined text-sm">arrow_back</span>
        </a>
      ) : null}
    </div>
  );
}

function ProgramCard({ program }: { program: ProgramCard }) {
  return (
    <div className="group flex cursor-pointer items-center gap-4 rounded-2xl border border-outline-variant/20 bg-surface-container-low p-4 transition-all hover:bg-white hover:shadow-xl hover:shadow-primary/5">
      <div className="flex h-16 w-16 items-center justify-center rounded-xl bg-primary/5 text-secondary transition-colors group-hover:bg-secondary group-hover:text-white">
        <span className="material-symbols-outlined text-3xl">{program.icon}</span>
      </div>
      <div>
        <h4 className="text-lg font-bold text-primary">{program.title}</h4>
        <p className="text-xs text-stone-500">{program.count}</p>
      </div>
    </div>
  );
}

function ArtistCard({ artist }: { artist: ArtistCard }) {
  return (
    <div className="group relative overflow-hidden rounded-xl border border-outline-variant/10 bg-surface-container-lowest transition-all duration-500 hover:shadow-xl">
      <div className="relative aspect-square overflow-hidden">
        <img
          alt={artist.alt}
          className="h-full w-full object-cover grayscale transition-all duration-700 group-hover:scale-105 group-hover:grayscale-0"
          src={artist.image}
        />
        <div className="absolute inset-0 flex items-end bg-gradient-to-t from-primary/80 via-transparent to-transparent p-6 opacity-0 transition-opacity duration-500 group-hover:opacity-100">
          <button className="w-full rounded-full bg-secondary-container px-6 py-2 text-xs font-bold text-on-secondary-container">مشاهده آثار</button>
        </div>
      </div>
      <div className="p-5 text-right">
        <h3 className="mb-1 text-xl font-bold text-primary">{artist.name}</h3>
        <p className="text-sm font-medium text-on-surface-variant">{artist.role}</p>
      </div>
    </div>
  );
}

function TrackList({ rows, squarePlay = false }: { rows: TrackRow[]; squarePlay?: boolean }) {
  return (
    <div className="overflow-hidden rounded-2xl border border-outline-variant/20 bg-surface-container-low shadow-sm">
      <div className="divide-y divide-on-surface/5">
        {rows.map((row) => (
          <div key={row.title} className="group flex items-center gap-4 p-4 transition-colors hover:bg-white/50">
            <div
              className={
                squarePlay
                  ? "flex h-10 w-10 cursor-pointer items-center justify-center rounded-lg bg-primary/5 text-secondary transition-all group-hover:bg-secondary group-hover:text-white"
                  : "flex h-10 w-10 cursor-pointer items-center justify-center rounded-full bg-primary/5 text-primary transition-all group-hover:bg-secondary group-hover:text-white"
              }
            >
              <span className="material-symbols-outlined text-xl" style={{ fontVariationSettings: "'FILL' 1" }}>play_arrow</span>
            </div>
            <div className="flex-1">
              <h5 className="text-sm font-bold text-primary">{row.title}</h5>
              <p className="mt-0.5 text-xs text-stone-500">{row.subtitle}</p>
            </div>
            <div className="text-xs font-medium text-stone-400">{row.duration}</div>
          </div>
        ))}
      </div>
    </div>
  );
}

export function HomePage() {
  return (
    <>
      <section className="px-12 py-8">
        <div className="group relative h-[320px] overflow-hidden rounded-3xl shadow-2xl shadow-primary/10">
          <img
            alt="نمای نزدیک از ساز سنتی ایران"
            className="h-full w-full object-cover transition-transform duration-700 group-hover:scale-105"
            src={heroImage}
          />
          <div className="absolute inset-0 flex items-center bg-gradient-to-l from-primary/90 via-primary/40 to-transparent pr-16">
            <div className="max-w-xl text-white">
              <span className="mb-4 inline-block rounded bg-secondary/80 px-3 py-1 text-xs font-bold tracking-widest">برنامه ویژه</span>
              <h2 className="mb-6 text-4xl font-bold leading-tight">گلهای رنگارنگ</h2>
              <p className="mb-4 text-lg leading-relaxed text-white/80">
                بشنوید آثاری جاودانه از استاد محمدرضا شجریان و غلامحسین بنان در مجموعه‌ای بی‌نظیر که روح هر شنونده‌ای را جلا می‌دهد.
              </p>
              <div className="flex gap-4">
                <button className="flex items-center gap-2 rounded-full bg-secondary px-8 py-3 font-bold text-on-secondary transition-colors hover:bg-secondary-container">
                  <span className="material-symbols-outlined" style={{ fontVariationSettings: "'FILL' 1" }}>play_arrow</span>
                  پخش آخرین قسمت
                </button>
                <button className="rounded-full border border-white/20 bg-white/10 px-8 py-3 font-bold text-white backdrop-blur-md transition-colors hover:bg-white/20">
                  مشاهده لیست پخش
                </button>
              </div>
            </div>
          </div>
        </div>
      </section>

      <section className="px-12 py-12">
        <SectionHeader title="برنامه‌ها" />
        <div className="grid grid-cols-4 gap-6">
          {programs.map((program) => <ProgramCard key={program.title} program={program} />)}
        </div>
      </section>

      <section className="px-12 py-12">
        <SectionHeader title="خوانندگان برجسته" />
        <div className="grid grid-cols-4 gap-8">
          {singers.map((artist) => <ArtistCard key={artist.name} artist={artist} />)}
        </div>
      </section>

      <section className="px-12 py-8">
        <h3 className="mb-6 text-2xl font-bold text-primary">دستگاه‌ها و آوازها</h3>
        <div className="no-scrollbar flex gap-4 overflow-x-auto scroll-smooth pb-4">
          {modes.map((mode) => (
            <a
              key={mode}
              className="flex-none rounded-full border border-outline-variant/30 bg-surface-container-high px-8 py-3 font-medium text-on-surface shadow-sm transition-all hover:bg-secondary-container hover:text-on-secondary-container"
              href="#"
            >
              {mode}
            </a>
          ))}
        </div>
      </section>

      <section className="px-12 py-12">
        <SectionHeader title="نوازندگان برجسته" />
        <div className="grid grid-cols-4 gap-8">
          {players.map((artist) => <ArtistCard key={artist.name} artist={artist} />)}
        </div>
      </section>

      <section className="grid grid-cols-12 gap-12 px-12 py-12">
        <div className="col-span-6">
          <SectionHeader title="برترین برنامه‌ها" showAll={false} refresh />
          <div className="space-y-6">
            <TrackList rows={topPrograms} />
          </div>
        </div>
        <div className="col-span-6">
          <SectionHeader title="شنیده شده‌های اخیر" showAll={false} />
          <TrackList rows={recentTracks} squarePlay />
        </div>
      </section>
    </>
  );
}
