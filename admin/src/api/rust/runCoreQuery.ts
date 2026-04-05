import { createRequire } from 'node:module'
import { execFileSync } from 'node:child_process'
import { CORE_DB_PATH, CORE_DIR, resolveCoreAddonPath } from './coreRuntime'

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
}

export type LookupKind = 'orchestras' | 'instruments' | 'modes'
export type SearchMatchMode = 'any' | 'all'

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

function pushListFlag(args: string[], flag: string, values?: number[]) {
  if (!values || values.length === 0) return
  args.push(flag, values.join(','))
}

function pushMatchFlag(args: string[], flag: string, mode: SearchMatchMode, values?: number[]) {
  if (!values || values.length === 0) return
  args.push(flag, mode)
}

function runCoreCli<T>(args: string[]) {
  const output = execFileSync('cargo', ['run', '--quiet', '--', '--db', CORE_DB_PATH, ...args], {
    cwd: CORE_DIR,
    encoding: 'utf8',
  })
  return parseJson<T>(output)
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
    } = params || {}

    return this.wrap(() => {
      const args = ['admin-program-search', '--page', String(page)]

      if (transcriptQuery?.trim()) {
        args.push('--transcript-query', transcriptQuery.trim())
      }

      pushListFlag(args, '--category-ids', categoryIds)
      pushListFlag(args, '--mode-ids', modeIds)
      pushMatchFlag(args, '--mode-match', modeMatch, modeIds)
      pushListFlag(args, '--orchestra-ids', orchestraIds)
      pushMatchFlag(args, '--orchestra-match', orchestraMatch, orchestraIds)
      pushListFlag(args, '--instrument-ids', instrumentIds)
      pushMatchFlag(args, '--instrument-match', instrumentMatch, instrumentIds)
      pushListFlag(args, '--singer-ids', singerIds)
      pushMatchFlag(args, '--singer-match', singerMatch, singerIds)
      pushListFlag(args, '--poet-ids', poetIds)
      pushMatchFlag(args, '--poet-match', poetMatch, poetIds)
      pushListFlag(args, '--announcer-ids', announcerIds)
      pushMatchFlag(args, '--announcer-match', announcerMatch, announcerIds)
      pushListFlag(args, '--composer-ids', composerIds)
      pushMatchFlag(args, '--composer-match', composerMatch, composerIds)
      pushListFlag(args, '--arranger-ids', arrangerIds)
      pushMatchFlag(args, '--arranger-match', arrangerMatch, arrangerIds)
      pushListFlag(args, '--performer-ids', performerIds)
      pushMatchFlag(args, '--performer-match', performerMatch, performerIds)
      pushListFlag(args, '--orchestra-leader-ids', orchestraLeaderIds)
      pushMatchFlag(args, '--orchestra-leader-match', orchestraLeaderMatch, orchestraLeaderIds)

      return runCoreCli<T>(args)
    })
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
