use radiogolha_core::{LookupKind, ProgramSearchFilters, ProgramSortField, RadioGolhaCore, SearchMatchMode, SortDirection};
use rusqlite::Connection;
use serde::{Deserialize, Serialize};

pub use radiogolha_core::models::{ProgramDetail, ProgramSearchOptions, ProgramSearchResponse};

pub type AdapterResult<T> = Result<T, String>;

#[derive(Debug, Clone, Serialize)]
#[serde(rename_all = "camelCase")]
pub struct HomePayload {
    pub programs: Vec<HomeProgramItem>,
    pub singers: Vec<HomeArtistItem>,
    pub modes: Vec<HomeModeItem>,
    pub musicians: Vec<HomeMusicianItem>,
    pub top_tracks: Vec<TrackItem>,
}

#[derive(Debug, Clone, Serialize)]
#[serde(rename_all = "camelCase")]
pub struct HomeProgramItem {
    pub id: i64,
    pub title: String,
    pub episode_count: i64,
}

#[derive(Debug, Clone, Serialize)]
#[serde(rename_all = "camelCase")]
pub struct HomeModeItem {
    pub id: i64,
    pub name: String,
    pub usage_count: i64,
}

#[derive(Debug, Clone, Serialize)]
#[serde(rename_all = "camelCase")]
pub struct HomeArtistItem {
    pub id: i64,
    pub name: String,
    pub avatar: Option<String>,
    pub program_count: i64,
}

#[derive(Debug, Clone, Serialize)]
#[serde(rename_all = "camelCase")]
pub struct HomeMusicianItem {
    pub id: i64,
    pub name: String,
    pub instrument: String,
    pub avatar: Option<String>,
    pub program_count: i64,
}

#[derive(Debug, Clone, Serialize)]
#[serde(rename_all = "camelCase")]
pub struct TrackItem {
    pub id: i64,
    pub title: String,
    pub artist: String,
    pub duration: String,
    pub audio_url: Option<String>,
    pub mode: Option<String>,
    pub singer_avatars: Vec<String>,
}

#[derive(Debug, Clone, Serialize)]
#[serde(rename_all = "camelCase")]
pub struct ArtistDetailPayload {
    pub id: i64,
    pub name: String,
    pub avatar: Option<String>,
    pub instrument: Option<String>,
    pub track_count: i64,
    pub tracks: Vec<TrackItem>,
    pub category_counts: Vec<ArtistCategoryCountItem>,
}

#[derive(Debug, Clone, Serialize)]
#[serde(rename_all = "camelCase")]
pub struct ArtistCategoryCountItem {
    pub category_id: i64,
    pub title: String,
    pub count: i64,
}

#[derive(Debug, Clone, Serialize)]
#[serde(rename_all = "camelCase")]
pub struct ProgramTracksPayload {
    pub id: i64,
    pub title: String,
    pub category_name: String,
    pub no: i64,
    pub sub_no: Option<String>,
    pub duration: Option<String>,
    pub audio_url: Option<String>,
    pub tracks: Vec<TrackItem>,
}

#[derive(Debug, Clone, Serialize)]
#[serde(rename_all = "camelCase")]
pub struct TopBarSearchResult {
    pub kind: String,
    pub id: i64,
    pub title: String,
    pub subtitle: String,
    pub avatar: Option<String>,
    pub track_id: Option<i64>,
}

#[derive(Debug, Clone, Deserialize, Default)]
#[serde(rename_all = "camelCase")]
pub struct SearchProgramsPayload {
    pub transcript_query: Option<String>,
    pub page: Option<i64>,
    pub category_ids: Option<Vec<i64>>,
    pub mode_ids: Option<Vec<i64>>,
    pub mode_match: Option<String>,
    pub orchestra_ids: Option<Vec<i64>>,
    pub orchestra_match: Option<String>,
    pub instrument_ids: Option<Vec<i64>>,
    pub instrument_match: Option<String>,
    pub singer_ids: Option<Vec<i64>>,
    pub singer_match: Option<String>,
    pub poet_ids: Option<Vec<i64>>,
    pub poet_match: Option<String>,
    pub announcer_ids: Option<Vec<i64>>,
    pub announcer_match: Option<String>,
    pub composer_ids: Option<Vec<i64>>,
    pub composer_match: Option<String>,
    pub arranger_ids: Option<Vec<i64>>,
    pub arranger_match: Option<String>,
    pub performer_ids: Option<Vec<i64>>,
    pub performer_match: Option<String>,
    pub orchestra_leader_ids: Option<Vec<i64>>,
    pub orchestra_leader_match: Option<String>,
}

