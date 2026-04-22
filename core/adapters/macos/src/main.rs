use anyhow::Result;
use clap::{Parser, Subcommand};
use radiogolha_core::RadioGolhaCore;
use radiogolha_core::{ProgramSearchFilters, ProgramSortField, SearchMatchMode, SortDirection};
use serde::Deserialize;
use serde_json::json;
use radiogolha_core::user_data::UserDataStore;

#[derive(Debug, Parser)]
#[command(name = "radiogolha-macos-bridge-cli")]
struct Cli {
    #[command(subcommand)]
    command: Command,
}

#[derive(Debug, Subcommand)]
enum Command {
    HomeFeedJson {
        #[arg(long)]
        db: String,
    },
    TopTracksJson {
        #[arg(long)]
        db: String,
    },
    ArtistDetailJson {
        #[arg(long)]
        db: String,
        #[arg(long)]
        artist_id: i64,
    },
    SingersJson {
        #[arg(long)]
        db: String,
    },
    MusiciansJson {
        #[arg(long)]
        db: String,
    },
    SearchOptionsJson {
        #[arg(long)]
        db: String,
    },
    SearchProgramsJson {
        #[arg(long)]
        db: String,
        #[arg(long)]
        filters_json: String,
    },
    ProgramsByCategoryJson {
        #[arg(long)]
        db: String,
        #[arg(long)]
        category_id: i64,
    },
    ProgramsByIdsJson {
        #[arg(long)]
        db: String,
        #[arg(long)]
        ids_json: String,
    },
    ProgramDetailJson {
        #[arg(long)]
        db: String,
        #[arg(long)]
        program_id: i64,
    },
    RecordPlayback {
        #[arg(long)]
        user_db: String,
        #[arg(long)]
        track_id: i64,
    },
    RecentlyPlayedIdsJson {
        #[arg(long)]
        user_db: String,
        #[arg(long, default_value_t = 20)]
        limit: i64,
    },
    MostPlayedIdsJson {
        #[arg(long)]
        user_db: String,
        #[arg(long, default_value_t = 20)]
        limit: i64,
    },
}

fn main() -> Result<()> {
    let cli = Cli::parse();

    match cli.command {
        Command::HomeFeedJson { db } => {
            println!("{}", build_home_feed_json(&db));
        }
        Command::TopTracksJson { db } => {
            println!("{}", build_top_tracks_json(&db));
        }
        Command::ArtistDetailJson { db, artist_id } => {
            println!("{}", build_artist_detail_json(&db, artist_id));
        }
        Command::SingersJson { db } => {
            println!("{}", build_singers_json(&db));
        }
        Command::MusiciansJson { db } => {
            println!("{}", build_musicians_json(&db));
        }
        Command::SearchOptionsJson { db } => {
            println!("{}", build_search_options_json(&db));
        }
        Command::SearchProgramsJson { db, filters_json } => {
            println!("{}", search_programs_json(&db, &filters_json));
        }
        Command::ProgramsByCategoryJson { db, category_id } => {
            println!("{}", build_programs_by_category_json(&db, category_id));
        }
        Command::ProgramsByIdsJson { db, ids_json } => {
            println!("{}", build_programs_by_ids_json(&db, &ids_json));
        }
        Command::ProgramDetailJson { db, program_id } => {
            println!("{}", build_program_detail_json(&db, program_id));
        }
        Command::RecordPlayback { user_db, track_id } => {
            println!("{}", record_playback(&user_db, track_id));
        }
        Command::RecentlyPlayedIdsJson { user_db, limit } => {
            println!("{}", get_recently_played_ids_json(&user_db, limit));
        }
        Command::MostPlayedIdsJson { user_db, limit } => {
            println!("{}", get_most_played_ids_json(&user_db, limit));
        }
    }

    Ok(())
}

#[derive(Deserialize, Default)]
#[serde(rename_all = "camelCase")]
struct MacSearchRequest {
    transcript_query: Option<String>,
    page: Option<i64>,
    category_ids: Option<Vec<i64>>,
    mode_ids: Option<Vec<i64>>,
    mode_match: Option<String>,
    orchestra_ids: Option<Vec<i64>>,
    orchestra_match: Option<String>,
    instrument_ids: Option<Vec<i64>>,
    instrument_match: Option<String>,
    singer_ids: Option<Vec<i64>>,
    singer_match: Option<String>,
    poet_ids: Option<Vec<i64>>,
    poet_match: Option<String>,
    announcer_ids: Option<Vec<i64>>,
    announcer_match: Option<String>,
    composer_ids: Option<Vec<i64>>,
    composer_match: Option<String>,
    arranger_ids: Option<Vec<i64>>,
    arranger_match: Option<String>,
    performer_ids: Option<Vec<i64>>,
    performer_match: Option<String>,
    orchestra_leader_ids: Option<Vec<i64>>,
    orchestra_leader_match: Option<String>,
}

fn parse_match_mode(s: &Option<String>) -> SearchMatchMode {
    match s.as_deref() {
        Some("all") => SearchMatchMode::All,
        _ => SearchMatchMode::Any,
    }
}

