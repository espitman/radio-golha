use jni::JNIEnv;
use jni::objects::{JClass, JString};
use jni::sys::jstring;
use radiogolha_core::{LookupKind, ProgramSearchFilters, ProgramSortField, RadioGolhaCore, SearchMatchMode, SortDirection};
use serde::{Deserialize, Serialize};
use serde_json::{json, to_string};

#[derive(Serialize)]
#[serde(rename_all = "camelCase")]
struct AndroidHomePayload {
    programs: Vec<AndroidProgramItem>,
    singers: Vec<AndroidSingerItem>,
    dastgahs: Vec<AndroidNamedItem>,
    musicians: Vec<AndroidMusicianItem>,
    top_tracks: Vec<AndroidTrackItem>,
}

#[derive(Serialize)]
#[serde(rename_all = "camelCase")]
struct AndroidProgramItem {
    title: String,
    episode_count: i64,
}

#[derive(Serialize)]
#[serde(rename_all = "camelCase")]
struct AndroidNamedItem {
    name: String,
}

#[derive(Serialize)]
#[serde(rename_all = "camelCase")]
struct AndroidSingerItem {
    id: i64,
    name: String,
    avatar: Option<String>,
}

#[derive(Serialize)]
#[serde(rename_all = "camelCase")]
struct AndroidSingerListItem {
    id: i64,
    name: String,
    avatar: Option<String>,
    program_count: i64,
}

#[derive(Serialize)]
#[serde(rename_all = "camelCase")]
struct AndroidMusicianItem {
    id: i64,
    name: String,
    instrument: String,
    avatar: Option<String>,
}

#[derive(Serialize)]
#[serde(rename_all = "camelCase")]
struct AndroidMusicianListItem {
    id: i64,
    name: String,
    instrument: String,
    avatar: Option<String>,
    program_count: i64,
}

#[derive(Serialize)]
#[serde(rename_all = "camelCase")]
struct AndroidTrackItem {
    id: i64,
    title: String,
    artist: String,
    duration: String,
    audio_url: String,
}

#[derive(Serialize)]
#[serde(rename_all = "camelCase")]
struct AndroidCategoryProgramItem {
    id: i64,
    title: String,
    no: i64,
    artist: String,
    mode: Option<String>,
    duration: Option<String>,
    audio_url: Option<String>,
}

#[derive(Serialize)]
#[serde(rename_all = "camelCase")]
struct AndroidArtistDetailPayload {
    id: i64,
    name: String,
    avatar: Option<String>,
    instrument: Option<String>,
    track_count: i64,
    tracks: Vec<AndroidCategoryProgramItem>,
}

fn categories_json(db_path: &str) -> Result<String, String> {
    let core = RadioGolhaCore::open(db_path).map_err(|error| error.to_string())?;
    let categories = core.program_categories().map_err(|error| error.to_string())?;
    to_string(&categories).map_err(|error| error.to_string())
}

