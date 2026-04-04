import sqlite3 from 'sqlite3'
import path from 'path'

type LookupKind = 'orchestras' | 'instruments' | 'modes'

const LOOKUP_CONFIG: Record<LookupKind, { table: string; usageSql: string }> = {
  orchestras: {
    table: 'orchestra',
    usageSql: '(SELECT COUNT(*) FROM program_orchestras po WHERE po.orchestra_id = base.id)',
  },
  instruments: {
    table: 'instrument',
    usageSql: '(SELECT COUNT(*) FROM program_performers pp WHERE pp.instrument_id = base.id)',
  },
  modes: {
    table: 'mode',
    usageSql: '(SELECT COUNT(*) FROM program_modes pm WHERE pm.mode_id = base.id)',
  },
}

export class LookupRepository {
  private db: sqlite3.Database

  constructor() {
    const dbPath = path.resolve(process.cwd(), '../database/golha_database.db')
    this.db = new sqlite3.Database(dbPath)
  }

  async list(kind: LookupKind, search: string, page: number, limit: number = 24) {
    const config = LOOKUP_CONFIG[kind]
    const offset = (page - 1) * limit
    let sql = `
      SELECT
        base.id,
        base.name,
        ${config.usageSql} AS usage_count
      FROM ${config.table} base
      WHERE 1 = 1
    `
    const params: any[] = []

    if (search) {
      sql += ' AND base.name LIKE ?'
      params.push(`%${search}%`)
    }

    sql += ' ORDER BY base.name COLLATE NOCASE ASC LIMIT ? OFFSET ?'
    params.push(limit, offset)

    return new Promise((resolve, reject) => {
      this.db.all(sql, params, (err, rows) => err ? reject(err) : resolve(rows || []))
    })
  }

  async count(kind: LookupKind, search: string) {
    const config = LOOKUP_CONFIG[kind]
    let sql = `SELECT COUNT(*) as total FROM ${config.table} base WHERE 1 = 1`
    const params: any[] = []

    if (search) {
      sql += ' AND base.name LIKE ?'
      params.push(`%${search}%`)
    }

    return new Promise((resolve, reject) => {
      this.db.get(sql, params, (err, row: any) => err ? reject(err) : resolve(row?.total || 0))
    })
  }

  async stats(kind: LookupKind) {
    const config = LOOKUP_CONFIG[kind]
    const sql = `
      SELECT
        (SELECT COUNT(*) FROM ${config.table}) AS total_items,
        (SELECT COALESCE(SUM(usage_count), 0) FROM (
          SELECT ${config.usageSql} AS usage_count
          FROM ${config.table} base
        )) AS total_usage
    `

    return new Promise((resolve, reject) => {
      this.db.get(sql, [], (err, row) => err ? reject(err) : resolve(row || {}))
    })
  }

  close() {
    this.db.close()
  }
}

export type { LookupKind }