fn search_programs_json(db_path: &str, filters_json: &str) -> String {
    let req: MacSearchRequest = match serde_json::from_str(filters_json) {
        Ok(req) => req,
        Err(error) => return json!({ "error": error.to_string() }).to_string(),
    };

    let filters = ProgramSearchFilters {
        transcript_query: req.transcript_query,
        category_ids: req.category_ids.unwrap_or_default(),
        mode_ids: req.mode_ids.unwrap_or_default(),
        mode_match: parse_match_mode(&req.mode_match),
        orchestra_ids: req.orchestra_ids.unwrap_or_default(),
        orchestra_match: parse_match_mode(&req.orchestra_match),
        instrument_ids: req.instrument_ids.unwrap_or_default(),
        instrument_match: parse_match_mode(&req.instrument_match),
        singer_ids: req.singer_ids.unwrap_or_default(),
        singer_match: parse_match_mode(&req.singer_match),
        poet_ids: req.poet_ids.unwrap_or_default(),
        poet_match: parse_match_mode(&req.poet_match),
        announcer_ids: req.announcer_ids.unwrap_or_default(),
        announcer_match: parse_match_mode(&req.announcer_match),
        composer_ids: req.composer_ids.unwrap_or_default(),
        composer_match: parse_match_mode(&req.composer_match),
        arranger_ids: req.arranger_ids.unwrap_or_default(),
        arranger_match: parse_match_mode(&req.arranger_match),
        performer_ids: req.performer_ids.unwrap_or_default(),
        performer_match: parse_match_mode(&req.performer_match),
        orchestra_leader_ids: req.orchestra_leader_ids.unwrap_or_default(),
        orchestra_leader_match: parse_match_mode(&req.orchestra_leader_match),
        sort_field: ProgramSortField::No,
        sort_direction: SortDirection::Asc,
    };
    let page = req.page.unwrap_or(1);

    match RadioGolhaCore::open(db_path) {
        Ok(core) => match core.search_programs(&filters, page) {
            Ok(result) => json!(result).to_string(),
            Err(error) => json!({ "error": error.to_string() }).to_string(),
        },
        Err(error) => json!({ "error": error.to_string() }).to_string(),
    }
}

fn build_programs_by_ids_json(db_path: &str, ids_json: &str) -> String {
    let ids: Vec<i64> = match serde_json::from_str(ids_json) {
        Ok(ids) => ids,
        Err(error) => return json!({ "error": error.to_string() }).to_string(),
    };
    if ids.is_empty() {
        return "[]".to_string();
    }

    match RadioGolhaCore::open(db_path) {
        Ok(core) => {
            let conn = core.connection();
            let id_list = ids.iter().map(|id| id.to_string()).collect::<Vec<_>>().join(",");
            let sql = format!(
                "SELECT
                    p.id,
                    p.title,
                    p.no,
                    COALESCE(
                        (
                            SELECT GROUP_CONCAT(a.name, ' و ')
                            FROM program_singers ps
                            JOIN singer s ON s.id = ps.singer_id
                            JOIN artist a ON a.id = s.artist_id
                            WHERE ps.program_id = p.id
                        ),
                        'ناشناس'
                    ) AS artist_names,
                    (
                        SELECT GROUP_CONCAT(m.name, ' و ')
                        FROM program_modes pm
                        JOIN mode m ON m.id = pm.mode_id
                        WHERE pm.program_id = p.id
                    ) AS mode_names,
                    (SELECT MAX(end_time) FROM program_timeline WHERE program_id = p.id) AS duration,
                    p.audio_url
                 FROM program p
                 WHERE p.id IN ({})
                 ORDER BY p.no ASC, p.id ASC",
                id_list
            );

            let mut stmt = match conn.prepare(&sql) {
                Ok(stmt) => stmt,
                Err(error) => return json!({ "error": error.to_string() }).to_string(),
            };

            let rows = match stmt.query_map([], |row| {
                Ok(json!({
                    "id": row.get::<_, i64>(0)?,
                    "title": row.get::<_, Option<String>>(1)?,
                    "no": row.get::<_, i64>(2)?,
                    "artist": row.get::<_, String>(3)?,
                    "mode": row.get::<_, Option<String>>(4)?,
                    "duration": row.get::<_, Option<String>>(5)?,
                    "audioUrl": row.get::<_, Option<String>>(6)?
                }))
            }) {
                Ok(rows) => rows,
                Err(error) => return json!({ "error": error.to_string() }).to_string(),
            };

            let programs: Vec<_> = rows.filter_map(|r| r.ok()).collect();
            json!(programs).to_string()
        }
        Err(error) => json!({ "error": error.to_string() }).to_string(),
    }
}

fn build_program_detail_json(db_path: &str, program_id: i64) -> String {
    match RadioGolhaCore::open(db_path) {
        Ok(core) => match core.get_program_detail(program_id) {
            Ok(detail) => json!(detail).to_string(),
            Err(error) => json!({ "error": error.to_string() }).to_string(),
        },
        Err(error) => json!({ "error": error.to_string() }).to_string(),
    }
}

fn build_search_options_json(db_path: &str) -> String {
    match RadioGolhaCore::open(db_path) {
        Ok(core) => match core.program_search_options() {
            Ok(options) => json!(options).to_string(),
            Err(error) => json!({ "error": error.to_string() }).to_string(),
        },
        Err(error) => json!({ "error": error.to_string() }).to_string(),
    }
}

fn record_playback(user_db_path: &str, track_id: i64) -> String {
    match UserDataStore::open(user_db_path) {
        Ok(store) => match store.record_playback(track_id) {
            Ok(_) => "ok".to_string(),
            Err(error) => json!({ "error": error.to_string() }).to_string(),
        },
        Err(error) => json!({ "error": error.to_string() }).to_string(),
    }
}

