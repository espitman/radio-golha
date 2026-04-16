use rusqlite::{params, Connection};
use serde::{Deserialize, Serialize};
use std::path::Path;

use crate::error::CoreResult;

pub struct UserDataStore {
    conn: Connection,
}

#[derive(Debug, Clone, Serialize)]
#[serde(rename_all = "camelCase")]
pub struct Playlist {
    pub id: i64,
    pub name: String,
    #[serde(rename = "type")]
    pub playlist_type: String,
    pub filters_json: String,
    pub created_at: i64,
    pub track_ids: Vec<i64>,
}

#[derive(Debug, Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct CreatePlaylistRequest {
    pub name: String,
    #[serde(rename = "type")]
    pub playlist_type: Option<String>,
    pub filters_json: Option<String>,
}

impl UserDataStore {
    pub fn open<P: AsRef<Path>>(path: P) -> CoreResult<Self> {
        let conn = Connection::open(path)?;
        conn.pragma_update(None, "foreign_keys", "ON")?;
        conn.pragma_update(None, "journal_mode", "WAL")?;

        conn.execute_batch(
            "
            CREATE TABLE IF NOT EXISTS playlists (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT NOT NULL,
                type TEXT NOT NULL DEFAULT 'search',
                filters_json TEXT NOT NULL DEFAULT '{}',
                created_at INTEGER NOT NULL DEFAULT (strftime('%s','now') * 1000)
            );
            CREATE TABLE IF NOT EXISTS playlist_tracks (
                playlist_id INTEGER NOT NULL REFERENCES playlists(id) ON DELETE CASCADE,
                track_id INTEGER NOT NULL,
                added_at INTEGER NOT NULL DEFAULT (strftime('%s','now') * 1000),
                PRIMARY KEY (playlist_id, track_id)
            );
            CREATE TABLE IF NOT EXISTS favorite_artists (
                artist_id INTEGER PRIMARY KEY,
                artist_type TEXT NOT NULL DEFAULT 'singer',
                added_at INTEGER NOT NULL DEFAULT (strftime('%s','now') * 1000)
            );
            CREATE TABLE IF NOT EXISTS playback_history (
                track_id INTEGER PRIMARY KEY,
                play_count INTEGER NOT NULL DEFAULT 1,
                last_played_at INTEGER NOT NULL DEFAULT (strftime('%s','now') * 1000)
            );
            ",
        )?;

        Ok(Self { conn })
    }

    pub fn record_playback(&self, track_id: i64) -> CoreResult<()> {
        self.conn.execute(
            "INSERT INTO playback_history (track_id, last_played_at, play_count)
             VALUES (?1, (strftime('%s','now') * 1000), 1)
             ON CONFLICT(track_id) DO UPDATE SET
                play_count = play_count + 1,
                last_played_at = (strftime('%s','now') * 1000)",
            params![track_id],
        )?;
        Ok(())
    }

    pub fn get_recent_tracks(&self, limit: i64) -> CoreResult<Vec<i64>> {
        let mut stmt = self.conn.prepare(
            "SELECT track_id FROM playback_history ORDER BY last_played_at DESC LIMIT ?1",
        )?;
        let rows = stmt.query_map([limit], |row| row.get(0))?;
        rows.collect::<Result<Vec<_>, _>>().map_err(Into::into)
    }

    pub fn get_most_played_tracks(&self, limit: i64) -> CoreResult<Vec<i64>> {
        let mut stmt = self.conn.prepare(
            "SELECT track_id FROM playback_history ORDER BY play_count DESC, last_played_at DESC LIMIT ?1",
        )?;
        let rows = stmt.query_map([limit], |row| row.get(0))?;
        rows.collect::<Result<Vec<_>, _>>().map_err(Into::into)
    }

    pub fn get_all_playlists(&self) -> CoreResult<Vec<Playlist>> {
        let mut stmt = self.conn.prepare(
            "SELECT id, name, type, filters_json, created_at FROM playlists ORDER BY created_at DESC",
        )?;
        let rows = stmt.query_map([], |row| {
            Ok((
                row.get::<_, i64>(0)?,
                row.get::<_, String>(1)?,
                row.get::<_, String>(2)?,
                row.get::<_, String>(3)?,
                row.get::<_, i64>(4)?,
            ))
        })?;

        let mut playlists = Vec::new();
        for row in rows {
            let (id, name, ptype, filters_json, created_at) = row?;
            let track_ids = self.get_track_ids(id)?;
            playlists.push(Playlist {
                id,
                name,
                playlist_type: ptype,
                filters_json,
                created_at,
                track_ids,
            });
        }
        Ok(playlists)
    }

