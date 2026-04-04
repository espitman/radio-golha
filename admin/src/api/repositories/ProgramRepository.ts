import sqlite3 from 'sqlite3'
import path from 'path'

export class ProgramRepository {
  private db: sqlite3.Database;

  constructor() {
    const dbPath = path.resolve(process.cwd(), '../database/golha_database.db');
    this.db = new sqlite3.Database(dbPath);
  }

  async list(search: string, page: number, categoryId?: number, singerId?: number, limit: number = 24) {
    const offset = (page - 1) * limit;
    let sql = 'SELECT p.*, c.title_fa as category_name FROM program p JOIN category c ON p.category_id = c.id WHERE 1=1';
    const params: any[] = [];
    if (search) {
      sql += ' AND (p.title LIKE ? OR CAST(p.no AS TEXT) LIKE ? OR COALESCE(p.sub_no, \'\') LIKE ?)';
      params.push(`%${search}%`, `%${search}%`, `%${search}%`);
    }
    if (categoryId) { sql += ' AND p.category_id = ?'; params.push(categoryId); }
    if (singerId) {
      sql += ' AND EXISTS (SELECT 1 FROM program_singers ps WHERE ps.program_id = p.id AND ps.singer_id = ?)';
      params.push(singerId);
    }
    sql += ' ORDER BY p.no ASC, COALESCE(p.sub_no, \'\') ASC, p.id ASC LIMIT ? OFFSET ?';
    params.push(limit, offset);
    return new Promise((r, j) => { this.db.all(sql, params, (err, rows) => err ? j(err) : r(rows)); });
  }

  async count(search: string, categoryId?: number, singerId?: number) {
    let sql = 'SELECT COUNT(*) as total FROM program p WHERE 1=1';
    const params: any[] = [];
    if (search) {
      sql += ' AND (p.title LIKE ? OR CAST(p.no AS TEXT) LIKE ? OR COALESCE(p.sub_no, \'\') LIKE ?)';
      params.push(`%${search}%`, `%${search}%`, `%${search}%`);
    }
    if (categoryId) {
      sql += ' AND p.category_id = ?';
      params.push(categoryId);
    }
    if (singerId) {
      sql += ' AND EXISTS (SELECT 1 FROM program_singers ps WHERE ps.program_id = p.id AND ps.singer_id = ?)';
      params.push(singerId);
    }
    return new Promise((r, j) => { this.db.get(sql, params, (err, res: any) => err ? j(err) : r(res?.total || 0)); });
  }

  async categories() {
    return new Promise((r, j) => {
      this.db.all('SELECT id, title_fa FROM category ORDER BY id ASC', [], (err, rows) => err ? j(err) : r(rows || []));
    });
  }

  async singers() {
    return new Promise((r, j) => {
      this.db.all(`
        SELECT DISTINCT s.id, a.name
        FROM singer s
        JOIN artist a ON s.artist_id = a.id
        JOIN program_singers ps ON ps.singer_id = s.id
        ORDER BY a.name ASC
      `, [], (err, rows) => err ? j(err) : r(rows || []));
    });
  }

