import path from 'node:path'
import { existsSync } from 'node:fs'

export const CORE_DB_PATH = path.resolve(process.cwd(), '../database/golha_database.db')
export const CORE_DIR = path.resolve(process.cwd(), '../core')
export const CORE_NODE_DIR = path.resolve(process.cwd(), '../core-node')
export const CORE_DEBUG_ADDON_PATH = path.resolve(CORE_NODE_DIR, 'target/debug/radiogolha_core.node')
export const CORE_RELEASE_ADDON_PATH = path.resolve(CORE_NODE_DIR, 'target/release/radiogolha_core.node')

export function resolveCoreAddonPath() {
  return existsSync(CORE_DEBUG_ADDON_PATH) ? CORE_DEBUG_ADDON_PATH : CORE_RELEASE_ADDON_PATH
}