    pub fn get_playlist(&self, id: i64) -> CoreResult<Option<Playlist>> {
        let result = self.conn.query_row(
            "SELECT id, name, type, filters_json, created_at FROM playlists WHERE id = ?1",
            [id],
            |row| {
                Ok((
                    row.get::<_, i64>(0)?,
                    row.get::<_, String>(1)?,
                    row.get::<_, String>(2)?,
                    row.get::<_, String>(3)?,
                    row.get::<_, i64>(4)?,
                ))
            },
        );
        match result {
            Ok((id, name, ptype, filters_json, created_at)) => {
                let track_ids = self.get_track_ids(id)?;
                Ok(Some(Playlist { id, name, playlist_type: ptype, filters_json, created_at, track_ids }))
            }
            Err(rusqlite::Error::QueryReturnedNoRows) => Ok(None),
            Err(e) => Err(e.into()),
        }
    }

    pub fn create_playlist(&self, name: &str, playlist_type: &str, filters_json: &str) -> CoreResult<i64> {
        self.conn.execute(
            "INSERT INTO playlists (name, type, filters_json) VALUES (?1, ?2, ?3)",
            params![name, playlist_type, filters_json],
        )?;
        Ok(self.conn.last_insert_rowid())
    }

    pub fn rename_playlist(&self, id: i64, name: &str) -> CoreResult<()> {
        self.conn.execute("UPDATE playlists SET name = ?1 WHERE id = ?2", params![name, id])?;
        Ok(())
    }

    pub fn delete_playlist(&self, id: i64) -> CoreResult<()> {
        self.conn.execute("DELETE FROM playlists WHERE id = ?1", [id])?;
        Ok(())
    }

    pub fn add_track(&self, playlist_id: i64, track_id: i64) -> CoreResult<()> {
        self.conn.execute(
            "INSERT OR IGNORE INTO playlist_tracks (playlist_id, track_id) VALUES (?1, ?2)",
            params![playlist_id, track_id],
        )?;
        Ok(())
    }

    pub fn remove_track(&self, playlist_id: i64, track_id: i64) -> CoreResult<()> {
        self.conn.execute(
            "DELETE FROM playlist_tracks WHERE playlist_id = ?1 AND track_id = ?2",
            params![playlist_id, track_id],
        )?;
        Ok(())
    }

    pub fn get_manual_playlists(&self) -> CoreResult<Vec<Playlist>> {
        let all = self.get_all_playlists()?;
        Ok(all.into_iter().filter(|p| p.playlist_type == "manual").collect())
    }

    // ── Favorite Artists ──

    pub fn add_favorite_artist(&self, artist_id: i64, artist_type: &str) -> CoreResult<()> {
        self.conn.execute(
            "INSERT OR IGNORE INTO favorite_artists (artist_id, artist_type) VALUES (?1, ?2)",
            params![artist_id, artist_type],
        )?;
        Ok(())
    }

    pub fn remove_favorite_artist(&self, artist_id: i64) -> CoreResult<()> {
        self.conn.execute("DELETE FROM favorite_artists WHERE artist_id = ?1", [artist_id])?;
        Ok(())
    }

    pub fn is_favorite_artist(&self, artist_id: i64) -> CoreResult<bool> {
        let count: i64 = self.conn.query_row(
            "SELECT COUNT(*) FROM favorite_artists WHERE artist_id = ?1", [artist_id], |r| r.get(0),
        )?;
        Ok(count > 0)
    }

    pub fn get_favorite_artist_ids(&self) -> CoreResult<Vec<i64>> {
        let mut stmt = self.conn.prepare("SELECT artist_id FROM favorite_artists ORDER BY added_at DESC")?;
        let rows = stmt.query_map([], |row| row.get(0))?;
        rows.collect::<Result<Vec<_>, _>>().map_err(Into::into)
    }

    pub fn get_favorite_artist_ids_by_type(&self, artist_type: &str) -> CoreResult<Vec<i64>> {
        let mut stmt = self.conn.prepare("SELECT artist_id FROM favorite_artists WHERE artist_type = ?1 ORDER BY added_at DESC")?;
        let rows = stmt.query_map([artist_type], |row| row.get(0))?;
        rows.collect::<Result<Vec<_>, _>>().map_err(Into::into)
    }

    fn get_track_ids(&self, playlist_id: i64) -> CoreResult<Vec<i64>> {
        let mut stmt = self.conn.prepare(
            "SELECT track_id FROM playlist_tracks WHERE playlist_id = ?1 ORDER BY added_at ASC",
        )?;
        let rows = stmt.query_map([playlist_id], |row| row.get(0))?;
        rows.collect::<Result<Vec<_>, _>>().map_err(Into::into)
    }
}
