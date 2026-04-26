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

export function searchPrograms(payload: CoreSearchPayload) {
  void payload;
  const rows = fallbackTracks.map((track) => ({ id: track.id, title: track.title, categoryName: "گلها", no: track.id, duration: track.duration, audioUrl: track.audioUrl, artist: track.artist }));
  return withFallback(async () => bridge()?.searchPrograms(payload), { rows, total: rows.length, page: 1, totalPages: 1 });
}