fn get_recently_played_ids_json(user_db_path: &str, limit: i64) -> String {
    match UserDataStore::open(user_db_path) {
        Ok(store) => match store.get_recent_tracks(limit) {
            Ok(ids) => json!(ids).to_string(),
            Err(error) => json!({ "error": error.to_string() }).to_string(),
        },
        Err(error) => json!({ "error": error.to_string() }).to_string(),
    }
}

fn get_most_played_ids_json(user_db_path: &str, limit: i64) -> String {
    match UserDataStore::open(user_db_path) {
        Ok(store) => match store.get_most_played_tracks(limit) {
            Ok(ids) => json!(ids).to_string(),
            Err(error) => json!({ "error": error.to_string() }).to_string(),
        },
        Err(error) => json!({ "error": error.to_string() }).to_string(),
    }
}

fn build_programs_by_category_json(db_path: &str, category_id: i64) -> String {
    match RadioGolhaCore::open(db_path) {
        Ok(core) => {
            let conn = core.connection();
            let mut stmt = match conn.prepare(
                "
                SELECT
                    p.id,
                    p.title,
                    p.no,
                    COALESCE(
                        (
                            SELECT GROUP_CONCAT(a.name, ' و ')
                            FROM program_singers ps
                            JOIN singer s ON s.id = ps.singer_id
                            JOIN artist a ON a.id = s.artist_id
                            WHERE ps.program_id = p.id
                        ),
                        'ناشناس'
                    ) AS artist_names,
                    (
                        SELECT GROUP_CONCAT(m.name, ' و ')
                        FROM program_modes pm
                        JOIN mode m ON m.id = pm.mode_id
                        WHERE pm.program_id = p.id
                    ) AS mode_names,
                    (SELECT MAX(end_time) FROM program_timeline WHERE program_id = p.id) AS duration,
                    p.audio_url
                FROM program p
                WHERE p.category_id = ?1
                ORDER BY p.no ASC, p.id ASC
                ",
            ) {
                Ok(stmt) => stmt,
                Err(error) => return json!({ "error": error.to_string() }).to_string(),
            };

            let rows = match stmt.query_map([category_id], |row| {
                Ok(json!({
                    "id": row.get::<_, i64>(0)?,
                    "title": row.get::<_, Option<String>>(1)?,
                    "no": row.get::<_, i64>(2)?,
                    "artist": row.get::<_, String>(3)?,
                    "mode": row.get::<_, Option<String>>(4)?,
                    "duration": row.get::<_, Option<String>>(5)?,
                    "audioUrl": row.get::<_, Option<String>>(6)?
                }))
            }) {
                Ok(rows) => rows,
                Err(error) => return json!({ "error": error.to_string() }).to_string(),
            };

            let programs: Vec<_> = rows.filter_map(|r| r.ok()).collect();
            json!(programs).to_string()
        }
        Err(error) => json!({ "error": error.to_string() }).to_string(),
    }
}

fn build_musicians_json(db_path: &str) -> String {
    match RadioGolhaCore::open(db_path) {
        Ok(core) => {
            let conn = core.connection();
            let mut stmt = match conn.prepare(
                "
                SELECT
                  a.id,
                  a.name,
                  (
                    SELECT i.name
                    FROM program_performers pp2
                    LEFT JOIN instrument i ON i.id = pp2.instrument_id
                    WHERE pp2.performer_id = p.id AND i.name IS NOT NULL AND TRIM(i.name) <> ''
                    GROUP BY i.id, i.name
                    ORDER BY COUNT(*) DESC, i.name ASC
                    LIMIT 1
                  ) AS instrument_name,
                  a.avatar,
                  COUNT(DISTINCT pp.program_id) AS total
                FROM program_performers pp
                JOIN performer p ON p.id = pp.performer_id
                JOIN artist a ON a.id = p.artist_id
                GROUP BY p.id, a.id, a.name, a.avatar
                ORDER BY total DESC, a.name ASC
                ",
            ) {
                Ok(stmt) => stmt,
                Err(error) => return json!({ "error": error.to_string() }).to_string(),
            };

            let rows = match stmt.query_map([], |row| {
                let instrument: Option<String> = row.get(2)?;
                Ok(json!({
                    "id": row.get::<_, i64>(0)?,
                    "name": row.get::<_, String>(1)?,
                    "instrument": instrument
                        .filter(|v| !v.trim().is_empty())
                        .unwrap_or_else(|| "نوازنده".to_string()),
                    "avatar": row.get::<_, Option<String>>(3)?,
                    "programCount": row.get::<_, i64>(4)?
                }))
            }) {
                Ok(rows) => rows,
                Err(error) => return json!({ "error": error.to_string() }).to_string(),
            };

            let musicians: Vec<_> = rows.filter_map(|r| r.ok()).collect();
            json!(musicians).to_string()
        }
        Err(error) => json!({ "error": error.to_string() }).to_string(),
    }
}

