export type CoreTrack = {
  id: number;
  title: string;
  artist: string;
  duration: string;
  audioUrl?: string | null;
  mode?: string | null;
  singerAvatars: string[];
};

export type CoreHomeProgram = {
  id: number;
  title: string;
  episodeCount: number;
};

export type CoreHomeArtist = {
  id: number;
  name: string;
  avatar?: string | null;
  programCount: number;
};

export type CoreHomeMusician = CoreHomeArtist & {
  instrument: string;
};

export type CoreMode = {
  id: number;
  name: string;
  usageCount: number;
};

export type CoreHomePayload = {
  programs: CoreHomeProgram[];
  singers: CoreHomeArtist[];
  modes: CoreMode[];
  musicians: CoreHomeMusician[];
  topTracks: CoreTrack[];
};

export type CoreArtistDetail = {
  id: number;
  name: string;
  avatar?: string | null;
  instrument?: string | null;
  trackCount: number;
  tracks: CoreTrack[];
  categoryCounts: Array<{ categoryId: number; title: string; count: number }>;
};

export type CoreProgramTracks = {
  id: number;
  title: string;
  categoryName: string;
  no: number;
  subNo?: string | null;
  duration?: string | null;
  audioUrl?: string | null;
  tracks: CoreTrack[];
};

export type CoreSearchOption = { id: number; name: string };
export type CoreCategoryOption = { id: number; titleFa: string };

export type CoreSearchOptions = {
  categories: CoreCategoryOption[];
  singers: CoreSearchOption[];
  poets: CoreSearchOption[];
  announcers: CoreSearchOption[];
  composers: CoreSearchOption[];
  arrangers: CoreSearchOption[];
  performers: CoreSearchOption[];
  orchestraLeaders: CoreSearchOption[];
  modes: CoreSearchOption[];
  orchestras: CoreSearchOption[];
  instruments: CoreSearchOption[];
};

export type CoreProgramListItem = {
  id: number;
  title: string;
  categoryName: string;
  no: number;
  subNo?: string | null;
  duration?: string | null;
  audioUrl?: string | null;
  artist?: string | null;
};

export type CoreSearchResponse = {
  rows: CoreProgramListItem[];
  total: number;
  page: number;
  totalPages: number;
};

export type CoreTopBarSearchResult = {
  kind: "artist" | "track";
  id: number;
  title: string;
  subtitle: string;
  avatar?: string | null;
  trackId?: number | null;
};

export type CoreArtistCredit = {
  artistId?: number | null;
  name: string;
  avatar?: string | null;
};

export type CorePerformerCredit = {
  artistId: number;
  name: string;
  avatar?: string | null;
  instrument?: string | null;
};

export type CoreTimelineSegment = {
  id: number;
  startTime?: string | null;
  endTime?: string | null;
  modeName?: string | null;
  singers: string[];
  poets: string[];
  announcers: string[];
  orchestras: string[];
  performers: CorePerformerCredit[];
};

export type CoreTrackDetail = {
  id: number;
  title: string;
  categoryName: string;
  no: number;
  subNo?: string | null;
  duration?: string | null;
  audioUrl?: string | null;
  singers: CoreArtistCredit[];
  poets: CoreArtistCredit[];
  announcers: CoreArtistCredit[];
  composers: CoreArtistCredit[];
  arrangers: CoreArtistCredit[];
  modes: string[];
  orchestras: CoreArtistCredit[];
  performers: CorePerformerCredit[];
  timeline: CoreTimelineSegment[];
};

export type CoreSearchPayload = {
  transcriptQuery?: string;
  page?: number;
  categoryIds?: number[];
  modeIds?: number[];
  modeMatch?: "all" | "any";
  orchestraIds?: number[];
  orchestraMatch?: "all" | "any";
  instrumentIds?: number[];
  instrumentMatch?: "all" | "any";
  singerIds?: number[];
  singerMatch?: "all" | "any";
  poetIds?: number[];
  poetMatch?: "all" | "any";
  announcerIds?: number[];
  announcerMatch?: "all" | "any";
  composerIds?: number[];
  composerMatch?: "all" | "any";
  arrangerIds?: number[];
  arrangerMatch?: "all" | "any";
  performerIds?: number[];
  performerMatch?: "all" | "any";
  orchestraLeaderIds?: number[];
  orchestraLeaderMatch?: "all" | "any";
};