  async getDetail(id: number) {
    const program: any = await new Promise((r, j) => {
        this.db.get('SELECT p.*, c.title_fa as category_name FROM program p JOIN category c ON p.category_id = c.id WHERE p.id = ?', [id], (err, row) => err ? j(err) : r(row));
    });
    if (!program) return null;

    // OVERALL METADATA (SINGERS, POETS, PERFORMERS)
    program.singers = await new Promise(r => this.db.all('SELECT a.name FROM program_singers ps JOIN singer s ON ps.singer_id = s.id JOIN artist a ON s.artist_id = a.id WHERE ps.program_id = ?', [id], (_, res) => r((res || []).map((s: any) => s.name))));
    program.poets = await new Promise(r => this.db.all('SELECT a.name FROM program_poets pp JOIN poet p ON pp.poet_id = p.id JOIN artist a ON p.artist_id = a.id WHERE pp.program_id = ?', [id], (_, res) => r((res || []).map((p: any) => p.name))));
    program.performers = await new Promise(r => this.db.all('SELECT a.name, i.name as instrument FROM program_performers pp JOIN performer r ON pp.performer_id = r.id JOIN artist a ON r.artist_id = a.id JOIN instrument i ON pp.instrument_id = i.id WHERE pp.program_id = ?', [id], (_, res) => r(res || [])));
    
    // NEW OVERALL METADATA: ANNOUNCERS, ORCHESTRAS, COMPOSERS, ARRANGERS
    program.announcers = await new Promise(r => this.db.all('SELECT a.name FROM program_announcers pa JOIN announcer s ON pa.announcer_id = s.id JOIN artist a ON s.artist_id = a.id WHERE pa.program_id = ?', [id], (_, res) => r((res || []).map((a: any) => a.name))));
    program.orchestras = await new Promise(r => this.db.all('SELECT o.name FROM program_orchestras po JOIN orchestra o ON po.orchestra_id = o.id WHERE po.program_id = ?', [id], (_, res) => r((res || []).map((o: any) => o.name))));
    program.modes = await new Promise(r => this.db.all('SELECT DISTINCT m.name FROM program_modes pm JOIN mode m ON pm.mode_id = m.id WHERE pm.program_id = ? ORDER BY m.name ASC', [id], (_, res) => r((res || []).map((m: any) => m.name))));
    program.orchestra_leaders = await new Promise(r => this.db.all(`
      SELECT DISTINCT a.name, o.name as orchestra
      FROM program_orchestra_leaders pol
      JOIN orchestra_leader ol ON pol.orchestra_leader_id = ol.id
      JOIN artist a ON ol.artist_id = a.id
      JOIN orchestra o ON pol.orchestra_id = o.id
      WHERE pol.program_id = ?
      ORDER BY a.name ASC
    `, [id], (_, res) => r(res || [])));
    program.composers = await new Promise(r => this.db.all('SELECT a.name FROM program_composers pc JOIN composer s ON pc.composer_id = s.id JOIN artist a ON s.artist_id = a.id WHERE pc.program_id = ?', [id], (_, res) => r((res || []).map((c: any) => c.name))));
    program.arrangers = await new Promise(r => this.db.all('SELECT a.name FROM program_arrangers pa JOIN arranger s ON pa.arranger_id = s.id JOIN artist a ON s.artist_id = a.id WHERE pa.program_id = ?', [id], (_, res) => r((res || []).map((arr: any) => arr.name))));

    // TIMELINE METADATA (STRICT SEPARATION TO FIX MOLAVI/ATTAR MIXUP)
    const timeline: any[] = await new Promise(r => this.db.all(`
      SELECT
        t.*,
        COALESCE(
          (
            SELECT GROUP_CONCAT(m2.name, '، ')
            FROM program_timeline_modes ptm
            JOIN mode m2 ON ptm.mode_id = m2.id
            WHERE ptm.timeline_id = t.id
          ),
          m.name
        ) as mode_name
      FROM program_timeline t
      LEFT JOIN mode m ON t.mode_id = m.id
      WHERE t.program_id = ?
    `, [id], (_, res) => r(res || [])));
    const fullTimeline = [];
    for (const segment of timeline) {
        const singers = await new Promise(r => this.db.all('SELECT a.name FROM program_timeline_singers pts JOIN singer s ON pts.singer_id = s.id JOIN artist a ON s.artist_id = a.id WHERE pts.timeline_id = ?', [segment.id], (_, res) => r((res || []).map((s: any) => s.name))));
        const performers = await new Promise(r => this.db.all('SELECT a.name, i.name as instrument FROM program_timeline_performers ptp JOIN performer r ON ptp.performer_id = r.id JOIN artist a ON r.artist_id = a.id LEFT JOIN program_performers gp ON (gp.program_id = ? AND gp.performer_id = ptp.performer_id) LEFT JOIN instrument i ON gp.instrument_id = i.id WHERE ptp.timeline_id = ?', [id, segment.id], (_, res) => r(res || [])));
        const poets = await new Promise(r => this.db.all('SELECT a.name FROM program_timeline_poets ptpo JOIN poet p ON ptpo.poet_id = p.id JOIN artist a ON p.artist_id = a.id WHERE ptpo.timeline_id = ?', [segment.id], (_, res) => r((res || []).map((p: any) => p.name))));
        const announcers = await new Promise(r => this.db.all('SELECT a.name FROM program_timeline_announcers ptan JOIN announcer s ON ptan.announcer_id = s.id JOIN artist a ON s.artist_id = a.id WHERE ptan.timeline_id = ?', [segment.id], (_, res) => r((res || []).map((a: any) => a.name))));
        const orchestras = await new Promise(r => this.db.all('SELECT DISTINCT o.name FROM program_timeline_orchestras pto JOIN orchestra o ON pto.orchestra_id = o.id WHERE pto.timeline_id = ?', [segment.id], (_, res) => r((res || []).map((o: any) => o.name))));
        const orchestraLeaders = await new Promise(r => this.db.all(`
          SELECT DISTINCT a.name, o.name as orchestra
          FROM program_timeline_orchestra_leaders ptol
          JOIN orchestra_leader ol ON ptol.orchestra_leader_id = ol.id
          JOIN artist a ON ol.artist_id = a.id
          JOIN orchestra o ON ptol.orchestra_id = o.id
          WHERE ptol.timeline_id = ?
          ORDER BY a.name ASC
        `, [segment.id], (_, res) => r(res || [])));

        fullTimeline.push({...segment, singers, performers, poets, announcers, orchestras, orchestraLeaders});
    }

    program.timeline = fullTimeline;
    return program;
  }

  close() { this.db.close(); }
}
