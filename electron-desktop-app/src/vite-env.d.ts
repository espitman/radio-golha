/// <reference types="vite/client" />

import type {
  CoreArtistDetail,
  CoreHomeArtist,
  CoreHomeMusician,
  CoreHomePayload,
  CoreMode,
  CoreProgramTracks,
  CoreSearchOptions,
  CoreSearchPayload,
  CoreSearchResponse,
  CoreTopBarSearchResult,
  CoreTrack,
  CoreTrackDetail,
} from "./lib/coreApi";

declare global {
  interface Window {
    radioGolhaCore?: {
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
  }
}