fn home_json(db_path: &str) -> Result<String, String> {
    let core = RadioGolhaCore::open(db_path).map_err(|error| error.to_string())?;
    let conn = core.connection();
    let category_breakdown = core.category_breakdown().map_err(|error| error.to_string())?;
    let mode_lookup = core
        .browse_lookup_items(LookupKind::Modes, "", 1)
        .map_err(|error| error.to_string())?;

    let programs = category_breakdown
        .into_iter()
        .map(|item| AndroidProgramItem {
            title: item.name,
            episode_count: item.total,
        })
        .collect();

    let mut singers_stmt = conn.prepare(
        "
        SELECT a.id, a.name, a.avatar, COUNT(DISTINCT ps.program_id) AS total
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
        LIMIT 8
        ",
    ).map_err(|error| error.to_string())?;

    let singer_rows = singers_stmt.query_map([], |row| {
        Ok(AndroidSingerItem {
            id: row.get(0)?,
            name: row.get(1)?,
            avatar: row.get(2)?,
        })
    }).map_err(|error| error.to_string())?;

    let singers = singer_rows
        .collect::<Result<Vec<_>, _>>()
        .map_err(|error| error.to_string())?;

    let dastgahs = mode_lookup
        .rows
        .into_iter()
        .map(|item| AndroidNamedItem { name: item.name })
        .collect();

    let top_tracks = core
        .random_vocal_track_summaries(10)
        .map_err(|error| error.to_string())?
        .into_iter()
        .map(|item| AndroidTrackItem {
            id: item.id,
            title: item.title,
            artist: item.artist,
            duration: item.duration.unwrap_or_else(|| "نامشخص".to_string()),
            audio_url: item.audio_url,
        })
        .collect();

    let mut musicians_stmt = conn.prepare(
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
        LIMIT 8
        ",
    ).map_err(|error| error.to_string())?;

    let musician_rows = musicians_stmt.query_map([], |row| {
        let instrument: Option<String> = row.get(2)?;
        Ok(AndroidMusicianItem {
            id: row.get(0)?,
            name: row.get(1)?,
            instrument: instrument
                .filter(|value| !value.trim().is_empty())
                .unwrap_or_else(|| "نوازنده".to_string()),
            avatar: row.get(3)?,
        })
    }).map_err(|error| error.to_string())?;

    let musicians = musician_rows
        .collect::<Result<Vec<_>, _>>()
        .map_err(|error| error.to_string())?;

    let payload = AndroidHomePayload {
        programs,
        singers,
        dastgahs,
        musicians,
        top_tracks,
    };

    to_string(&payload).map_err(|error| error.to_string())
}

fn top_tracks_json(db_path: &str) -> Result<String, String> {
    let core = RadioGolhaCore::open(db_path).map_err(|error| error.to_string())?;
    let top_tracks = core
        .random_vocal_track_summaries(5)
        .map_err(|error| error.to_string())?
        .into_iter()
        .map(|item| AndroidTrackItem {
            id: item.id,
            title: item.title,
            artist: item.artist,
            duration: item.duration.unwrap_or_else(|| "نامشخص".to_string()),
            audio_url: item.audio_url,
        })
        .collect::<Vec<_>>();

    to_string(&top_tracks).map_err(|error| error.to_string())
}

fn singers_json(db_path: &str) -> Result<String, String> {
    let core = RadioGolhaCore::open(db_path).map_err(|error| error.to_string())?;
    let conn = core.connection();
    let mut stmt = conn.prepare(
        "
        SELECT a.id, a.name, a.avatar, COUNT(DISTINCT ps.program_id) AS total
        FROM program_singers ps
        JOIN singer s ON s.id = ps.singer_id
        JOIN artist a ON a.id = s.artist_id
        GROUP BY s.id, a.id, a.name, a.avatar
        ORDER BY total DESC, a.name ASC
        "
    ).map_err(|error| error.to_string())?;

    let rows = stmt.query_map([], |row| {
        Ok(AndroidSingerListItem {
            id: row.get(0)?,
            name: row.get(1)?,
            avatar: row.get(2)?,
            program_count: row.get(3)?,
        })
    }).map_err(|error| error.to_string())?;

    let singers = rows.collect::<Result<Vec<_>, _>>().map_err(|error| error.to_string())?;

    to_string(&singers).map_err(|error| error.to_string())
}

fn musicians_json(db_path: &str) -> Result<String, String> {
    let core = RadioGolhaCore::open(db_path).map_err(|error| error.to_string())?;
    let conn = core.connection();
    let mut stmt = conn.prepare(
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
        "
    ).map_err(|error| error.to_string())?;

    let rows = stmt.query_map([], |row| {
        let instrument: Option<String> = row.get(2)?;
        Ok(AndroidMusicianListItem {
            id: row.get(0)?,
            name: row.get(1)?,
            instrument: instrument
                .filter(|value| !value.trim().is_empty())
                .unwrap_or_else(|| "نوازنده".to_string()),
            avatar: row.get(3)?,
            program_count: row.get(4)?,
        })
    }).map_err(|error| error.to_string())?;

    let musicians = rows.collect::<Result<Vec<_>, _>>().map_err(|error| error.to_string())?;

    to_string(&musicians).map_err(|error| error.to_string())
}

