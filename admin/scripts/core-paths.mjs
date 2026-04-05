import path from 'node:path'
import { fileURLToPath } from 'node:url'

const __dirname = path.dirname(fileURLToPath(import.meta.url))

export const adminDir = path.resolve(__dirname, '..')
export const rootDir = path.resolve(adminDir, '..')
export const coreNodeDir = path.resolve(rootDir, 'core/adapters/node')

export const profile = process.env.NODE_ENV === 'production' ? 'release' : 'debug'
export const targetDir = path.join(coreNodeDir, 'target', profile)

export const sourceLibraryName =
  process.platform === 'darwin'
    ? 'libradiogolha_core_node.dylib'
    : process.platform === 'win32'
      ? 'radiogolha_core_node.dll'
      : 'libradiogolha_core_node.so'

export const sourceLibraryPath = path.join(targetDir, sourceLibraryName)
export const outputAddonPath = path.join(targetDir, 'radiogolha_core.node')