type ElectronCoreBridge = {
  getHomeData: () => Promise<CoreHomePayload | null>;
  getTopTracks: (limit?: number) => Promise<CoreTrack[] | null>;
  getSingers: () => Promise<CoreHomeArtist[] | null>;
  getMusicians: () => Promise<CoreHomeMusician[] | null>;
  getModes: () => Promise<CoreMode[] | null>;
  getArtistDetail: (artistId: number) => Promise<CoreArtistDetail | null>;
  getProgramTracks: (programId: number) => Promise<CoreProgramTracks | null>;
  getTrackDetail: (programId: number) => Promise<CoreTrackDetail | null>;
  getSearchOptions: () => Promise<CoreSearchOptions | null>;
  topBarSearch: (query: string, limit?: number) => Promise<CoreTopBarSearchResult[] | null>;
  searchPrograms: (payload: CoreSearchPayload) => Promise<CoreSearchResponse | null>;
};

const avatar = "https://lh3.googleusercontent.com/aida-public/AB6AXuBQlX0v-Lh1Lj6CxDQYOd-xCKyNArhNLKOXT1e6WR3j251g0iUPqfUXkoPd4gjIMgt1QeRMHZDCbQeXgk7e6hsvtIN7AJzWyjXu8BRxJdWLB1WDSgoyYfG2_3oT6a9fFzoQtYdeZe430yfJn_MdqX-yfmEECMgIIn8tLeQLf8RO78T8vzpr-c1wSqYYgXEUGfN4KzDYQ7vH5P4PMUb9Hd7ufSqPmAeJmC59z2G7fYemqLFnCjTVpN0pHuR295sHnCYO1LAERIc-5CI3";
const secondAvatar = "https://lh3.googleusercontent.com/aida-public/AB6AXuCdxuO-btnXsk1jNpqhy440lM0cy98lRfDi1JAptVTgEX2Fsd20qzP5qdPm4Uyu9rzx-xCJKzIIKMklM9eum8R1VtG4twX-3SPLQQUBnnXlPCetNsBS9VQc3tYQgP5sjejwOy4-lmGLkfbTz8936tpKD_B0OJPamCaYwMrOV8wuT0xTtpzprbbol2QJem2cYn7aRphjkJD3NvDSv2m_WSAYqqSfcL9czHeJl__fvDr074p20jojBew89tr1330zt2CEnUGhkGTXOAdO";

const fallbackTracks: CoreTrack[] = [
  { id: 580, title: "گلهای رنگارنگ ۵۸۰", artist: "محمدرضا شجریان", duration: "35:10", audioUrl: null, mode: "بیات ترک", singerAvatars: [avatar] },
  { id: 120, title: "گلهای تازه ۱۲۰", artist: "غلامحسین بنان", duration: "15:20", audioUrl: null, mode: "شور", singerAvatars: [secondAvatar] },
  { id: 67, title: "یک شاخه گل ۶۷", artist: "ناصر مسعودی", duration: "14:32", audioUrl: null, mode: "ماهور", singerAvatars: [] },
];

