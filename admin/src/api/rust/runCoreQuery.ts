import path from 'node:path'
import { createRequire } from 'node:module'
import { existsSync } from 'node:fs'

const require = createRequire(import.meta.url)

const CORE_DB = path.resolve(process.cwd(), '../database/golha_database.db')
const CORE_DEBUG_ADDON = path.resolve(process.cwd(), '../core-node/target/debug/radiogolha_core.node')
const CORE_RELEASE_ADDON = path.resolve(process.cwd(), '../core-node/target/release/radiogolha_core.node')

type CoreCommand =
  | 'admin-dashboard'
  | 'admin-programs'
  | 'admin-program-detail'
  | 'admin-artists'
  | 'admin-lookup'

type NativeCoreModule = {
  dashboardOverview(dbPath: string): string
  listPrograms(dbPath: string, search: string, page: number, categoryId?: number, singerId?: number): string
  getProgramDetail(dbPath: string, id: number): string
  listArtists(dbPath: string, search: string, page: number, role?: string): string
  listLookupItems(dbPath: string, kind: string, search: string, page: number): string
}

let nativeCore: NativeCoreModule | null = null

function loadNativeCore() {
  if (nativeCore) return nativeCore
  const addonPath = existsSync(CORE_DEBUG_ADDON) ? CORE_DEBUG_ADDON : CORE_RELEASE_ADDON
  nativeCore = require(addonPath) as NativeCoreModule
  return nativeCore
}

function parseNumericArg(args: string[], key: string) {
  const index = args.indexOf(key)
  if (index === -1) return undefined
  const value = args[index + 1]
  if (!value) return undefined
  const parsed = Number.parseInt(value, 10)
  return Number.isFinite(parsed) ? parsed : undefined
}

function parseStringArg(args: string[], key: string) {
  const index = args.indexOf(key)
  if (index === -1) return undefined
  return args[index + 1] ?? undefined
}

export async function runCoreQuery<T>(command: CoreCommand, args: string[] = []): Promise<T> {
  const core = loadNativeCore()

  try {
    switch (command) {
      case 'admin-dashboard':
        return JSON.parse(core.dashboardOverview(CORE_DB)) as T

      case 'admin-programs':
        return JSON.parse(
          core.listPrograms(
            CORE_DB,
            parseStringArg(args, '--search') ?? '',
            parseNumericArg(args, '--page') ?? 1,
            parseNumericArg(args, '--category-id'),
            parseNumericArg(args, '--singer-id')
          )
        ) as T

      case 'admin-program-detail':
        return JSON.parse(core.getProgramDetail(CORE_DB, Number.parseInt(args[0] || '0', 10))) as T

      case 'admin-artists':
        return JSON.parse(
          core.listArtists(
            CORE_DB,
            parseStringArg(args, '--search') ?? '',
            parseNumericArg(args, '--page') ?? 1,
            parseStringArg(args, '--role')
          )
        ) as T

      case 'admin-lookup':
        return JSON.parse(
          core.listLookupItems(
            CORE_DB,
            args[0] || '',
            parseStringArg(args, '--search') ?? '',
            parseNumericArg(args, '--page') ?? 1
          )
        ) as T
    }
  } catch (error: any) {
    throw new Error(error?.message || 'Failed to run Rust core native query')
  }
}
