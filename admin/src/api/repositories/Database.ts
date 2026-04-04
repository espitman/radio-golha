import sqlite3 from 'sqlite3';
import path from 'path';

// Shared database path
const dbPath = path.resolve(__dirname, '../../../../database/golha_database.db');

export class Database {
    private static instance: sqlite3.Database | null = null;

    static getInstance(): sqlite3.Database {
        if (!this.instance) {
            this.instance = new sqlite3.Database(dbPath);
        }
        return this.instance;
    }

    static async query<T>(sql: string, params: any[] = []): Promise<T[]> {
        const db = this.getInstance();
        return new Promise((resolve, reject) => {
            db.all(sql, params, (err, rows) => {
                if (err) reject(err);
                else resolve(rows as T[]);
            });
        });
    }

    static async getOne<T>(sql: string, params: any[] = []): Promise<T | null> {
        const db = this.getInstance();
        return new Promise((resolve, reject) => {
            db.get(sql, params, (err, row) => {
                if (err) reject(err);
                else resolve((row as T) || null);
            });
        });
    }
}