fn open_core(db_path: &str) -> AdapterResult<RadioGolhaCore> {
    RadioGolhaCore::open(db_path).map_err(|error| error.to_string())
}

fn parse_match_mode(value: Option<String>) -> SearchMatchMode {
    match value.as_deref() {
        Some("all") => SearchMatchMode::All,
        _ => SearchMatchMode::Any,
    }
}

fn split_avatar_list(value: Option<String>) -> Vec<String> {
    value
        .unwrap_or_default()
        .split('|')
        .map(str::trim)
        .filter(|item| !item.is_empty())
        .map(ToOwned::to_owned)
        .collect()
}

pub fn get_home_data(db_path: &str) -> AdapterResult<HomePayload> {
    let core = open_core(db_path)?;
    let conn = core.connection();

    let mut category_stmt = conn
        .prepare(
            "
            SELECT c.id, c.title_fa, COUNT(*) AS total
            FROM program p
            JOIN category c ON c.id = p.category_id
            GROUP BY c.id, c.title_fa
            ORDER BY total DESC, c.id ASC
            ",
        )
        .map_err(|error| error.to_string())?;
    let programs = category_stmt
        .query_map([], |row| {
            Ok(HomeProgramItem {
                id: row.get(0)?,
                title: row.get(1)?,
                episode_count: row.get(2)?,
            })
        })
        .map_err(|error| error.to_string())?
        .collect::<Result<Vec<_>, _>>()
        .map_err(|error| error.to_string())?;

    let modes = core
        .browse_lookup_items(LookupKind::Modes, "", 1)
        .map_err(|error| error.to_string())?
        .rows
        .into_iter()
        .map(|item| HomeModeItem {
            id: item.id,
            name: item.name,
            usage_count: item.usage_count,
        })
        .collect();

    let singers = list_singers_from_conn(conn, Some(8))?;
    let musicians = list_musicians_from_conn(conn, Some(8))?;
    let top_tracks = random_track_items(&core, 10)?;

    Ok(HomePayload { programs, singers, modes, musicians, top_tracks })
}

pub fn get_top_tracks(db_path: &str, limit: usize) -> AdapterResult<Vec<TrackItem>> {
    let core = open_core(db_path)?;
    random_track_items(&core, limit)
}

pub fn get_singers(db_path: &str) -> AdapterResult<Vec<HomeArtistItem>> {
    let core = open_core(db_path)?;
    list_singers_from_conn(core.connection(), None)
}

pub fn get_musicians(db_path: &str) -> AdapterResult<Vec<HomeMusicianItem>> {
    let core = open_core(db_path)?;
    list_musicians_from_conn(core.connection(), None)
}

pub fn get_modes(db_path: &str) -> AdapterResult<Vec<HomeModeItem>> {
    let core = open_core(db_path)?;
    let items = core
        .list_lookup_items(LookupKind::Modes, "", 1, 1000)
        .map_err(|error| error.to_string())?;
    Ok(items
        .into_iter()
        .map(|item| HomeModeItem { id: item.id, name: item.name, usage_count: item.usage_count })
        .collect())
}

pub fn get_search_options(db_path: &str) -> AdapterResult<ProgramSearchOptions> {
    let core = open_core(db_path)?;
    core.program_search_options().map_err(|error| error.to_string())
}