fn build_singers_json(db_path: &str) -> String {
    match RadioGolhaCore::open(db_path) {
        Ok(core) => {
            let conn = core.connection();
            let mut stmt = match conn.prepare(
                "
                SELECT a.id, a.name, a.avatar, COUNT(DISTINCT ps.program_id) AS total
                FROM program_singers ps
                JOIN singer s ON s.id = ps.singer_id
                JOIN artist a ON a.id = s.artist_id
                GROUP BY s.id, a.id, a.name, a.avatar
                ORDER BY total DESC, a.name ASC
                ",
            ) {
                Ok(stmt) => stmt,
                Err(error) => return json!({ "error": error.to_string() }).to_string(),
            };

            let rows = match stmt.query_map([], |row| {
                Ok(json!({
                    "id": row.get::<_, i64>(0)?,
                    "name": row.get::<_, String>(1)?,
                    "avatar": row.get::<_, Option<String>>(2)?,
                    "programCount": row.get::<_, i64>(3)?
                }))
            }) {
                Ok(rows) => rows,
                Err(error) => return json!({ "error": error.to_string() }).to_string(),
            };

            let singers: Vec<_> = rows.filter_map(|r| r.ok()).collect();
            json!(singers).to_string()
        }
        Err(error) => json!({ "error": error.to_string() }).to_string(),
    }
}