fn orchestras_json(db_path: &str) -> Result<String, String> {
    let core = RadioGolhaCore::open(db_path).map_err(|error| error.to_string())?;
    let items = core
        .list_lookup_items(LookupKind::Orchestras, "", 1, 1000)
        .map_err(|error| error.to_string())?;
    let result: Vec<_> = items
        .into_iter()
        .map(|item| {
            json!({
                "id": item.id,
                "name": item.name,
                "programCount": item.usage_count
            })
        })
        .collect();
    to_string(&result).map_err(|error| error.to_string())
}

fn programs_by_orchestra_json(db_path: &str, orchestra_id: i64) -> Result<String, String> {
    let core = RadioGolhaCore::open(db_path).map_err(|error| error.to_string())?;
    let conn = core.connection();
    let mut stmt = conn.prepare(
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
        JOIN program_orchestras po ON po.program_id = p.id
        WHERE po.orchestra_id = ?1
        ORDER BY p.no ASC, p.id ASC
        "
    ).map_err(|error| error.to_string())?;

    let rows = stmt.query_map([orchestra_id], |row| {
        Ok(AndroidCategoryProgramItem {
            id: row.get(0)?,
            title: row.get(1)?,
            no: row.get(2)?,
            artist: row.get(3)?,
            mode: row.get(4)?,
            duration: row.get(5)?,
            audio_url: row.get(6)?,
        })
    }).map_err(|error| error.to_string())?;

    let items: Vec<_> = rows.collect::<Result<Vec<_>, _>>().map_err(|error| error.to_string())?;
    to_string(&items).map_err(|error| error.to_string())
}

fn artist_detail_json(db_path: &str, artist_id: i64) -> Result<String, String> {
    let core = RadioGolhaCore::open(db_path).map_err(|error| error.to_string())?;
    let conn = core.connection();

    let payload = conn.query_row(
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
        [artist_id],
        |row| {
            Ok((
                row.get::<_, i64>(0)?,
                row.get::<_, String>(1)?,
                row.get::<_, Option<String>>(2)?,
                row.get::<_, Option<String>>(3)?,
                row.get::<_, i64>(4)?,
            ))
        },
    ).map_err(|error| error.to_string())?;

    let mut stmt = conn.prepare(
        "
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
        FROM program p
        WHERE EXISTS (
            SELECT 1
            FROM program_singers ps
            JOIN singer s ON s.id = ps.singer_id
            WHERE ps.program_id = p.id AND s.artist_id = ?1
        )
        OR EXISTS (
            SELECT 1
            FROM program_performers pp
            JOIN performer pf ON pf.id = pp.performer_id
            WHERE pp.program_id = p.id AND pf.artist_id = ?1
        )
        ORDER BY p.no ASC, p.id ASC
        "
    ).map_err(|error| error.to_string())?;

    let rows = stmt.query_map([artist_id], |row| {
        Ok(AndroidCategoryProgramItem {
            id: row.get(0)?,
            title: row.get(1)?,
            no: row.get(2)?,
            artist: row.get(3)?,
            mode: row.get(4)?,
            duration: row.get(5)?,
            audio_url: row.get(6)?,
        })
    }).map_err(|error| error.to_string())?;

    let tracks = rows.collect::<Result<Vec<_>, _>>().map_err(|error| error.to_string())?;

    let payload = AndroidArtistDetailPayload {
        id: payload.0,
        name: payload.1,
        avatar: payload.2,
        instrument: payload.3,
        track_count: payload.4,
        tracks,
    };

    to_string(&payload).map_err(|error| error.to_string())
}

