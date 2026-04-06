import path from 'path'
import { createRequire } from 'module'
const require = createRequire(import.meta.url)

const addonPath = '/Users/espitman/Documents/Projects/radioGolha/core/adapters/node/target/debug/radiogolha_core.node'
const dbPath = '/Users/espitman/Documents/Projects/radioGolha/database/golha_database.db'

const core = require(addonPath)
console.log('Core loaded')

try {
  const result = core.getArtistDetail(dbPath, 551)
  console.log('Result for 551:', result)
} catch (e) {
  console.error('Error for 551:', e)
}