fn build_artist_detail_json(db_path: &str, artist_id: i64) -> String {
    match RadioGolhaCore::open(db_path) {
        Ok(core) => {
            let conn = core.connection();

            let query_artist_payload = |id: i64| -> Result<(i64, String, Option<String>, Option<String>, i64), String> {
                conn.query_row(
                    "
                    SELECT
                      a.id,
                      a.name,
                      a.avatar,
                      (
                        SELECT i.name
                        FROM program_performers pp
                        JOIN performer p ON p.id = pp.performer_id
                        LEFT JOIN instrument i ON i.id = pp.instrument_id
                        WHERE p.artist_id = a.id AND i.name IS NOT NULL AND TRIM(i.name) <> ''
                        GROUP BY i.id, i.name
                        ORDER BY COUNT(*) DESC, i.name ASC
                        LIMIT 1
                      ) AS instrument_name,
                      (
                        SELECT COUNT(DISTINCT p2.id)
                        FROM program p2
                        WHERE EXISTS (
                            SELECT 1
                            FROM program_singers ps
                            JOIN singer s ON s.id = ps.singer_id
                            WHERE ps.program_id = p2.id AND s.artist_id = a.id
                        )
                        OR EXISTS (
                            SELECT 1
                            FROM program_performers pp
                            JOIN performer p ON p.id = pp.performer_id
                            WHERE pp.program_id = p2.id AND p.artist_id = a.id
                        )
                      ) AS track_count
                    FROM artist a
                    WHERE a.id = ?1
                    ",
                    [id],
                    |row| {
                        Ok((
                            row.get::<_, i64>(0)?,
                            row.get::<_, String>(1)?,
                            row.get::<_, Option<String>>(2)?,
                            row.get::<_, Option<String>>(3)?,
                            row.get::<_, i64>(4)?,
                        ))
                    },
                )
                .map_err(|error| error.to_string())
            };

            // Prefer direct artist id. If it has no related tracks, resolve through singer/performer ids.
            let direct_payload = query_artist_payload(artist_id).ok();
            let resolved_artist_id = if let Some(payload) = &direct_payload {
                if payload.4 > 0 {
                    payload.0
                } else {
                    conn.query_row("SELECT artist_id FROM singer WHERE id = ?1", [artist_id], |row| row.get::<_, i64>(0))
                        .or_else(|_| conn.query_row("SELECT artist_id FROM performer WHERE id = ?1", [artist_id], |row| row.get::<_, i64>(0)))
                        .unwrap_or(payload.0)
                }
            } else {
                match conn
                    .query_row("SELECT artist_id FROM singer WHERE id = ?1", [artist_id], |row| row.get::<_, i64>(0))
                    .or_else(|_| conn.query_row("SELECT artist_id FROM performer WHERE id = ?1", [artist_id], |row| row.get::<_, i64>(0)))
                {
                    Ok(id) => id,
                    Err(error) => return json!({ "error": error.to_string() }).to_string(),
                }
            };

            let payload = match query_artist_payload(resolved_artist_id) {
                Ok(value) => value,
                Err(error) => return json!({ "error": error }).to_string(),
            };

            let mut stmt = match conn.prepare(
                "
                WITH artist_program_ids AS (
                    SELECT DISTINCT ps.program_id AS program_id
                    FROM program_singers ps
                    JOIN singer s ON s.id = ps.singer_id
                    WHERE s.artist_id = ?1
                    UNION
                    SELECT DISTINCT pp.program_id AS program_id
                    FROM program_performers pp
                    JOIN performer pf ON pf.id = pp.performer_id
                    WHERE pf.artist_id = ?1
                )
                SELECT
                    p.id,
                    p.title,
                    p.no,
                    COALESCE(
                        (
                            SELECT GROUP_CONCAT(name, ' و ')
                            FROM (
                                SELECT DISTINCT a2.name AS name
                                FROM program_singers ps
                                JOIN singer s ON s.id = ps.singer_id
                                JOIN artist a2 ON a2.id = s.artist_id
                                WHERE ps.program_id = p.id
                                ORDER BY a2.name ASC
                            )
                        ),
                        'ناشناس'
                    ) AS artist_names,
                    (
                        SELECT GROUP_CONCAT(m.name, ' و ')
                        FROM program_modes pm
                        JOIN mode m ON m.id = pm.mode_id
                        WHERE pm.program_id = p.id
                    ) AS mode_names,
                    (SELECT MAX(end_time) FROM program_timeline WHERE program_id = p.id) AS duration,
                    p.audio_url
                FROM artist_program_ids ap
                JOIN program p ON p.id = ap.program_id
                ORDER BY p.no ASC, p.id ASC
                ",
            ) {
                Ok(stmt) => stmt,
                Err(error) => return json!({ "error": error.to_string() }).to_string(),
            };

            let rows = match stmt.query_map([resolved_artist_id], |row| {
                Ok(json!({
                    "id": row.get::<_, i64>(0)?,
                    "title": row.get::<_, String>(1)?,
                    "no": row.get::<_, i64>(2)?,
                    "artist": row.get::<_, String>(3)?,
                    "mode": row.get::<_, Option<String>>(4)?,
                    "duration": row.get::<_, Option<String>>(5)?,
                    "audioUrl": row.get::<_, Option<String>>(6)?
                }))
            }) {
                Ok(rows) => rows,
                Err(error) => return json!({ "error": error.to_string() }).to_string(),
            };

            let tracks: Vec<_> = rows.filter_map(|item| item.ok()).collect();

            let mut category_counts_stmt = match conn.prepare(
                "
                WITH artist_program_ids AS (
                    SELECT DISTINCT ps.program_id AS program_id
                    FROM program_singers ps
                    JOIN singer s ON s.id = ps.singer_id
                    WHERE s.artist_id = ?1
                    UNION
                    SELECT DISTINCT pp.program_id AS program_id
                    FROM program_performers pp
                    JOIN performer pf ON pf.id = pp.performer_id
                    WHERE pf.artist_id = ?1
                )
                SELECT c.id, c.title_fa, COUNT(DISTINCT p.id) AS total
                FROM artist_program_ids ap
                JOIN program p ON p.id = ap.program_id
                JOIN category c ON c.id = p.category_id
                GROUP BY c.id, c.title_fa
                ORDER BY total DESC, c.id ASC
                ",
            ) {
                Ok(stmt) => stmt,
                Err(error) => return json!({ "error": error.to_string() }).to_string(),
            };
            let category_counts_rows = match category_counts_stmt.query_map([resolved_artist_id], |row| {
                Ok(json!({
                    "categoryId": row.get::<_, i64>(0)?,
                    "title": row.get::<_, String>(1)?,
                    "count": row.get::<_, i64>(2)?
                }))
            }) {
                Ok(rows) => rows,
                Err(error) => return json!({ "error": error.to_string() }).to_string(),
            };
            let category_counts: Vec<_> = category_counts_rows.filter_map(|item| item.ok()).collect();

            let mut top_modes_stmt = match conn.prepare(
                "
                WITH artist_program_ids AS (
                    SELECT DISTINCT ps.program_id AS program_id
                    FROM program_singers ps
                    JOIN singer s ON s.id = ps.singer_id
                    WHERE s.artist_id = ?1
                    UNION
                    SELECT DISTINCT pp.program_id AS program_id
                    FROM program_performers pp
                    JOIN performer pf ON pf.id = pp.performer_id
                    WHERE pf.artist_id = ?1
                )
                SELECT m.name, COUNT(*) AS total
                FROM artist_program_ids ap
                JOIN program_modes pm ON pm.program_id = ap.program_id
                JOIN mode m ON m.id = pm.mode_id
                WHERE m.name IS NOT NULL AND TRIM(m.name) <> ''
                GROUP BY m.id, m.name
                ORDER BY total DESC, m.name ASC
                LIMIT 4
                ",
            ) {
                Ok(stmt) => stmt,
                Err(error) => return json!({ "error": error.to_string() }).to_string(),
            };
            let top_modes_rows = match top_modes_stmt.query_map([resolved_artist_id], |row| {
                Ok(row.get::<_, String>(0)?)
            }) {
                Ok(rows) => rows,
                Err(error) => return json!({ "error": error.to_string() }).to_string(),
            };
            let top_modes: Vec<_> = top_modes_rows.filter_map(|item| item.ok()).collect();

            let mut singer_collab_stmt = match conn.prepare(
                "
                WITH artist_program_ids AS (
                    SELECT DISTINCT ps.program_id AS program_id
                    FROM program_singers ps
                    JOIN singer s ON s.id = ps.singer_id
                    WHERE s.artist_id = ?1
                    UNION
                    SELECT DISTINCT pp.program_id AS program_id
                    FROM program_performers pp
                    JOIN performer pf ON pf.id = pp.performer_id
                    WHERE pf.artist_id = ?1
                )
                SELECT a.id, a.name, a.avatar, COUNT(DISTINCT ps.program_id) AS shared_count
                FROM artist_program_ids ap
                JOIN program_singers ps ON ps.program_id = ap.program_id
                JOIN singer s ON s.id = ps.singer_id
                JOIN artist a ON a.id = s.artist_id
                WHERE a.id <> ?1
                GROUP BY a.id, a.name, a.avatar
                ORDER BY shared_count DESC, a.name ASC
                LIMIT 2
                ",
            ) {
                Ok(stmt) => stmt,
                Err(error) => return json!({ "error": error.to_string() }).to_string(),
            };

            let mut collaborators: Vec<serde_json::Value> = Vec::new();
            let mut collaborator_ids = std::collections::HashSet::new();

            let singer_rows = match singer_collab_stmt.query_map([resolved_artist_id], |row| {
                Ok((
                    row.get::<_, i64>(0)?,
                    row.get::<_, String>(1)?,
                    row.get::<_, Option<String>>(2)?,
                    row.get::<_, i64>(3)?,
                ))
            }) {
                Ok(rows) => rows,
                Err(error) => return json!({ "error": error.to_string() }).to_string(),
            };

            for (id, name, avatar, shared_count) in singer_rows.filter_map(|r| r.ok()) {
                collaborator_ids.insert(id);
                collaborators.push(json!({
                    "id": id,
                    "name": name,
                    "avatar": avatar,
                    "kind": "singer",
                    "role": "خواننده",
                    "sharedCount": shared_count
                }));
            }

            if collaborators.iter().filter(|item| item["kind"] == "singer").count() < 2 {
                let needed = 2 - collaborators.iter().filter(|item| item["kind"] == "singer").count();
                let mut fallback_singers_stmt = match conn.prepare(
                    "
                    SELECT a.id, a.name, a.avatar, COUNT(DISTINCT ps.program_id) AS total
                    FROM program_singers ps
                    JOIN singer s ON s.id = ps.singer_id
                    JOIN artist a ON a.id = s.artist_id
                    WHERE a.id <> ?1
                    GROUP BY a.id, a.name, a.avatar
                    ORDER BY total DESC, a.name ASC
                    LIMIT 20
                    ",
                ) {
                    Ok(stmt) => stmt,
                    Err(error) => return json!({ "error": error.to_string() }).to_string(),
                };

                let fallback_rows = match fallback_singers_stmt.query_map([resolved_artist_id], |row| {
                    Ok((
                        row.get::<_, i64>(0)?,
                        row.get::<_, String>(1)?,
                        row.get::<_, Option<String>>(2)?,
                        row.get::<_, i64>(3)?,
                    ))
                }) {
                    Ok(rows) => rows,
                    Err(error) => return json!({ "error": error.to_string() }).to_string(),
                };

                for (id, name, avatar, total) in fallback_rows.filter_map(|r| r.ok()) {
                    if collaborator_ids.contains(&id) {
                        continue;
                    }
                    collaborator_ids.insert(id);
                    collaborators.push(json!({
                        "id": id,
                        "name": name,
                        "avatar": avatar,
                        "kind": "singer",
                        "role": "خواننده",
                        "sharedCount": total
                    }));
                    if collaborators.iter().filter(|item| item["kind"] == "singer").count() >= 2 || needed == 0 {
                        break;
                    }
                }
            }

            let mut musician_collab_stmt = match conn.prepare(
                "
                WITH artist_program_ids AS (
                    SELECT DISTINCT ps.program_id AS program_id
                    FROM program_singers ps
                    JOIN singer s ON s.id = ps.singer_id
                    WHERE s.artist_id = ?1
                    UNION
                    SELECT DISTINCT pp.program_id AS program_id
                    FROM program_performers pp
                    JOIN performer pf ON pf.id = pp.performer_id
                    WHERE pf.artist_id = ?1
                )
                SELECT
                    a.id,
                    a.name,
                    a.avatar,
                    COALESCE(
                      (
                        SELECT i.name
                        FROM program_performers pp2
                        LEFT JOIN instrument i ON i.id = pp2.instrument_id
                        WHERE pp2.performer_id = p.id AND i.name IS NOT NULL AND TRIM(i.name) <> ''
                        GROUP BY i.id, i.name
                        ORDER BY COUNT(*) DESC, i.name ASC
                        LIMIT 1
                      ),
                      'نوازنده'
                    ) AS instrument_name,
                    COUNT(DISTINCT pp.program_id) AS shared_count
                FROM artist_program_ids ap
                JOIN program_performers pp ON pp.program_id = ap.program_id
                JOIN performer p ON p.id = pp.performer_id
                JOIN artist a ON a.id = p.artist_id
                WHERE a.id <> ?1
                GROUP BY p.id, a.id, a.name, a.avatar
                ORDER BY shared_count DESC, a.name ASC
                LIMIT 2
                ",
            ) {
                Ok(stmt) => stmt,
                Err(error) => return json!({ "error": error.to_string() }).to_string(),
            };

            let musician_rows = match musician_collab_stmt.query_map([resolved_artist_id], |row| {
                Ok((
                    row.get::<_, i64>(0)?,
                    row.get::<_, String>(1)?,
                    row.get::<_, Option<String>>(2)?,
                    row.get::<_, String>(3)?,
                    row.get::<_, i64>(4)?,
                ))
            }) {
                Ok(rows) => rows,
                Err(error) => return json!({ "error": error.to_string() }).to_string(),
            };

            for (id, name, avatar, instrument, shared_count) in musician_rows.filter_map(|r| r.ok()) {
                if collaborator_ids.contains(&id) {
                    continue;
                }
                collaborator_ids.insert(id);
                collaborators.push(json!({
                    "id": id,
                    "name": name,
                    "avatar": avatar,
                    "kind": "musician",
                    "role": instrument,
                    "sharedCount": shared_count
                }));
            }

            if collaborators.iter().filter(|item| item["kind"] == "musician").count() < 2 {
                let mut fallback_musicians_stmt = match conn.prepare(
                    "
                    SELECT
                        a.id,
                        a.name,
                        a.avatar,
                        COALESCE(
                          (
                            SELECT i.name
                            FROM program_performers pp2
                            LEFT JOIN instrument i ON i.id = pp2.instrument_id
                            WHERE pp2.performer_id = p.id AND i.name IS NOT NULL AND TRIM(i.name) <> ''
                            GROUP BY i.id, i.name
                            ORDER BY COUNT(*) DESC, i.name ASC
                            LIMIT 1
                          ),
                          'نوازنده'
                        ) AS instrument_name,
                        COUNT(DISTINCT pp.program_id) AS total
                    FROM program_performers pp
                    JOIN performer p ON p.id = pp.performer_id
                    JOIN artist a ON a.id = p.artist_id
                    WHERE a.id <> ?1
                    GROUP BY p.id, a.id, a.name, a.avatar
                    ORDER BY total DESC, a.name ASC
                    LIMIT 20
                    ",
                ) {
                    Ok(stmt) => stmt,
                    Err(error) => return json!({ "error": error.to_string() }).to_string(),
                };

                let fallback_rows = match fallback_musicians_stmt.query_map([resolved_artist_id], |row| {
                    Ok((
                        row.get::<_, i64>(0)?,
                        row.get::<_, String>(1)?,
                        row.get::<_, Option<String>>(2)?,
                        row.get::<_, String>(3)?,
                        row.get::<_, i64>(4)?,
                    ))
                }) {
                    Ok(rows) => rows,
                    Err(error) => return json!({ "error": error.to_string() }).to_string(),
                };

                for (id, name, avatar, instrument, total) in fallback_rows.filter_map(|r| r.ok()) {
                    if collaborator_ids.contains(&id) {
                        continue;
                    }
                    collaborator_ids.insert(id);
                    collaborators.push(json!({
                        "id": id,
                        "name": name,
                        "avatar": avatar,
                        "kind": "musician",
                        "role": instrument,
                        "sharedCount": total
                    }));
                    if collaborators.iter().filter(|item| item["kind"] == "musician").count() >= 2 {
                        break;
                    }
                }
            }

            if collaborators.len() < 4 {
                let mut any_artist_stmt = match conn.prepare(
                    "
                    SELECT a.id, a.name, a.avatar
                    FROM artist a
                    WHERE a.id <> ?1
                    ORDER BY a.name ASC
                    LIMIT 200
                    ",
                ) {
                    Ok(stmt) => stmt,
                    Err(error) => return json!({ "error": error.to_string() }).to_string(),
                };

                let any_rows = match any_artist_stmt.query_map([resolved_artist_id], |row| {
                    Ok((
                        row.get::<_, i64>(0)?,
                        row.get::<_, String>(1)?,
                        row.get::<_, Option<String>>(2)?,
                    ))
                }) {
                    Ok(rows) => rows,
                    Err(error) => return json!({ "error": error.to_string() }).to_string(),
                };

                for (id, name, avatar) in any_rows.filter_map(|r| r.ok()) {
                    if collaborator_ids.contains(&id) {
                        continue;
                    }
                    collaborator_ids.insert(id);
                    collaborators.push(json!({
                        "id": id,
                        "name": name,
                        "avatar": avatar,
                        "kind": "singer",
                        "role": "خواننده",
                        "sharedCount": 0
                    }));
                    if collaborators.len() >= 4 {
                        break;
                    }
                }
            }

            let collaborators: Vec<_> = collaborators.into_iter().take(4).collect();

            json!({
                "id": payload.0,
                "name": payload.1,
                "avatar": payload.2,
                "instrument": payload.3,
                "trackCount": payload.4,
                "tracks": tracks,
                "categoryCounts": category_counts,
                "collaborators": collaborators,
                "topModes": top_modes
            })
            .to_string()
        }
        Err(error) => json!({ "error": error.to_string() }).to_string(),
    }
}

