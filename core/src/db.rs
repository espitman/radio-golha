use std::path::{Path, PathBuf};

use rusqlite::Connection;

use crate::error::{CoreError, CoreResult};

pub struct RadioGolhaCore {
    conn: Connection,
    db_path: PathBuf,
}

impl RadioGolhaCore {
    pub fn open<P: AsRef<Path>>(db_path: P) -> CoreResult<Self> {
        Self::open_mode(db_path, true)
    }

    pub fn open_rw<P: AsRef<Path>>(db_path: P) -> CoreResult<Self> {
        Self::open_mode(db_path, false)
    }

    fn open_mode<P: AsRef<Path>>(db_path: P, read_only: bool) -> CoreResult<Self> {
        let db_path = db_path.as_ref().to_path_buf();
        if !db_path.exists() {
            return Err(CoreError::DatabaseNotFound(db_path));
        }

        let conn = Connection::open(&db_path)?;
        conn.pragma_update(None, "foreign_keys", "ON")?;
        if read_only {
            conn.pragma_update(None, "query_only", "ON")?;
        }

        Ok(Self { conn, db_path })
    }

    pub fn open_default() -> CoreResult<Self> {
        Self::open("../database/golha_database.db")
    }

    pub fn connection(&self) -> &Connection {
        &self.conn
    }

    pub fn db_path(&self) -> &Path {
        &self.db_path
    }
}
