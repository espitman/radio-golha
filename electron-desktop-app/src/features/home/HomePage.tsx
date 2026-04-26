import { Link } from "@tanstack/react-router";
import { useEffect, useState } from "react";
import { ArtistCard } from "../../components/artist/ArtistCard";
import { HomeSkeleton } from "../../components/skeleton/Skeletons";
import { TrackList, type TrackRowData } from "../../components/track/TrackRow";
import { getHomeData, getTopTracks, type CoreHomePayload } from "../../lib/coreApi";
import { readRecentTracks, RECENT_TRACKS_CHANGED_EVENT } from "../../lib/recentTracks";

type ProgramCardData = {
  id: number;
  title: string;
  count: string;
  icon: string;
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

const programs: ProgramCardData[] = [
  { id: 1, title: "گل‌های تازه", count: "۱۵۵ برنامه", icon: "filter_vintage" },
  { id: 2, title: "برگ سبز", count: "۳۱۲ برنامه", icon: "eco" },
  { id: 3, title: "یک شاخه گل", count: "۴۶۵ برنامه", icon: "local_florist" },
  { id: 4, title: "گلهای جاویدان", count: "۱۰۱ برنامه", icon: "auto_awesome" },
];

type HomeShowAllRoute = "/archive" | "/singers" | "/players" | "/recent" | "/popular";

function SectionHeader({
  title,
  showAllTo,
  refresh = false,
  onRefresh,
  isRefreshing = false,
}: {
  title: string;
  showAllTo?: HomeShowAllRoute;
  refresh?: boolean;
  onRefresh?: () => void;
  isRefreshing?: boolean;
}) {
  return (
    <div className="mb-8 flex items-end justify-between">
      <h3 className="text-3xl font-bold text-primary">{title}</h3>
      {refresh ? (
        <button
          className="text-secondary transition-transform duration-500 hover:rotate-180 disabled:cursor-default disabled:opacity-50"
          onClick={onRefresh}
          disabled={isRefreshing}
          type="button"
        >
          <span className="material-symbols-outlined">refresh</span>
        </button>
      ) : showAllTo ? (
        <Link className="flex items-center gap-1 font-medium text-secondary transition-all hover:gap-2" to={showAllTo}>
          مشاهده همه
          <span className="material-symbols-outlined text-sm">arrow_back</span>
        </Link>
      ) : null}
    </div>
  );
}

function ProgramCard({ program }: { program: ProgramCardData }) {
  return (
    <Link
      to="/program-categories/$categoryId"
      params={{ categoryId: String(program.id) }}
      className="group flex cursor-pointer items-center gap-4 rounded-2xl border border-outline-variant/20 bg-surface-container-low p-4 transition-all hover:bg-white hover:shadow-xl hover:shadow-primary/5"
    >
      <div className="flex h-16 w-16 items-center justify-center rounded-xl bg-primary/5 text-secondary transition-colors group-hover:bg-secondary group-hover:text-white">
        <span className="material-symbols-outlined text-3xl">{program.icon}</span>
      </div>
      <div>
        <h4 className="text-lg font-bold text-primary">{program.title}</h4>
        <p className="text-xs text-stone-500">{program.count}</p>
      </div>
    </Link>
  );
}



export function HomePage() {
  const [homeData, setHomeData] = useState<CoreHomePayload | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [recentTracks, setRecentTracks] = useState<TrackRowData[]>([]);
  const [isRefreshingTopTracks, setIsRefreshingTopTracks] = useState(false);

  useEffect(() => {
    let isMounted = true;
    getHomeData()
      .then((payload) => {
        if (isMounted) setHomeData(payload);
      })
      .catch((reason) => {
        if (isMounted) setError(String(reason));
      });
    return () => {
      isMounted = false;
    };
  }, []);

  function handleRefreshTopTracks() {
    if (isRefreshingTopTracks) return;
    setIsRefreshingTopTracks(true);
    getTopTracks(5)
      .then((topTracks) => {
        setHomeData((current) => (current ? { ...current, topTracks } : current));
      })
      .finally(() => {
        setIsRefreshingTopTracks(false);
      });
  }

  useEffect(() => {
    const refresh = () => {
      setRecentTracks(
        readRecentTracks(5).map((track) => ({
          id: track.id,
          title: track.title,
          subtitle: track.subtitle,
          duration: track.duration,
          audioUrl: track.audioUrl,
          artworkUrls: track.artworkUrls,
        })),
      );
    };

    refresh();
    window.addEventListener(RECENT_TRACKS_CHANGED_EVENT, refresh);
    window.addEventListener("storage", refresh);
    return () => {
      window.removeEventListener(RECENT_TRACKS_CHANGED_EVENT, refresh);
      window.removeEventListener("storage", refresh);
    };
  }, []);

  if (error) {
    return <div className="mx-auto max-w-5xl px-12 py-12 text-right text-sm font-bold text-on-error-container">{error}</div>;
  }

  if (!homeData) {
    return <HomeSkeleton />;
  }

  const dynamicPrograms = homeData.programs.slice(0, 4).map((program, index) => ({
    id: program.id,
    title: program.title,
    count: `${program.episodeCount.toLocaleString("fa-IR")} برنامه`,
    icon: programs[index]?.icon ?? "radio",
  }));
  const heroCategory = homeData.programs.find((program) => program.title.includes("رنگارنگ")) ?? homeData.programs[0];
  const dynamicSingers = homeData.singers.slice(0, 4).map((artist) => ({
    id: artist.id,
    name: artist.name,
    role: `${artist.programCount.toLocaleString("fa-IR")} برنامه`,
    image: artist.avatar || images.shajarian,
    alt: artist.name,
  }));
  const dynamicPlayers = homeData.musicians.slice(0, 4).map((artist) => ({
    id: artist.id,
    name: artist.name,
    role: artist.instrument,
    image: artist.avatar || images.sharif,
    alt: artist.name,
  }));
  const dynamicModes = homeData.modes.map((mode) => ({ id: mode.id, name: mode.name }));
  const dynamicTopTracks = homeData.topTracks.slice(0, 5).map((track) => ({
    id: track.id,
    title: track.title,
    subtitle: track.artist,
    duration: track.duration,
    audioUrl: track.audioUrl,
    artworkUrls: track.singerAvatars,
  }));

  return (
    <div className="pb-[96px]">
      <section className="mx-auto max-w-5xl px-12 py-8">
        <Link
          to="/program-categories/$categoryId"
          params={{ categoryId: String(heroCategory?.id ?? 1) }}
          className="group relative block h-[320px] overflow-hidden rounded-3xl shadow-2xl shadow-primary/10"
        >
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
                <span className="flex items-center gap-2 rounded-full bg-secondary px-8 py-3 font-bold text-on-secondary transition-colors group-hover:bg-secondary-container">
                  <span className="material-symbols-outlined" style={{ fontVariationSettings: "'FILL' 1" }}>play_arrow</span>
                  پخش آخرین قسمت
                </span>
                <span className="rounded-full border border-white/20 bg-white/10 px-8 py-3 font-bold text-white backdrop-blur-md transition-colors group-hover:bg-white/20">
                  مشاهده لیست پخش
                </span>
              </div>
            </div>
          </div>
        </Link>
      </section>

      <section className="mx-auto max-w-5xl px-12 py-12">
        <SectionHeader title="برنامه‌ها" />
        <div className="grid grid-cols-4 gap-6">
          {dynamicPrograms.map((program) => <ProgramCard key={program.title} program={program} />)}
        </div>
      </section>

      <section className="mx-auto max-w-5xl px-12 py-12">
        <SectionHeader title="خوانندگان برجسته" showAllTo="/singers" />
        <div className="grid grid-cols-4 gap-8">
          {dynamicSingers.map((artist) => <ArtistCard key={artist.name} artist={{ id: artist.id, name: artist.name, subtitle: artist.role, image: artist.image, alt: artist.alt, favoriteKind: "singer" }} />)}
        </div>
      </section>

      <section className="mx-auto max-w-5xl px-12 py-8">
        <h3 className="mb-6 text-2xl font-bold text-primary">دستگاه‌ها و آوازها</h3>
        <div className="no-scrollbar flex gap-4 overflow-x-auto scroll-smooth pb-4">
          {dynamicModes.map((mode) => (
            <Link
              key={mode.id}
              to="/modes/$modeId"
              params={{ modeId: String(mode.id) }}
              className="flex-none rounded-full border border-outline-variant/30 bg-surface-container-high px-8 py-3 font-medium text-on-surface shadow-sm transition-all hover:bg-secondary-container hover:text-on-secondary-container"
            >
              {mode.name}
            </Link>
          ))}
        </div>
      </section>

      <section className="mx-auto max-w-5xl px-12 py-12">
        <SectionHeader title="نوازندگان برجسته" showAllTo="/players" />
        <div className="grid grid-cols-4 gap-8">
          {dynamicPlayers.map((artist) => <ArtistCard key={artist.name} artist={{ id: artist.id, name: artist.name, subtitle: artist.role, image: artist.image, alt: artist.alt, favoriteKind: "player" }} />)}
        </div>
      </section>

      <section className="mx-auto grid max-w-5xl grid-cols-12 gap-12 px-12 py-12">
        <div className="col-span-6">
          <SectionHeader title="برترین برنامه‌ها" refresh onRefresh={handleRefreshTopTracks} isRefreshing={isRefreshingTopTracks} />
          <TrackList tracks={dynamicTopTracks} />
        </div>
        <div className="col-span-6">
          <SectionHeader title="شنیده شده‌های اخیر" showAllTo="/recent" />
          {recentTracks.length > 0 ? (
            <TrackList tracks={recentTracks} playShape="square" linkMode="none" />
          ) : (
            <div className="rounded-2xl border border-outline-variant/20 bg-surface-container-low px-8 py-12 text-center text-sm font-bold text-on-surface-variant">
              هنوز برنامه‌ای پخش نشده است.
            </div>
          )}
        </div>
      </section>
    </div>
  );
}
