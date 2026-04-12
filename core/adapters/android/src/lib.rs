use jni::JNIEnv;
use jni::objects::{JClass, JString};
use jni::sys::jstring;
use radiogolha_core::{LookupKind, RadioGolhaCore};
use serde::Serialize;
use serde_json::{json, to_string};

#[derive(Serialize)]
struct AndroidHomePayload {
    programs: Vec<AndroidProgramItem>,
    singers: Vec<AndroidSingerItem>,
    dastgahs: Vec<AndroidNamedItem>,
    musicians: Vec<AndroidMusicianItem>,
    top_tracks: Vec<AndroidTrackItem>,
}

#[derive(Serialize)]
struct AndroidProgramItem {
    title: String,
    episode_count: i64,
}

#[derive(Serialize)]
struct AndroidNamedItem {
    name: String,
}

#[derive(Serialize)]
struct AndroidSingerItem {
    name: String,
    avatar: Option<String>,
}

#[derive(Serialize)]
struct AndroidSingerListItem {
    id: i64,
    name: String,
    avatar: Option<String>,
    program_count: i64,
}

#[derive(Serialize)]
struct AndroidMusicianItem {
    name: String,
    instrument: String,
    avatar: Option<String>,
}

#[derive(Serialize)]
struct AndroidMusicianListItem {
    id: i64,
    name: String,
    instrument: String,
    avatar: Option<String>,
    program_count: i64,
}

#[derive(Serialize)]
struct AndroidTrackItem {
    id: i64,
    title: String,
    artist: String,
    duration: String,
    audio_url: String,
}

#[derive(Serialize)]
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
    let category_breakdown = core.category_breakdown().map_err(|error| error.to_string())?;
    let mode_lookup = core
        .browse_lookup_items(LookupKind::Modes, "", 1)
        .map_err(|error| error.to_string())?;
    let mut top_singers = core.top_singers(24).map_err(|error| error.to_string())?;

    let programs = category_breakdown
        .into_iter()
        .map(|item| AndroidProgramItem {
            title: item.name,
            episode_count: item.total,
        })
        .collect();

    top_singers.sort_by(|left, right| {
        let left_has_avatar = left
            .avatar
            .as_deref()
            .map(str::trim)
            .is_some_and(|value| !value.is_empty());
        let right_has_avatar = right
            .avatar
            .as_deref()
            .map(str::trim)
            .is_some_and(|value| !value.is_empty());

        right_has_avatar
            .cmp(&left_has_avatar)
            .then_with(|| right.total.cmp(&left.total))
            .then_with(|| left.name.cmp(&right.name))
    });

    let singers = top_singers
        .into_iter()
        .take(8)
        .map(|item| AndroidSingerItem {
            name: item.name,
            avatar: item.avatar,
        })
        .collect();

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

    let musicians = core
        .top_performers(8)
        .map_err(|error| error.to_string())?
        .into_iter()
        .map(|item| AndroidMusicianItem {
            name: item.name,
            instrument: item
                .instrument
                .filter(|value| !value.trim().is_empty())
                .unwrap_or_else(|| "نوازنده".to_string()),
            avatar: item.avatar,
        })
        .collect();

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
