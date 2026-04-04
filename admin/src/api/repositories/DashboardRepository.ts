import sqlite3 from 'sqlite3'
import path from 'path'

export class DashboardRepository {
  private db: sqlite3.Database

  constructor() {
    const dbPath = path.resolve(process.cwd(), '../database/golha_database.db')
    this.db = new sqlite3.Database(dbPath)
  }

  private all<T = any>(sql: string, params: any[] = []) {
    return new Promise<T[]>((resolve, reject) => {
      this.db.all(sql, params, (error, rows) => (error ? reject(error) : resolve((rows as T[]) || [])))
    })
  }

  private get<T = any>(sql: string, params: any[] = []) {
    return new Promise<T>((resolve, reject) => {
      this.db.get(sql, params, (error, row) => (error ? reject(error) : resolve((row as T) || ({} as T))))
    })
  }

  async summary() {
    return this.get<{
      totalPrograms: number
      totalArtists: number
      totalSegments: number
      totalModes: number
      programsWithAudio: number
      programsWithTimeline: number
      totalCategories: number
      totalOrchestras: number
      totalInstruments: number
    }>(`
      SELECT
        (SELECT COUNT(*) FROM program) AS totalPrograms,
        (SELECT COUNT(*) FROM artist) AS totalArtists,
        (SELECT COUNT(*) FROM program_timeline) AS totalSegments,
        (SELECT COUNT(*) FROM mode) AS totalModes,
        (SELECT COUNT(*) FROM program WHERE audio_url IS NOT NULL AND TRIM(audio_url) <> '') AS programsWithAudio,
        (SELECT COUNT(DISTINCT program_id) FROM program_timeline) AS programsWithTimeline,
        (SELECT COUNT(*) FROM category) AS totalCategories,
        (SELECT COUNT(*) FROM orchestra) AS totalOrchestras,
        (SELECT COUNT(*) FROM instrument) AS totalInstruments
    `)
  }

  async categoryBreakdown() {
    return this.all<{ name: string; total: number }>(`
      SELECT c.title_fa AS name, COUNT(*) AS total
      FROM program p
      JOIN category c ON c.id = p.category_id
      GROUP BY c.id
      ORDER BY total DESC, c.id ASC
    `)
  }

  async topSingers(limit = 8) {
    return this.all<{ name: string; total: number }>(`
      SELECT a.name, COUNT(*) AS total
      FROM program_singers ps
      JOIN singer s ON s.id = ps.singer_id
      JOIN artist a ON a.id = s.artist_id
      GROUP BY s.id
      ORDER BY total DESC, a.name ASC
      LIMIT ?
    `, [limit])
  }

  async topModes(limit = 8) {
    return this.all<{ name: string; total: number }>(`
      SELECT m.name, COUNT(*) AS total
      FROM program_modes pm
      JOIN mode m ON m.id = pm.mode_id
      GROUP BY m.id
      ORDER BY total DESC, m.name ASC
      LIMIT ?
    `, [limit])
  }

  async topOrchestras(limit = 5) {
    return this.all<{ name: string; total: number }>(`
      SELECT o.name, COUNT(*) AS total
      FROM program_orchestras po
      JOIN orchestra o ON o.id = po.orchestra_id
      GROUP BY o.id
      ORDER BY total DESC, o.name ASC
      LIMIT ?
    `, [limit])
  }

  async recentPrograms(limit = 6) {
    return this.all<{ id: number; title: string; no: number; sub_no: string | null; category_name: string }>(`
      SELECT p.id, p.title, p.no, p.sub_no, c.title_fa AS category_name
      FROM program p
      JOIN category c ON c.id = p.category_id
      ORDER BY p.id DESC
      LIMIT ?
    `, [limit])
  }

  close() {
    this.db.close()
  }
}
