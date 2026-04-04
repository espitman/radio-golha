import { defineConfig } from 'vite'
import { devtools } from '@tanstack/devtools-vite'
import tsconfigPaths from 'vite-tsconfig-paths'
import { tanstackRouter } from '@tanstack/router-plugin/vite'
import viteReact from '@vitejs/plugin-react'
import tailwindcss from '@tailwindcss/vite'
import sqlite3 from 'sqlite3'
import path from 'path'
import { URL } from 'url'

// Path to the ACTUAL database on your disk - Using Absolute Resolution for stability
const dbPath = path.resolve(__dirname, '../database/golha_database.db')

export default defineConfig({
  plugins: [
    devtools(),
    tsconfigPaths({ projects: ['./tsconfig.json'] }),
    tailwindcss(),
    tanstackRouter({ target: 'react', autoCodeSplitting: true }),
    viteReact(),
    {
      name: 'sqlite-api',
      configureServer(server) {
        server.middlewares.use(async (req, res, next) => {
          const url = new URL(req.url!, 'http://localhost');
          
          // API for Programs List with Pagination Support
          if (req.method === 'GET' && url.pathname.startsWith('/api/programs')) {
            const db = new sqlite3.Database(dbPath);
            const search = url.searchParams.get('search') || '';
            const page = parseInt(url.searchParams.get('page') || '1');
            const limit = 24;
            const offset = (page - 1) * limit;

            let sql = 'SELECT p.*, c.title_fa as category_name FROM program p JOIN category c ON p.category_id = c.id WHERE 1=1';
            let params: any[] = [];
            if (search) {
                sql += ' AND (p.title LIKE ? OR p.no LIKE ?)';
                params.push(`%${search}%`, `%${search}%`);
            }
            sql += ' ORDER BY p.id ASC LIMIT ? OFFSET ?';
            params.push(limit, offset);

            db.all(sql, params, (err, rows) => {
              if (err) { db.close(); res.statusCode = 500; return res.end(JSON.stringify({ error: err.message })); }
              
              // Count Total for Pagination
              const countSql = 'SELECT COUNT(*) as total FROM program p WHERE 1=1' + (search ? ' AND (p.title LIKE ? OR p.no LIKE ?)' : '');
              db.get(countSql, search ? [`%${search}%`, `%${search}%`] : [], (err, countRes: any) => {
                res.setHeader('Content-Type', 'application/json');
                res.end(JSON.stringify({
                    rows: rows,
                    total: countRes?.total || 0,
                    page,
                    totalPages: Math.ceil((countRes?.total || 0) / 24)
                }));
                db.close();
              });
            });
            return;
          }

          // API for Program Detail
          if (req.method === 'GET' && url.pathname.startsWith('/api/program/')) {
            const id = url.pathname.split('/').pop();
            if (!id || isNaN(parseInt(id))) return next();

            const db = new sqlite3.Database(dbPath);
            db.get('SELECT p.*, c.title_fa as category_name FROM program p JOIN category c ON p.category_id = c.id WHERE p.id = ?', [id], (err, row: any) => {
                if (err || !row) { db.close(); res.statusCode = 404; return res.end(JSON.stringify({ error: "Not found" })); }
                
                db.all('SELECT a.name FROM program_singers ps JOIN singer s ON ps.singer_id = s.id JOIN artist a ON s.artist_id = a.id WHERE ps.program_id = ?', [id], (err, singers) => {
                    row.singers = singers.map((s: any) => s.name);
                    db.all('SELECT a.name FROM program_poets pp JOIN poet p ON pp.poet_id = p.id JOIN artist a ON p.artist_id = a.id WHERE pp.program_id = ?', [id], (err, poets) => {
                        row.poets = poets.map((p: any) => p.name);
                        db.all('SELECT a.name, i.name as instrument FROM program_performers pp JOIN performer r ON pp.performer_id = r.id JOIN artist a ON r.artist_id = a.id JOIN instrument i ON pp.instrument_id = i.id WHERE pp.program_id = ?', [id], (err, perf) => {
                            row.performers = perf;
                            db.all('SELECT t.*, m.name as mode_name FROM program_timeline t LEFT JOIN mode m ON t.mode_id = m.id WHERE t.program_id = ?', [id], (err, timeline) => {
                                row.timeline = timeline;
                                res.setHeader('Content-Type', 'application/json');
                                res.end(JSON.stringify(row));
                                db.close();
                            });
                        });
                    });
                });
            });
            return;
          }
          next();
        });
      }
    }
  ],
  server: {
    port: 3336,
  },
})