const fallbackHome: CoreHomePayload = {
  programs: [
    { id: 1, title: "گل‌های تازه", episodeCount: 155 },
    { id: 2, title: "برگ سبز", episodeCount: 312 },
    { id: 3, title: "یک شاخه گل", episodeCount: 465 },
    { id: 4, title: "گلهای جاویدان", episodeCount: 101 },
  ],
  singers: [
    { id: 1, name: "محمدرضا شجریان", avatar, programCount: 120 },
    { id: 2, name: "غلامحسین بنان", avatar: secondAvatar, programCount: 94 },
    { id: 3, name: "بانو دلکش", avatar, programCount: 72 },
    { id: 4, name: "بانو مرضیه", avatar: secondAvatar, programCount: 85 },
  ],
  musicians: [
    { id: 11, name: "جلیل شهناز", avatar: secondAvatar, programCount: 89, instrument: "نوازنده تار" },
    { id: 12, name: "حسن کسایی", avatar, programCount: 52, instrument: "نوازنده نی" },
    { id: 13, name: "فرهنگ شریف", avatar: secondAvatar, programCount: 67, instrument: "نوازنده تار" },
    { id: 14, name: "حبیب‌الله بدیعی", avatar, programCount: 62, instrument: "نوازنده ویولن" },
  ],
  modes: ["شور", "ماهور", "همایون", "اصفهان", "سه‌گاه", "چهارگاه", "نوا"].map((name, index) => ({ id: index + 1, name, usageCount: 10 - index })),
  topTracks: fallbackTracks,
};

const fallbackSearchOptions: CoreSearchOptions = {
  categories: fallbackHome.programs.map((program) => ({ id: program.id, titleFa: program.title })),
  singers: fallbackHome.singers.map(({ id, name }) => ({ id, name })),
  poets: ["حافظ", "سعدی", "مولانا"].map((name, index) => ({ id: index + 1, name })),
  announcers: ["آذر پژوهش", "روشنک"].map((name, index) => ({ id: index + 1, name })),
  composers: ["روح‌الله خالقی", "جواد معروفی"].map((name, index) => ({ id: index + 1, name })),
  arrangers: ["مرتضی حنانه", "فرامرز پایور"].map((name, index) => ({ id: index + 1, name })),
  performers: fallbackHome.musicians.map(({ id, name }) => ({ id, name })),
  orchestraLeaders: ["روح‌الله خالقی", "جواد معروفی"].map((name, index) => ({ id: index + 1, name })),
  modes: fallbackHome.modes.map(({ id, name }) => ({ id, name })),
  orchestras: ["ارکستر گل‌ها", "ارکستر رادیو ایران"].map((name, index) => ({ id: index + 1, name })),
  instruments: ["تار", "نی", "پیانو", "ویولن", "سنتور"].map((name, index) => ({ id: index + 1, name })),
};

const fallbackArtistDetail: CoreArtistDetail = {
  id: 1,
  name: "محمدرضا شجریان",
  avatar,
  instrument: null,
  trackCount: 120,
  tracks: fallbackTracks,
  categoryCounts: [
    { categoryId: 1, title: "گل‌های تازه", count: 48 },
    { categoryId: 2, title: "گل‌های رنگارنگ", count: 62 },
    { categoryId: 3, title: "یک شاخه گل", count: 10 },
  ],
};

const fallbackProgramTracks: CoreProgramTracks = {
  id: 580,
  title: "گلهای رنگارنگ شماره ۵۸۰",
  categoryName: "مجموعه گلهای رنگارنگ",
  no: 580,
  duration: "35:10",
  audioUrl: null,
  tracks: fallbackTracks,
};

const fallbackTrackDetail: CoreTrackDetail = {
  id: 580,
  title: "گلهای رنگارنگ ۵۸۰",
  categoryName: "گلهای رنگارنگ",
  no: 580,
  duration: "35:10",
  audioUrl: null,
  singers: [{ artistId: 1, name: "محمدرضا شجریان", avatar }],
  poets: [{ artistId: 31, name: "حافظ" }],
  announcers: [],
  composers: [],
  arrangers: [],
  modes: ["بیات ترک"],
  orchestras: [{ artistId: 41, name: "ارکستر گل‌ها" }],
  performers: [{ artistId: 11, name: "جلیل شهناز", avatar: secondAvatar, instrument: "تار" }],
  timeline: [
    { id: 1, startTime: "00:00", modeName: "پیش‌درآمد", singers: [], poets: [], announcers: [], orchestras: ["ارکستر گل‌ها"], performers: [] },
    { id: 2, startTime: "05:30", modeName: "آواز", singers: ["محمدرضا شجریان"], poets: ["حافظ"], announcers: [], orchestras: [], performers: [{ artistId: 11, name: "جلیل شهناز", instrument: "تار" }] },
  ],
};

