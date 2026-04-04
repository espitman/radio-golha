import { Database } from './Database';

export class ProgramRepository {
    static async findAll(search: string = '', limit: number = 24, offset: number = 0) {
        let sql = 'SELECT p.*, c.title_fa as category_name FROM program p JOIN category c ON p.category_id = c.id WHERE 1=1';
        let params: any[] = [];
        if (search) {
            sql += ' AND (p.title LIKE ? OR p.no LIKE ?)';
            params.push(`%${search}%`, `%${search}%`);
        }
        sql += ' ORDER BY p.id ASC LIMIT ? OFFSET ?';
        params.push(limit, offset);
        return await Database.query(sql, params);
    }

    static async findById(id: number) {
        const sql = 'SELECT p.*, c.title_fa as category_name FROM program p JOIN category c ON p.category_id = c.id WHERE p.id = ?';
        return await Database.getOne(sql, [id]);
    }

    static async findSingers(programId: number) {
        const sql = 'SELECT a.name FROM program_singers ps JOIN singer s ON ps.singer_id = s.id JOIN artist a ON s.artist_id = a.id WHERE ps.program_id = ?';
        return await Database.query<{ name: string }>(sql, [programId]);
    }

    static async findPoets(programId: number) {
        const sql = 'SELECT a.name FROM program_poets pp JOIN poet p ON pp.poet_id = p.id JOIN artist a ON p.artist_id = a.id WHERE pp.program_id = ?';
        return await Database.query<{ name: string }>(sql, [programId]);
    }

    static async findPerformers(programId: number) {
        const sql = 'SELECT a.name, i.name as instrument FROM program_performers pp JOIN performer r ON pp.performer_id = r.id JOIN artist a ON r.artist_id = a.id JOIN instrument i ON pp.instrument_id = i.id WHERE pp.program_id = ?';
        return await Database.query<{ name: string, instrument: string }>(sql, [programId]);
    }

    static async findTimeline(programId: number) {
        const sql = 'SELECT t.*, m.name as mode_name FROM program_timeline t LEFT JOIN mode m ON t.mode_id = m.id WHERE t.program_id = ?';
        return await Database.query(sql, [programId]);
    }
}
