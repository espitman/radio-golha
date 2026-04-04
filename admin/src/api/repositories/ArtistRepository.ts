import sqlite3 from 'sqlite3'
import path from 'path'

export class ArtistRepository {
  private db: sqlite3.Database

  constructor() {
    const dbPath = path.resolve(process.cwd(), '../database/golha_database.db')
    this.db = new sqlite3.Database(dbPath)
  }

  async list(search: string, page: number, role?: string, limit: number = 24) {
    const offset = (page - 1) * limit
    let sql = `
      SELECT
        a.id,
        a.name,
        EXISTS(SELECT 1 FROM singer s WHERE s.artist_id = a.id) AS is_singer,
        EXISTS(SELECT 1 FROM performer p WHERE p.artist_id = a.id) AS is_performer,
        EXISTS(SELECT 1 FROM poet po WHERE po.artist_id = a.id) AS is_poet,
        EXISTS(SELECT 1 FROM announcer an WHERE an.artist_id = a.id) AS is_announcer,
        EXISTS(SELECT 1 FROM composer c WHERE c.artist_id = a.id) AS is_composer,
        EXISTS(SELECT 1 FROM arranger ar WHERE ar.artist_id = a.id) AS is_arranger
      FROM artist a
      WHERE 1 = 1
    `
    const params: any[] = []

    if (search) {
      sql += ' AND a.name LIKE ?'
      params.push(`%${search}%`)
    }

    if (role) {
      const roleMap: Record<string, string> = {
        singer: 'EXISTS(SELECT 1 FROM singer s WHERE s.artist_id = a.id)',
        performer: 'EXISTS(SELECT 1 FROM performer p WHERE p.artist_id = a.id)',
        poet: 'EXISTS(SELECT 1 FROM poet po WHERE po.artist_id = a.id)',
        announcer: 'EXISTS(SELECT 1 FROM announcer an WHERE an.artist_id = a.id)',
        composer: 'EXISTS(SELECT 1 FROM composer c WHERE c.artist_id = a.id)',
        arranger: 'EXISTS(SELECT 1 FROM arranger ar WHERE ar.artist_id = a.id)',
      }
      if (roleMap[role]) sql += ` AND ${roleMap[role]}`
    }

    sql += ' ORDER BY a.name COLLATE NOCASE ASC LIMIT ? OFFSET ?'
    params.push(limit, offset)

    return new Promise((resolve, reject) => {
      this.db.all(sql, params, (err, rows) => err ? reject(err) : resolve(rows || []))
    })
  }

  async count(search: string, role?: string) {
    let sql = 'SELECT COUNT(*) as total FROM artist a WHERE 1 = 1'
    const params: any[] = []

    if (search) {
      sql += ' AND a.name LIKE ?'
      params.push(`%${search}%`)
    }

    if (role) {
      const roleMap: Record<string, string> = {
        singer: 'EXISTS(SELECT 1 FROM singer s WHERE s.artist_id = a.id)',
        performer: 'EXISTS(SELECT 1 FROM performer p WHERE p.artist_id = a.id)',
        poet: 'EXISTS(SELECT 1 FROM poet po WHERE po.artist_id = a.id)',
        announcer: 'EXISTS(SELECT 1 FROM announcer an WHERE an.artist_id = a.id)',
        composer: 'EXISTS(SELECT 1 FROM composer c WHERE c.artist_id = a.id)',
        arranger: 'EXISTS(SELECT 1 FROM arranger ar WHERE ar.artist_id = a.id)',
      }
      if (roleMap[role]) sql += ` AND ${roleMap[role]}`
    }

    return new Promise((resolve, reject) => {
      this.db.get(sql, params, (err, row: any) => err ? reject(err) : resolve(row?.total || 0))
    })
  }

  async stats() {
    const sql = `
      SELECT
        (SELECT COUNT(*) FROM artist) AS total_artists,
        (SELECT COUNT(*) FROM singer) AS singers,
        (SELECT COUNT(*) FROM performer) AS performers,
        (SELECT COUNT(*) FROM poet) AS poets
    `
    return new Promise((resolve, reject) => {
      this.db.get(sql, [], (err, row) => err ? reject(err) : resolve(row || {}))
    })
  }

  close() {
    this.db.close()
  }
}
