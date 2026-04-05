import { copyFileSync, existsSync, mkdirSync } from 'node:fs'
import path from 'node:path'
import { fileURLToPath } from 'node:url'
import { execFileSync } from 'node:child_process'

const __dirname = path.dirname(fileURLToPath(import.meta.url))
const adminDir = path.resolve(__dirname, '..')
const rootDir = path.resolve(adminDir, '..')
const coreDir = path.resolve(rootDir, 'core-node')

const cargoBin = existsSync(path.join(process.env.HOME || '', '.cargo/bin/cargo'))
  ? path.join(process.env.HOME || '', '.cargo/bin/cargo')
  : 'cargo'

const profile = process.env.NODE_ENV === 'production' ? 'release' : 'debug'
const targetDir = path.join(coreDir, 'target', profile)

const sourceLibraryName =
  process.platform === 'darwin'
    ? 'libradiogolha_core_node.dylib'
    : process.platform === 'win32'
      ? 'radiogolha_core_node.dll'
      : 'libradiogolha_core_node.so'

const sourceLibrary = path.join(targetDir, sourceLibraryName)
const outputAddon = path.join(targetDir, 'radiogolha_core.node')

execFileSync(cargoBin, ['build', ...(profile === 'release' ? ['--release'] : [])], {
  cwd: coreDir,
  stdio: 'inherit',
})

if (!existsSync(sourceLibrary)) {
  throw new Error(`Rust dynamic library not found: ${sourceLibrary}`)
}

mkdirSync(targetDir, { recursive: true })
copyFileSync(sourceLibrary, outputAddon)

console.log(`Rust addon ready: ${outputAddon}`)