function bridge(): ElectronCoreBridge | undefined {
  return window.radioGolhaCore;
}

async function withFallback<T>(loader: (() => Promise<T | null | undefined>) | undefined, fallback: T): Promise<T> {
  if (!loader) return fallback;
  const payload = await loader();
  return payload ?? fallback;
}

export function getHomeData() {
  return withFallback(async () => bridge()?.getHomeData(), fallbackHome);
}

export function getTopTracks(limit = 10) {
  return withFallback(async () => bridge()?.getTopTracks(limit), fallbackTracks.slice(0, limit));
}

export function getSingers() {
  return withFallback(async () => bridge()?.getSingers(), fallbackHome.singers);
}

export function getMusicians() {
  return withFallback(async () => bridge()?.getMusicians(), fallbackHome.musicians);
}

export function getModes() {
  return withFallback(async () => bridge()?.getModes(), fallbackHome.modes);
}

export function getArtistDetail(artistId: number) {
  return withFallback(async () => bridge()?.getArtistDetail(artistId), { ...fallbackArtistDetail, id: artistId });
}

export function getProgramTracks(programId: number) {
  return withFallback(async () => bridge()?.getProgramTracks(programId), { ...fallbackProgramTracks, id: programId });
}

export function getTrackDetail(programId: number) {
  return withFallback(async () => bridge()?.getTrackDetail(programId), { ...fallbackTrackDetail, id: programId });
}

export function getSearchOptions() {
  return withFallback(async () => bridge()?.getSearchOptions(), fallbackSearchOptions);
}

function normalizePersianText(value: string) {
  return value.trim().replace(/ي/g, "ی").replace(/ك/g, "ک").toLocaleLowerCase("fa-IR");
}

function scoreSearchMatch(value: string, query: string) {
  if (value.startsWith(query)) return 0;
  const wordHit = value.split(/\s+/).some((part) => part.startsWith(query));
  if (wordHit) return 1;
  return 2;
}

let cachedTopBarArtistPoolPromise: Promise<CoreTopBarSearchResult[]> | null = null;

const topBarRolePriority = {
  singer: 0,
  performer: 1,
  poet: 2,
  announcer: 3,
  composer: 4,
  arranger: 5,
  orchestraLeader: 6,
} as const;

type TopBarRoleKind = keyof typeof topBarRolePriority;

function rankTopBarRole(kind: TopBarRoleKind) {
  return topBarRolePriority[kind];
}

async function loadTopBarArtistPool() {
  if (!cachedTopBarArtistPoolPromise) {
    cachedTopBarArtistPoolPromise = Promise.all([getSearchOptions(), getSingers(), getMusicians()])
      .then(([options, singers, musicians]) => {
        const singerByName = new Map<string, CoreHomeArtist>();
        const musicianByName = new Map<string, CoreHomeMusician>();
        const mergedByName = new Map<string, { result: CoreTopBarSearchResult; role: TopBarRoleKind }>();

        singers.forEach((artist) => {
          singerByName.set(normalizePersianText(artist.name), artist);
        });

        musicians.forEach((artist) => {
          musicianByName.set(normalizePersianText(artist.name), artist);
        });

        const append = (items: CoreSearchOption[], role: TopBarRoleKind, subtitle: string, resolveSubtitle?: (item: CoreSearchOption) => string) => {
          items.forEach((item) => {
            const normalizedName = normalizePersianText(item.name);
            const singer = singerByName.get(normalizedName);
            const musician = musicianByName.get(normalizedName);
            const nextSubtitle = resolveSubtitle?.(item) ?? subtitle;
            const existing = mergedByName.get(normalizedName);
            if (!existing || rankTopBarRole(role) < rankTopBarRole(existing.role)) {
              mergedByName.set(normalizedName, {
                role,
                result: {
                  kind: "artist",
                  id: singer?.id ?? musician?.id ?? item.id,
                  title: item.name,
                  subtitle: nextSubtitle,
                  avatar: singer?.avatar ?? musician?.avatar ?? null,
                  trackId: null,
                },
              });
            }
          });
        };

        append(options.singers, "singer", "خواننده");
        append(options.performers, "performer", "نوازنده", (item) => {
          const instrument = musicianByName.get(normalizePersianText(item.name))?.instrument?.trim();
          return instrument ? instrument : "نوازنده";
        });
        append(options.poets, "poet", "شاعر");
        append(options.announcers, "announcer", "گوینده");
        append(options.composers, "composer", "آهنگساز");
        append(options.arrangers, "arranger", "تنظیم‌کننده");
        append(options.orchestraLeaders, "orchestraLeader", "رهبر ارکستر");

        return Array.from(mergedByName.values()).map((entry) => entry.result);
      })
      .catch(() => {
        return [
          ...fallbackHome.singers.map((artist) => ({
            kind: "artist" as const,
            id: artist.id,
            title: artist.name,
            subtitle: "خواننده",
            avatar: artist.avatar,
            trackId: null,
          })),
          ...fallbackHome.musicians.map((artist) => ({
            kind: "artist" as const,
            id: artist.id,
            title: artist.name,
            subtitle: artist.instrument || "نوازنده",
            avatar: artist.avatar,
            trackId: null,
          })),
        ];
      });
  }

  return cachedTopBarArtistPoolPromise;
}