pub fn top_bar_search(db_path: &str, query: &str, limit: i64) -> AdapterResult<Vec<TopBarSearchResult>> {
    let trimmed = query.trim();
    if trimmed.is_empty() {
        return Ok(Vec::new());
    }

    let safe_limit = if limit <= 0 { 10 } else { limit.min(30) };
    let like = format!("%{}%", trimmed);
    let starts_with = format!("{}%", trimmed);
    let core = open_core(db_path)?;
    let conn = core.connection();

    let mut stmt = conn.prepare(
        "
        WITH artist_hits AS (
            SELECT
                'artist' AS kind,
                a.id AS id,
                a.name AS title,
                CASE
                    WHEN EXISTS (SELECT 1 FROM singer s WHERE s.artist_id = a.id) THEN 'خواننده'
                    WHEN EXISTS (SELECT 1 FROM performer p WHERE p.artist_id = a.id) THEN COALESCE(
                        (
                            SELECT 'نوازنده ' || i.name
                            FROM program_performers pp
                            JOIN performer p2 ON p2.id = pp.performer_id
                            LEFT JOIN instrument i ON i.id = pp.instrument_id
                            WHERE p2.artist_id = a.id
                              AND i.name IS NOT NULL
                              AND TRIM(i.name) <> ''
                            GROUP BY i.id, i.name
                            ORDER BY COUNT(*) DESC, i.name ASC
                            LIMIT 1
                        ),
                        'نوازنده'
                    )
                    WHEN EXISTS (SELECT 1 FROM poet p WHERE p.artist_id = a.id) THEN 'شاعر'
                    WHEN EXISTS (SELECT 1 FROM announcer n WHERE n.artist_id = a.id) THEN 'گوینده'
                    WHEN EXISTS (SELECT 1 FROM composer c WHERE c.artist_id = a.id) THEN 'آهنگساز'
                    WHEN EXISTS (SELECT 1 FROM arranger r WHERE r.artist_id = a.id) THEN 'تنظیم‌کننده'
                    WHEN EXISTS (SELECT 1 FROM orchestra_leader l WHERE l.artist_id = a.id) THEN 'رهبر ارکستر'
                    ELSE 'هنرمند'
                END AS subtitle,
                a.avatar AS avatar,
                NULL AS track_id,
                CASE WHEN a.name LIKE ?2 THEN 0 ELSE 1 END AS rank,
                0 AS kind_priority
            FROM artist a
            WHERE a.name LIKE ?1
            LIMIT ?3
        ),
        track_hits AS (
            SELECT
                'track' AS kind,
                p.id AS id,
                COALESCE(NULLIF(TRIM(p.title), ''), 'برنامه ' || p.no) AS title,
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
                ) AS subtitle,
                NULL AS avatar,
                p.id AS track_id,
                CASE WHEN COALESCE(p.title, '') LIKE ?2 THEN 0 ELSE 1 END AS rank,
                1 AS kind_priority
            FROM program p
            WHERE COALESCE(p.title, '') LIKE ?1
            LIMIT ?3
        )
        SELECT kind, id, title, subtitle, avatar, track_id
        FROM (
            SELECT * FROM artist_hits
            UNION ALL
            SELECT * FROM track_hits
        )
        ORDER BY rank ASC, kind_priority ASC, title ASC
        LIMIT ?3
        ",
    ).map_err(|error| error.to_string())?;

    stmt
        .query_map((like, starts_with, safe_limit), |row| {
            Ok(TopBarSearchResult {
                kind: row.get(0)?,
                id: row.get(1)?,
                title: row.get(2)?,
                subtitle: row.get(3)?,
                avatar: row.get(4)?,
                track_id: row.get(5)?,
            })
        })
        .map_err(|error| error.to_string())?
        .collect::<Result<Vec<_>, _>>()
        .map_err(|error| error.to_string())
}

pub fn search_programs(db_path: &str, payload: SearchProgramsPayload) -> AdapterResult<ProgramSearchResponse> {
    let core = open_core(db_path)?;
    let filters = ProgramSearchFilters {
        transcript_query: payload.transcript_query.filter(|value| !value.trim().is_empty()),
        category_ids: payload.category_ids.unwrap_or_default(),
        mode_ids: payload.mode_ids.unwrap_or_default(),
        mode_match: parse_match_mode(payload.mode_match),
        orchestra_ids: payload.orchestra_ids.unwrap_or_default(),
        orchestra_match: parse_match_mode(payload.orchestra_match),
        instrument_ids: payload.instrument_ids.unwrap_or_default(),
        instrument_match: parse_match_mode(payload.instrument_match),
        singer_ids: payload.singer_ids.unwrap_or_default(),
        singer_match: parse_match_mode(payload.singer_match),
        poet_ids: payload.poet_ids.unwrap_or_default(),
        poet_match: parse_match_mode(payload.poet_match),
        announcer_ids: payload.announcer_ids.unwrap_or_default(),
        announcer_match: parse_match_mode(payload.announcer_match),
        composer_ids: payload.composer_ids.unwrap_or_default(),
        composer_match: parse_match_mode(payload.composer_match),
        arranger_ids: payload.arranger_ids.unwrap_or_default(),
        arranger_match: parse_match_mode(payload.arranger_match),
        performer_ids: payload.performer_ids.unwrap_or_default(),
        performer_match: parse_match_mode(payload.performer_match),
        orchestra_leader_ids: payload.orchestra_leader_ids.unwrap_or_default(),
        orchestra_leader_match: parse_match_mode(payload.orchestra_leader_match),
        sort_field: ProgramSortField::No,
        sort_direction: SortDirection::Asc,
    };
    core.search_programs(&filters, payload.page.unwrap_or(1)).map_err(|error| error.to_string())
}

