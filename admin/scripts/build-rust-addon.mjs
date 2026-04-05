import { copyFileSync, existsSync, mkdirSync } from 'node:fs'
import { execFileSync } from 'node:child_process'
import path from 'node:path'
import { coreNodeDir, outputAddonPath, sourceLibraryPath, targetDir, rootDir } from './core-paths.mjs'

const cargoBin = existsSync(path.join(process.env.HOME || '', '.cargo/bin/cargo'))
  ? path.join(process.env.HOME || '', '.cargo/bin/cargo')
  : 'cargo'

execFileSync(cargoBin, ['build', ...(process.env.NODE_ENV === 'production' ? ['--release'] : [])], {
  cwd: coreNodeDir,
  stdio: 'inherit',
  env: {
    ...process.env,
    RADIOGOLHA_ROOT_DIR: rootDir,
  },
})

if (!existsSync(sourceLibraryPath)) {
  throw new Error(`Rust dynamic library not found: ${sourceLibraryPath}`)
}

mkdirSync(targetDir, { recursive: true })
copyFileSync(sourceLibraryPath, outputAddonPath)

console.log(`Rust addon ready: ${outputAddonPath}`)