fn build_top_tracks_json(db_path: &str) -> String {
    match RadioGolhaCore::open(db_path) {
        Ok(core) => {
            let conn = core.connection();
            let artist_images_for_program = |program_id: i64| -> Vec<String> {
                let mut stmt = match conn.prepare(
                    "SELECT DISTINCT a.avatar
                     FROM program_singers ps
                     JOIN singer s ON s.id = ps.singer_id
                     JOIN artist a ON a.id = s.artist_id
                     WHERE ps.program_id = ?1
                       AND a.avatar IS NOT NULL
                       AND TRIM(a.avatar) <> ''
                     ORDER BY a.name ASC
                     LIMIT 5",
                ) {
                    Ok(stmt) => stmt,
                    Err(_) => return vec![],
                };
                stmt.query_map([program_id], |row| row.get::<_, String>(0))
                    .map(|rows| rows.filter_map(|r| r.ok()).collect())
                    .unwrap_or_default()
            };

            let top_tracks = core.random_vocal_track_summaries(10).unwrap_or_default();
            json!(top_tracks.iter().map(|t| json!({
                "id": t.id,
                "title": t.title,
                "artist": t.artist,
                "duration": t.duration.clone().unwrap_or_else(|| "00:00".to_string()),
                "audioUrl": t.audio_url,
                "artistImages": artist_images_for_program(t.id)
            })).collect::<Vec<_>>())
            .to_string()
        }
        Err(err) => json!({ "error": err.to_string() }).to_string(),
    }
}