#[derive(Deserialize, Default)]
#[serde(rename_all = "camelCase")]
struct AndroidSearchRequest {
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

fn duet_programs_json(db_path: &str, singer1: &str, singer2: &str) -> Result<String, String> {
    let core = RadioGolhaCore::open(db_path).map_err(|e| e.to_string())?;
    let conn = core.connection();
    let mut stmt = conn.prepare(
        "
        SELECT p.id, p.title, p.no,
               COALESCE(
                   (SELECT GROUP_CONCAT(a.name, ' و ')
                    FROM program_singers ps2
                    JOIN singer s2 ON s2.id = ps2.singer_id
                    JOIN artist a ON a.id = s2.artist_id
                    WHERE ps2.program_id = p.id),
                   'ناشناس'
               ) AS artist_names,
               (SELECT GROUP_CONCAT(m.name, ' و ')
                FROM program_modes pm JOIN mode m ON m.id = pm.mode_id
                WHERE pm.program_id = p.id) AS mode_names,
               (SELECT MAX(end_time) FROM program_timeline WHERE program_id = p.id) AS duration,
               p.audio_url
        FROM program p
        WHERE (SELECT COUNT(*) FROM program_singers ps WHERE ps.program_id = p.id) = 2
          AND EXISTS (
              SELECT 1 FROM program_singers ps
              JOIN singer s ON s.id = ps.singer_id
              JOIN artist a ON a.id = s.artist_id
              WHERE ps.program_id = p.id AND a.name = ?1
          )
          AND EXISTS (
              SELECT 1 FROM program_singers ps
              JOIN singer s ON s.id = ps.singer_id
              JOIN artist a ON a.id = s.artist_id
              WHERE ps.program_id = p.id AND a.name = ?2
          )
        ORDER BY p.no ASC, p.id ASC
        "
    ).map_err(|e| e.to_string())?;

    let rows = stmt.query_map([singer1, singer2], |row| {
        Ok(AndroidCategoryProgramItem {
            id: row.get(0)?,
            title: row.get(1)?,
            no: row.get(2)?,
            artist: row.get(3)?,
            mode: row.get(4)?,
            duration: row.get(5)?,
            audio_url: row.get(6)?,
        })
    }).map_err(|e| e.to_string())?;

    let items: Vec<_> = rows.collect::<Result<Vec<_>, _>>().map_err(|e| e.to_string())?;
    to_string(&items).map_err(|e| e.to_string())
}

fn config_json(db_path: &str, key: &str) -> Result<String, String> {
    let core = RadioGolhaCore::open(db_path).map_err(|e| e.to_string())?;
    let value = core.get_config(key).map_err(|e| e.to_string())?;
    to_string(&value).map_err(|e| e.to_string())
}

fn duet_pairs_config_json(db_path: &str) -> Result<String, String> {
    let core = RadioGolhaCore::open(db_path).map_err(|e| e.to_string())?;
    core.get_duet_pairs_raw().map_err(|e| e.to_string())
}

fn ordered_modes_json(db_path: &str) -> Result<String, String> {
    let core = RadioGolhaCore::open(db_path).map_err(|e| e.to_string())?;
    let modes = core.get_ordered_modes().map_err(|e| e.to_string())?;
    to_string(&modes).map_err(|e| e.to_string())
}

fn programs_by_mode_json(db_path: &str, mode_id: i64) -> Result<String, String> {
    let core = RadioGolhaCore::open(db_path).map_err(|e| e.to_string())?;
    let conn = core.connection();
    let mut stmt = conn.prepare(
        "
        SELECT p.id, p.title, p.no,
               COALESCE(
                   (SELECT GROUP_CONCAT(a.name, ' و ')
                    FROM program_singers ps
                    JOIN singer s ON s.id = ps.singer_id
                    JOIN artist a ON a.id = s.artist_id
                    WHERE ps.program_id = p.id),
                   'ناشناس'
               ) AS artist_names,
               (SELECT GROUP_CONCAT(m2.name, ' و ')
                FROM program_modes pm2 JOIN mode m2 ON m2.id = pm2.mode_id
                WHERE pm2.program_id = p.id) AS mode_names,
               (SELECT MAX(end_time) FROM program_timeline WHERE program_id = p.id) AS duration,
               p.audio_url
        FROM program p
        JOIN program_modes pm ON pm.program_id = p.id
        WHERE pm.mode_id = ?1
        ORDER BY p.no ASC, p.id ASC
        "
    ).map_err(|e| e.to_string())?;

    let rows = stmt.query_map([mode_id], |row| {
        Ok(AndroidCategoryProgramItem {
            id: row.get(0)?,
            title: row.get(1)?,
            no: row.get(2)?,
            artist: row.get(3)?,
            mode: row.get(4)?,
            duration: row.get(5)?,
            audio_url: row.get(6)?,
        })
    }).map_err(|e| e.to_string())?;

    let items: Vec<_> = rows.collect::<Result<Vec<_>, _>>().map_err(|e| e.to_string())?;
    to_string(&items).map_err(|e| e.to_string())
}

fn search_options_json(db_path: &str) -> Result<String, String> {
    let core = RadioGolhaCore::open(db_path).map_err(|e| e.to_string())?;
    let options = core.program_search_options().map_err(|e| e.to_string())?;
    to_string(&options).map_err(|e| e.to_string())
}

fn search_programs_json(db_path: &str, filters_json: &str) -> Result<String, String> {
    let req: AndroidSearchRequest = serde_json::from_str(filters_json).map_err(|e| e.to_string())?;
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
    let core = RadioGolhaCore::open(db_path).map_err(|e| e.to_string())?;
    let result = core.search_programs(&filters, page).map_err(|e| e.to_string())?;
    to_string(&result).map_err(|e| e.to_string())
}

fn jni_json_response(
    env: &mut JNIEnv,
    db_path: JString,
    builder: impl FnOnce(&str) -> Result<String, String>,
) -> jstring {
    let payload = match env.get_string(&db_path) {
        Ok(path) => match builder(&path.to_string_lossy()) {
            Ok(json) => json,
            Err(error) => json!({ "error": error }).to_string(),
        },
        Err(error) => json!({ "error": error.to_string() }).to_string(),
    };

    match env.new_string(payload) {
        Ok(value) => value.into_raw(),
        Err(error) => env
            .new_string(json!({ "error": error.to_string() }).to_string())
            .expect("fallback JNI string should be created")
            .into_raw(),
    }
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_com_radiogolha_mobile_RustCoreBridge_getCategoriesJson(
    mut env: JNIEnv,
    _class: JClass,
    db_path: JString,
) -> jstring {
    jni_json_response(&mut env, db_path, categories_json)
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_com_radiogolha_mobile_RustCoreBridge_getHomeFeedJson(
    mut env: JNIEnv,
    _class: JClass,
    db_path: JString,
) -> jstring {
    jni_json_response(&mut env, db_path, home_json)
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_com_radiogolha_mobile_RustCoreBridge_getTopTracksJson(
    mut env: JNIEnv,
    _class: JClass,
    db_path: JString,
) -> jstring {
    jni_json_response(&mut env, db_path, top_tracks_json)
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_com_radiogolha_mobile_RustCoreBridge_getSingersJson(
    mut env: JNIEnv,
    _class: JClass,
    db_path: JString,
) -> jstring {
    jni_json_response(&mut env, db_path, singers_json)
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_com_radiogolha_mobile_RustCoreBridge_getMusiciansJson(
    mut env: JNIEnv,
    _class: JClass,
    db_path: JString,
) -> jstring {
    jni_json_response(&mut env, db_path, musicians_json)
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_com_radiogolha_mobile_RustCoreBridge_getArtistDetailJson(
    mut env: JNIEnv,
    _class: JClass,
    db_path: JString,
    artist_id: i64,
) -> jstring {
    jni_json_response(&mut env, db_path, |path| artist_detail_json(path, artist_id))
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_com_radiogolha_mobile_RustCoreBridge_getProgramsByCategoryJson(
    mut env: JNIEnv,
    _class: JClass,
    db_path: JString,
    category_id: i64,
) -> jstring {
    jni_json_response(&mut env, db_path, |path| {
        let core = RadioGolhaCore::open(path).map_err(|error| error.to_string())?;
        let conn = core.connection();
        let mut stmt = conn.prepare(
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
            "
        ).map_err(|error| error.to_string())?;

        let rows = stmt.query_map([category_id], |row| {
            Ok(AndroidCategoryProgramItem {
                id: row.get(0)?,
                title: row.get(1)?,
                no: row.get(2)?,
                artist: row.get(3)?,
                mode: row.get(4)?,
                duration: row.get(5)?,
                audio_url: row.get(6)?,
            })
        }).map_err(|error| error.to_string())?;

        let items: Vec<_> = rows.collect::<Result<Vec<_>, _>>().map_err(|error| error.to_string())?;
        to_string(&items).map_err(|error| error.to_string())
    })
}
#[unsafe(no_mangle)]
pub extern "system" fn Java_com_radiogolha_mobile_RustCoreBridge_getProgramDetailJson(
    mut env: JNIEnv,
    _class: JClass,
    db_path: JString,
    program_id: i64,
) -> jstring {
    jni_json_response(&mut env, db_path, |path| {
        let core = RadioGolhaCore::open(path).map_err(|error| error.to_string())?;
        let detail = core.get_program_detail(program_id).map_err(|error| error.to_string())?;
        to_string(&detail).map_err(|error| error.to_string())
    })
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_com_radiogolha_mobile_RustCoreBridge_getOrchestrasJson(
    mut env: JNIEnv,
    _class: JClass,
    db_path: JString,
) -> jstring {
    jni_json_response(&mut env, db_path, orchestras_json)
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_com_radiogolha_mobile_RustCoreBridge_getProgramsByOrchestraJson(
    mut env: JNIEnv,
    _class: JClass,
    db_path: JString,
    orchestra_id: i64,
) -> jstring {
    jni_json_response(&mut env, db_path, |path| programs_by_orchestra_json(path, orchestra_id))
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_com_radiogolha_mobile_RustCoreBridge_getDuetPairsConfigJson(
    mut env: JNIEnv,
    _class: JClass,
    db_path: JString,
) -> jstring {
    jni_json_response(&mut env, db_path, duet_pairs_config_json)
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_com_radiogolha_mobile_RustCoreBridge_getProgramsByModeJson(
    mut env: JNIEnv,
    _class: JClass,
    db_path: JString,
    mode_id: i64,
) -> jstring {
    jni_json_response(&mut env, db_path, |path| programs_by_mode_json(path, mode_id))
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_com_radiogolha_mobile_RustCoreBridge_getOrderedModesJson(
    mut env: JNIEnv,
    _class: JClass,
    db_path: JString,
) -> jstring {
    jni_json_response(&mut env, db_path, ordered_modes_json)
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_com_radiogolha_mobile_RustCoreBridge_getConfigJson(
    mut env: JNIEnv,
    _class: JClass,
    db_path: JString,
    key: JString,
) -> jstring {
    let k = match env.get_string(&key) {
        Ok(s) => s.to_string_lossy().to_string(),
        Err(e) => return jni_json_response(&mut env, db_path, |_| Err(e.to_string())),
    };
    jni_json_response(&mut env, db_path, |path| config_json(path, &k))
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_com_radiogolha_mobile_RustCoreBridge_getSearchOptionsJson(
    mut env: JNIEnv,
    _class: JClass,
    db_path: JString,
) -> jstring {
    jni_json_response(&mut env, db_path, search_options_json)
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_com_radiogolha_mobile_RustCoreBridge_searchProgramsJson(
    mut env: JNIEnv,
    _class: JClass,
    db_path: JString,
    filters_json: JString,
) -> jstring {
    let filters = match env.get_string(&filters_json) {
        Ok(s) => s.to_string_lossy().to_string(),
        Err(e) => return jni_json_response(&mut env, db_path, |_| Err(e.to_string())),
    };
    jni_json_response(&mut env, db_path, |path| search_programs_json(path, &filters))
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_com_radiogolha_mobile_RustCoreBridge_getDuetProgramsJson(
    mut env: JNIEnv,
    _class: JClass,
    db_path: JString,
    singer1: JString,
    singer2: JString,
) -> jstring {
    let s1 = match env.get_string(&singer1) {
        Ok(s) => s.to_string_lossy().to_string(),
        Err(e) => return jni_json_response(&mut env, db_path, |_| Err(e.to_string())),
    };
    let s2 = match env.get_string(&singer2) {
        Ok(s) => s.to_string_lossy().to_string(),
        Err(e) => return jni_json_response(&mut env, db_path, |_| Err(e.to_string())),
    };
    jni_json_response(&mut env, db_path, |path| duet_programs_json(path, &s1, &s2))
}
