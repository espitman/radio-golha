import { createRequire } from 'node:module'
import { CORE_DB_PATH, resolveCoreAddonPath } from './coreRuntime'

const require = createRequire(import.meta.url)

type NativeCoreModule = {
  dashboardOverview(dbPath: string): string
  listPrograms(
    dbPath: string,
    search: string,
    page: number,
    categoryId?: number,
    singerId?: number,
    sortField?: string,
    sortDirection?: string,
  ): string
  getProgramDetail(dbPath: string, id: number): string
  listArtists(dbPath: string, search: string, page: number, role?: string): string
  listLookupItems(dbPath: string, kind: string, search: string, page: number): string
  getProgramSearchOptions(dbPath: string): string
  searchPrograms(dbPath: string, payloadJson: string): string
  getArtistDetail(dbPath: string, id: number): string
  updateArtist(dbPath: string, id: number, name: string, avatar: string | null): void
}

export type LookupKind = 'orchestras' | 'instruments' | 'modes'
export type SearchMatchMode = 'any' | 'all'
export type ProgramSortField = 'id' | 'no' | 'sub_no' | 'title' | 'category_name'

export type ArtistDetail = {
  id: number
  name: string
  avatar?: string
}

let nativeCore: NativeCoreModule | null = null

function loadNativeCore() {
  if (nativeCore) return nativeCore
  const addonPath = resolveCoreAddonPath()
  nativeCore = require(addonPath) as NativeCoreModule
  return nativeCore
}

function parseJson<T>(payload: string): T {
  return JSON.parse(payload) as T
}

export class RustCoreClient {
  private readonly dbPath: string

  constructor(dbPath: string = CORE_DB_PATH) {
    this.dbPath = dbPath
  }

  private get core() {
    return loadNativeCore()
  }

  async getDashboardOverview<T>(): Promise<T> {
    return this.wrap(() => parseJson<T>(this.core.dashboardOverview(this.dbPath)))
  }

  async listPrograms<T>(params?: {
    search?: string
    page?: number
    categoryId?: number
    singerId?: number
    sortField?: 'id' | 'no' | 'sub_no' | 'title' | 'category_name'
    sortDirection?: 'asc' | 'desc'
  }): Promise<T> {
    const {
      search = '',
      page = 1,
      categoryId,
      singerId,
      sortField = 'no',
      sortDirection = 'asc',
    } = params || {}
    return this.wrap(() =>
      parseJson<T>(
        this.core.listPrograms(
          this.dbPath,
          search,
          page,
          categoryId,
          singerId,
          sortField,
          sortDirection,
        ),
      )
    )
  }

  async getProgramDetail<T>(id: number): Promise<T> {
    return this.wrap(() => parseJson<T>(this.core.getProgramDetail(this.dbPath, id)))
  }

  async listArtists<T>(params?: {
    search?: string
    page?: number
    role?: string
  }): Promise<T> {
    const { search = '', page = 1, role } = params || {}
    return this.wrap(() => parseJson<T>(this.core.listArtists(this.dbPath, search, page, role)))
  }

  async getArtistDetail(id: number): Promise<ArtistDetail | null> {
    return this.wrap(() => parseJson<ArtistDetail | null>(this.core.getArtistDetail(this.dbPath, id)))
  }

  async updateArtist(id: number, name: string, avatar?: string | null): Promise<void> {
    return this.wrap(() => this.core.updateArtist(this.dbPath, id, name, avatar || null))
  }

  async listLookupItems<T>(kind: LookupKind, params?: {
    search?: string
    page?: number
  }): Promise<T> {
    const { search = '', page = 1 } = params || {}
    return this.wrap(() => parseJson<T>(this.core.listLookupItems(this.dbPath, kind, search, page)))
  }

  async getProgramSearchOptions<T>(): Promise<T> {
    return this.wrap(() => parseJson<T>(this.core.getProgramSearchOptions(this.dbPath)))
  }

  async searchPrograms<T>(params?: {
    transcriptQuery?: string
    page?: number
    categoryIds?: number[]
    modeIds?: number[]
    modeMatch?: SearchMatchMode
    orchestraIds?: number[]
    orchestraMatch?: SearchMatchMode
    instrumentIds?: number[]
    instrumentMatch?: SearchMatchMode
    singerIds?: number[]
    singerMatch?: SearchMatchMode
    poetIds?: number[]
    poetMatch?: SearchMatchMode
    announcerIds?: number[]
    announcerMatch?: SearchMatchMode
    composerIds?: number[]
    composerMatch?: SearchMatchMode
    arrangerIds?: number[]
    arrangerMatch?: SearchMatchMode
    performerIds?: number[]
    performerMatch?: SearchMatchMode
    orchestraLeaderIds?: number[]
    orchestraLeaderMatch?: SearchMatchMode
    sortField?: ProgramSortField
    sortDirection?: 'asc' | 'desc'
  }): Promise<T> {
    const {
      transcriptQuery,
      page = 1,
      categoryIds = [],
      modeIds = [],
      modeMatch = 'any',
      orchestraIds = [],
      orchestraMatch = 'any',
      instrumentIds = [],
      instrumentMatch = 'any',
      singerIds = [],
      singerMatch = 'any',
      poetIds = [],
      poetMatch = 'any',
      announcerIds = [],
      announcerMatch = 'any',
      composerIds = [],
      composerMatch = 'any',
      arrangerIds = [],
      arrangerMatch = 'any',
      performerIds = [],
      performerMatch = 'any',
      orchestraLeaderIds = [],
      orchestraLeaderMatch = 'any',
      sortField = 'no',
      sortDirection = 'asc',
    } = params || {}

    return this.wrap(() =>
      parseJson<T>(
        this.core.searchPrograms(
          this.dbPath,
          JSON.stringify({
            transcriptQuery: transcriptQuery?.trim() || undefined,
            page,
            categoryIds,
            modeIds,
            modeMatch,
            orchestraIds,
            orchestraMatch,
            instrumentIds,
            instrumentMatch,
            singerIds,
            singerMatch,
            poetIds,
            poetMatch,
            announcerIds,
            announcerMatch,
            composerIds,
            composerMatch,
            arrangerIds,
            arrangerMatch,
            performerIds,
            performerMatch,
            orchestraLeaderIds,
            orchestraLeaderMatch,
            sortField,
            sortDirection,
          }),
        ),
      ),
    )
  }

  private async wrap<T>(loader: () => T): Promise<T> {
    try {
      return loader()
    } catch (error: any) {
      throw new Error(error?.message || 'Failed to run Rust core native query')
    }
  }
}

export const rustCoreClient = new RustCoreClient()
