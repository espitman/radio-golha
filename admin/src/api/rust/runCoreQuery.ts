import { access } from 'node:fs/promises'
import path from 'node:path'
import { execFile as execFileCallback } from 'node:child_process'
import { promisify } from 'node:util'

const execFile = promisify(execFileCallback)

const CORE_DIR = path.resolve(process.cwd(), '../core')
const CORE_DB = path.resolve(process.cwd(), '../database/golha_database.db')
const CORE_BINARY = path.resolve(CORE_DIR, 'target/debug/radiogolha-core-cli')

type CoreCommand = 'admin-dashboard' | 'admin-programs'

async function resolveCoreExecutable() {
  try {
    await access(CORE_BINARY)
    return {
      command: CORE_BINARY,
      prefixArgs: [] as string[],
    }
  } catch {
    return {
      command: 'cargo',
      prefixArgs: ['run', '--quiet', '--bin', 'radiogolha-core-cli', '--'],
    }
  }
}

export async function runCoreQuery<T>(command: CoreCommand, args: string[] = []): Promise<T> {
  const executable = await resolveCoreExecutable()
  const finalArgs = [...executable.prefixArgs, '--db', CORE_DB, command, ...args]

  try {
    const { stdout } = await execFile(executable.command, finalArgs, {
      cwd: CORE_DIR,
      maxBuffer: 1024 * 1024 * 10,
    })

    return JSON.parse(stdout) as T
  } catch (error: any) {
    const stderr = error?.stderr?.toString()?.trim()
    const message = stderr || error?.message || 'Failed to run Rust core query'
    throw new Error(message)
  }
}