pub fn get_artist_detail(db_path: &str, artist_id: i64) -> AdapterResult<ArtistDetailPayload> {
    let core = open_core(db_path)?;
    let conn = core.connection();

    let query_artist_payload = |id: i64| -> AdapterResult<(i64, String, Option<String>, Option<String>, i64)> {
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
            |row| Ok((row.get(0)?, row.get(1)?, row.get(2)?, row.get(3)?, row.get(4)?)),
        )
        .map_err(|error| error.to_string())
    };

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
        conn.query_row("SELECT artist_id FROM singer WHERE id = ?1", [artist_id], |row| row.get::<_, i64>(0))
            .or_else(|_| conn.query_row("SELECT artist_id FROM performer WHERE id = ?1", [artist_id], |row| row.get::<_, i64>(0)))
            .map_err(|error| error.to_string())?
    };

    let payload = query_artist_payload(resolved_artist_id)?;
    let tracks = programs_for_artist(conn, resolved_artist_id)?;
    let category_counts = category_counts_for_artist(conn, resolved_artist_id)?;

    Ok(ArtistDetailPayload {
        id: payload.0,
        name: payload.1,
        avatar: payload.2,
        instrument: payload.3,
        track_count: payload.4,
        tracks,
        category_counts,
    })
}

pub fn get_program_tracks(db_path: &str, program_id: i64) -> AdapterResult<ProgramTracksPayload> {
    let core = open_core(db_path)?;
    let detail = core
        .get_program_detail(program_id)
        .map_err(|error| error.to_string())?
        .ok_or_else(|| format!("Program not found: {program_id}"))?;

    let artist = detail
        .singers
        .iter()
        .map(|artist| artist.name.as_str())
        .collect::<Vec<_>>()
        .join(" و ");
    let mode = detail.modes.first().cloned();
    let singer_avatars = detail
        .singers
        .iter()
        .filter_map(|artist| artist.avatar.clone())
        .collect::<Vec<_>>();

    Ok(ProgramTracksPayload {
        id: detail.id,
        title: detail.title.clone(),
        category_name: detail.category_name,
        no: detail.no,
        sub_no: detail.sub_no,
        duration: detail.duration.clone(),
        audio_url: detail.audio_url.clone(),
        tracks: vec![TrackItem {
            id: detail.id,
            title: detail.title,
            artist: if artist.is_empty() { "ناشناس".to_string() } else { artist },
            duration: detail.duration.unwrap_or_else(|| "نامشخص".to_string()),
            audio_url: detail.audio_url,
            mode,
            singer_avatars,
        }],
    })
}

pub fn get_track_detail(db_path: &str, program_id: i64) -> AdapterResult<ProgramDetail> {
    let core = open_core(db_path)?;
    core.get_program_detail(program_id)
        .map_err(|error| error.to_string())?
        .ok_or_else(|| format!("Program not found: {program_id}"))
}

fn random_track_items(core: &RadioGolhaCore, limit: usize) -> AdapterResult<Vec<TrackItem>> {
    Ok(core
        .random_vocal_track_summaries(limit)
        .map_err(|error| error.to_string())?
        .into_iter()
        .map(|item| TrackItem {
            id: item.id,
            title: item.title,
            artist: item.artist,
            duration: item.duration.unwrap_or_else(|| "نامشخص".to_string()),
            audio_url: Some(item.audio_url),
            mode: None,
            singer_avatars: Vec::new(),
        })
        .collect())
}

