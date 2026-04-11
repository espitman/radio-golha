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
    let singers = core
        .top_singers(256)
        .map_err(|error| error.to_string())?
        .into_iter()
        .map(|item| AndroidSingerListItem {
            name: item.name,
            avatar: item.avatar,
            program_count: item.total,
        })
        .collect::<Vec<_>>();

    to_string(&singers).map_err(|error| error.to_string())
}

fn musicians_json(db_path: &str) -> Result<String, String> {
    let core = RadioGolhaCore::open(db_path).map_err(|error| error.to_string())?;
    let musicians = core
        .top_performers(256)
        .map_err(|error| error.to_string())?
        .into_iter()
        .map(|item| AndroidMusicianListItem {
            name: item.name,
            instrument: item
                .instrument
                .filter(|value| !value.trim().is_empty())
                .unwrap_or_else(|| "نوازنده".to_string()),
            avatar: item.avatar,
            program_count: item.total,
        })
        .collect::<Vec<_>>();

    to_string(&musicians).map_err(|error| error.to_string())
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