fn build_home_feed_json(db_path: &str) -> String {
    match RadioGolhaCore::open(db_path) {
        Ok(core) => {
            let conn = core.connection();

            let categories = core.program_categories().unwrap_or_default();
            let cat_breakdown = core.category_breakdown().unwrap_or_default();

            let mut singers_stmt = match conn.prepare(
                "SELECT a.id, a.name, a.avatar, COUNT(DISTINCT ps.program_id) AS total
                 FROM program_singers ps
                 JOIN singer s ON s.id = ps.singer_id
                 JOIN artist a ON a.id = s.artist_id
                 GROUP BY s.id, a.id, a.name, a.avatar
                 ORDER BY
                   CASE
                     WHEN a.avatar IS NOT NULL AND TRIM(a.avatar) <> '' THEN 1
                     ELSE 0
                   END DESC,
                   total DESC,
                   a.name ASC
                 LIMIT 8",
            ) {
                Ok(stmt) => stmt,
                Err(err) => return json!({ "error": err.to_string() }).to_string(),
            };
            let singers: Vec<_> = singers_stmt
                .query_map([], |row| {
                    Ok(json!({
                        "id": row.get::<_, i64>(0)?,
                        "name": row.get::<_, String>(1)?,
                        "avatar": row.get::<_, Option<String>>(2)?,
                        "programCount": row.get::<_, i64>(3)?
                    }))
                })
                .map(|rows| rows.filter_map(|item| item.ok()).collect())
                .unwrap_or_default();

            let dastgahs = core.top_modes(10).unwrap_or_default();

            let mut musicians_stmt = match conn.prepare(
                "SELECT
                   a.id,
                   a.name,
                   (
                     SELECT i.name
                     FROM program_performers pp2
                     LEFT JOIN instrument i ON i.id = pp2.instrument_id
                     WHERE pp2.performer_id = p.id AND i.name IS NOT NULL AND TRIM(i.name) <> ''
                     GROUP BY i.id, i.name
                     ORDER BY COUNT(*) DESC, i.name ASC
                     LIMIT 1
                   ) AS instrument_name,
                   a.avatar,
                   COUNT(DISTINCT pp.program_id) AS total
                 FROM program_performers pp
                 JOIN performer p ON p.id = pp.performer_id
                 JOIN artist a ON a.id = p.artist_id
                 GROUP BY p.id, a.id, a.name, a.avatar
                 ORDER BY total DESC, a.name ASC
                 LIMIT 8",
            ) {
                Ok(stmt) => stmt,
                Err(err) => return json!({ "error": err.to_string() }).to_string(),
            };
            let musicians: Vec<_> = musicians_stmt
                .query_map([], |row| {
                    let instrument: Option<String> = row.get(2)?;
                    Ok(json!({
                        "id": row.get::<_, i64>(0)?,
                        "name": row.get::<_, String>(1)?,
                        "instrument": instrument
                            .filter(|value| !value.trim().is_empty())
                            .unwrap_or_else(|| "نوازنده".to_string()),
                        "avatar": row.get::<_, Option<String>>(3)?,
                        "programCount": row.get::<_, i64>(4)?
                    }))
                })
                .map(|rows| rows.filter_map(|item| item.ok()).collect())
                .unwrap_or_default();

            let top_tracks = core.random_vocal_track_summaries(10).unwrap_or_default();
            let artist_images_for_program = |program_id: i64| -> Vec<String> {
                let mut stmt = match conn.prepare(
                    "SELECT DISTINCT a.avatar
                     FROM program_singers ps
                     JOIN singer s ON s.id = ps.singer_id
                     JOIN artist a ON a.id = s.artist_id
                     WHERE ps.program_id = ?1
                       AND a.avatar IS NOT NULL
                       AND TRIM(a.avatar) <> ''
                     ORDER BY a.name ASC
                     LIMIT 5",
                ) {
                    Ok(stmt) => stmt,
                    Err(_) => return vec![],
                };
                stmt.query_map([program_id], |row| row.get::<_, String>(0))
                    .map(|rows| rows.filter_map(|r| r.ok()).collect())
                    .unwrap_or_default()
            };
            let duets_json = core.get_duet_pairs_raw().unwrap_or_else(|_| "[]".to_string());
            let duets: serde_json::Value = serde_json::from_str(&duets_json).unwrap_or(json!([]));

            json!({
                "programs": cat_breakdown.iter().map(|item| {
                    json!({ "title": item.name, "episodeCount": item.total })
                }).collect::<Vec<_>>(),
                "categories": categories.iter().map(|c| {
                    let count = cat_breakdown.iter()
                        .find(|item| item.name == c.title_fa)
                        .map(|item| item.total)
                        .unwrap_or(0);
                    json!({ "id": c.id, "title": c.title_fa, "episodeCount": count })
                }).collect::<Vec<_>>(),
                "singers": singers,
                "dastgahs": dastgahs.iter().map(|m| json!({ "name": m.name })).collect::<Vec<_>>(),
                "musicians": musicians,
                "topTracks": top_tracks.iter().map(|t| json!({
                    "id": t.id,
                    "title": t.title,
                    "artist": t.artist,
                    "duration": t.duration.clone().unwrap_or_else(|| "00:00".to_string()),
                    "audioUrl": t.audio_url,
                    "artistImages": artist_images_for_program(t.id)
                })).collect::<Vec<_>>(),
                "duets": duets
            })
            .to_string()
        }
        Err(err) => json!({ "error": err.to_string() }).to_string(),
    }
}
