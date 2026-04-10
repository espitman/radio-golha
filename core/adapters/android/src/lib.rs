use jni::JNIEnv;
use jni::objects::{JClass, JString};
use jni::sys::jstring;
use radiogolha_core::{LookupKind, RadioGolhaCore, ProgramSortField, SortDirection};
use serde::Serialize;
use serde_json::{json, to_string};
use std::collections::HashSet;

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
    title: String,
    artist: String,
    duration: String,
}

fn categories_json(db_path: &str) -> Result<String, String> {
    let core = RadioGolhaCore::open(db_path).map_err(|error| error.to_string())?;
    let categories = core.program_categories().map_err(|error| error.to_string())?;
    to_string(&categories).map_err(|error| error.to_string())
}

fn home_json(db_path: &str) -> Result<String, String> {
    let core = RadioGolhaCore::open(db_path).map_err(|error| error.to_string())?;
    let overview = core.dashboard_overview().map_err(|error| error.to_string())?;
    let mode_lookup = core
        .browse_lookup_items(LookupKind::Modes, "", 1)
        .map_err(|error| error.to_string())?;
    let mut top_singers = core.top_singers(24).map_err(|error| error.to_string())?;

    let programs = overview
        .category_breakdown
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

    let mut seen_musicians = HashSet::new();
    let mut musicians = Vec::new();
    let mut top_tracks = Vec::new();

    // 1. Fetch 10 Truly Random Vocal Tracks
    let random_programs = core
        .random_vocal_tracks(10)
        .map_err(|error| error.to_string())?;

    for rp in random_programs {
        if let Some(detail) = core.get_program_detail(rp.id).map_err(|e| e.to_string())? {
            top_tracks.push(AndroidTrackItem {
                title: detail.title.clone(),
                artist: detail.singers.join(" و "),
                duration: rp.duration.clone().unwrap_or_else(|| "نامشخص".to_string()),
            });
        }
    }

    // 2. Fetch Featured Musicians from recent programs
    // 2. Fetch Featured Musicians from recent programs
    let programs_pool = core
        .list_programs_filtered(
            "", 1, None, None, ProgramSortField::Id, SortDirection::Desc, 100
        )
        .map_err(|error| error.to_string())?;

    for program in programs_pool {
        let Some(detail) = core.get_program_detail(program.id).map_err(|e| e.to_string())? else { continue };

        for performer in detail.performers {
            let key = performer.name.trim().to_string();
            if key.is_empty() || !seen_musicians.insert(key.clone()) {
                continue;
            }

            musicians.push(AndroidMusicianItem {
                name: key,
                instrument: performer
                    .instrument
                    .filter(|value| !value.trim().is_empty())
                    .unwrap_or_else(|| "نوازنده".to_string()),
                avatar: performer.avatar,
            });

            if musicians.len() >= 8 {
                break;
            }
        }

        if musicians.len() >= 8 {
            break;
        }
    }

    let payload = AndroidHomePayload {
        programs,
        singers,
        dastgahs,
        musicians,
        top_tracks,
    };

    to_string(&payload).map_err(|error| error.to_string())
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