async function fallbackTopBarSearch(query: string, limit: number) {
  const normalized = normalizePersianText(query);
  if (!normalized) return [];

  const [artistPool, searchResponse] = await Promise.all([
    loadTopBarArtistPool(),
    searchPrograms({ transcriptQuery: query, page: 1 }).catch(() => ({
      rows: fallbackTracks.map((track) => ({
        id: track.id,
        title: track.title,
        categoryName: "گلها",
        no: track.id,
        duration: track.duration,
        audioUrl: track.audioUrl,
        artist: track.artist,
      })),
      total: fallbackTracks.length,
      page: 1,
      totalPages: 1,
    })),
  ]);

  const artists = artistPool
    .filter((item) => normalizePersianText(item.title).includes(normalized))
    .sort((left, right) => {
      const leftScore = scoreSearchMatch(normalizePersianText(left.title), normalized);
      const rightScore = scoreSearchMatch(normalizePersianText(right.title), normalized);
      if (leftScore !== rightScore) return leftScore - rightScore;
      return left.title.localeCompare(right.title, "fa-IR");
    });

  const seenTracks = new Set<number>();
  const tracks = searchResponse.rows
    .filter((row) => {
      const rowNormalizedTitle = normalizePersianText(row.title);
      if (!rowNormalizedTitle.includes(normalized)) return false;
      if (seenTracks.has(row.id)) return false;
      seenTracks.add(row.id);
      return true;
    })
    .map((row) => ({
      kind: "track" as const,
      id: row.id,
      title: row.title,
      subtitle: row.artist || "ناشناس",
      avatar: null,
      trackId: row.id,
    }))
    .sort((left, right) => {
      const leftScore = scoreSearchMatch(normalizePersianText(left.title), normalized);
      const rightScore = scoreSearchMatch(normalizePersianText(right.title), normalized);
      if (leftScore !== rightScore) return leftScore - rightScore;
      return left.title.localeCompare(right.title, "fa-IR");
    });

  return [...artists, ...tracks].slice(0, limit);
}

export async function topBarSearch(query: string, limit = 10) {
  try {
    const payload = await bridge()?.topBarSearch(query, limit);
    if (payload) {
      return payload;
    }
  } catch {
    // Older Electron main/preload processes may not expose the latest IPC handler yet.
  }

  return fallbackTopBarSearch(query, limit);
}

export function searchPrograms(payload: CoreSearchPayload) {
  void payload;
  const rows = fallbackTracks.map((track) => ({ id: track.id, title: track.title, categoryName: "گلها", no: track.id, duration: track.duration, audioUrl: track.audioUrl, artist: track.artist }));
  return withFallback(async () => bridge()?.searchPrograms(payload), { rows, total: rows.length, page: 1, totalPages: 1 });
}
