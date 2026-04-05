import { createRequire } from 'node:module'
import { CORE_DB_PATH, resolveCoreAddonPath } from './coreRuntime'

const require = createRequire(import.meta.url)

type NativeCoreModule = {
  dashboardOverview(dbPath: string): string
  listPrograms(dbPath: string, search: string, page: number, categoryId?: number, singerId?: number): string
  getProgramDetail(dbPath: string, id: number): string
  listArtists(dbPath: string, search: string, page: number, role?: string): string
  listLookupItems(dbPath: string, kind: string, search: string, page: number): string
}

export type LookupKind = 'orchestras' | 'instruments' | 'modes'

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
  }): Promise<T> {
    const { search = '', page = 1, categoryId, singerId } = params || {}
    return this.wrap(() =>
      parseJson<T>(this.core.listPrograms(this.dbPath, search, page, categoryId, singerId))
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

  private async wrap<T>(loader: () => T): Promise<T> {
    try {
      return loader()
    } catch (error: any) {
      throw new Error(error?.message || 'Failed to run Rust core native query')
    }
  }
}

export const rustCoreClient = new RustCoreClient()