fn list_singers_from_conn(conn: &Connection, limit: Option<usize>) -> AdapterResult<Vec<HomeArtistItem>> {
    let mut sql = String::from(
        "
        SELECT a.id, a.name, a.avatar, COUNT(DISTINCT ps.program_id) AS total
        FROM program_singers ps
        JOIN singer s ON s.id = ps.singer_id
        JOIN artist a ON a.id = s.artist_id
        GROUP BY s.id, a.id, a.name, a.avatar
        ORDER BY
          CASE WHEN a.avatar IS NOT NULL AND TRIM(a.avatar) <> '' THEN 1 ELSE 0 END DESC,
          total DESC,
          a.name ASC
        ",
    );
    if let Some(limit) = limit {
        sql.push_str(&format!(" LIMIT {}", limit));
    }
    let mut stmt = conn.prepare(&sql).map_err(|error| error.to_string())?;
    let rows = stmt
        .query_map([], |row| Ok(HomeArtistItem { id: row.get(0)?, name: row.get(1)?, avatar: row.get(2)?, program_count: row.get(3)? }))
        .map_err(|error| error.to_string())?;
    rows.collect::<Result<Vec<_>, _>>().map_err(|error| error.to_string())
}

fn list_musicians_from_conn(conn: &Connection, limit: Option<usize>) -> AdapterResult<Vec<HomeMusicianItem>> {
    let mut sql = String::from(
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
    );
    if let Some(limit) = limit {
        sql.push_str(&format!(" LIMIT {}", limit));
    }
    let mut stmt = conn.prepare(&sql).map_err(|error| error.to_string())?;
    let rows = stmt
        .query_map([], |row| {
            let instrument: Option<String> = row.get(2)?;
            Ok(HomeMusicianItem {
                id: row.get(0)?,
                name: row.get(1)?,
                instrument: instrument.filter(|value| !value.trim().is_empty()).unwrap_or_else(|| "نوازنده".to_string()),
                avatar: row.get(3)?,
                program_count: row.get(4)?,
            })
        })
        .map_err(|error| error.to_string())?;
    rows.collect::<Result<Vec<_>, _>>().map_err(|error| error.to_string())
}

fn programs_for_artist(conn: &Connection, artist_id: i64) -> AdapterResult<Vec<TrackItem>> {
    let mut stmt = conn.prepare(
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
            (SELECT GROUP_CONCAT(m.name, ' و ') FROM program_modes pm JOIN mode m ON m.id = pm.mode_id WHERE pm.program_id = p.id) AS mode_names,
            (SELECT MAX(end_time) FROM program_timeline WHERE program_id = p.id) AS duration,
            p.audio_url,
            (
                SELECT GROUP_CONCAT(avatar, '|')
                FROM (
                    SELECT DISTINCT a2.avatar AS avatar
                    FROM program_singers ps
                    JOIN singer s ON s.id = ps.singer_id
                    JOIN artist a2 ON a2.id = s.artist_id
                    WHERE ps.program_id = p.id AND a2.avatar IS NOT NULL AND TRIM(a2.avatar) != ''
                    ORDER BY a2.name ASC
                )
            ) AS singer_avatars
        FROM artist_program_ids ap
        JOIN program p ON p.id = ap.program_id
        ORDER BY p.no ASC, p.id ASC
        "
    ).map_err(|error| error.to_string())?;
    let rows = stmt
        .query_map([artist_id], |row| Ok(TrackItem {
            id: row.get(0)?,
            title: row.get(1)?,
            artist: row.get(2)?,
            mode: row.get(3)?,
            duration: row.get::<_, Option<String>>(4)?.unwrap_or_else(|| "نامشخص".to_string()),
            audio_url: row.get(5)?,
            singer_avatars: split_avatar_list(row.get::<_, Option<String>>(6)?),
        }))
        .map_err(|error| error.to_string())?;
    rows.collect::<Result<Vec<_>, _>>().map_err(|error| error.to_string())
}

fn category_counts_for_artist(conn: &Connection, artist_id: i64) -> AdapterResult<Vec<ArtistCategoryCountItem>> {
    let mut stmt = conn.prepare(
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
        "
    ).map_err(|error| error.to_string())?;
    let rows = stmt
        .query_map([artist_id], |row| Ok(ArtistCategoryCountItem { category_id: row.get(0)?, title: row.get(1)?, count: row.get(2)? }))
        .map_err(|error| error.to_string())?;
    rows.collect::<Result<Vec<_>, _>>().map_err(|error| error.to_string())
}
