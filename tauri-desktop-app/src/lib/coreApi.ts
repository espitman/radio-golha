import { invoke } from "@tauri-apps/api/core";

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

export function getHomeData() {
  return invoke<CoreHomePayload>("core_get_home_data");
}

export function getTopTracks(limit = 10) {
  return invoke<CoreTrack[]>("core_get_top_tracks", { limit });
}

export function getSingers() {
  return invoke<CoreHomeArtist[]>("core_get_singers");
}

export function getMusicians() {
  return invoke<CoreHomeMusician[]>("core_get_musicians");
}

export function getModes() {
  return invoke<CoreMode[]>("core_get_modes");
}

export function getArtistDetail(artistId: number) {
  return invoke<CoreArtistDetail>("core_get_artist_detail", { artistId });
}

export function getProgramTracks(programId: number) {
  return invoke<CoreProgramTracks>("core_get_program_tracks", { programId });
}

export function getTrackDetail(programId: number) {
  return invoke<CoreTrackDetail>("core_get_track_detail", { programId });
}

export function getSearchOptions() {
  return invoke<CoreSearchOptions>("core_get_search_options");
}

export function searchPrograms(payload: CoreSearchPayload) {
  return invoke<CoreSearchResponse>("core_search_programs", { payload });
}
