use jni::JNIEnv;
use jni::objects::{JClass, JString};
use jni::sys::jstring;
use radiogolha_core::{LookupKind, RadioGolhaCore};
use serde::Serialize;
use serde_json::{json, to_string};
use std::collections::HashSet;

#[derive(Serialize)]
struct AndroidHomePayload {
    programs: Vec<AndroidProgramItem>,
    singers: Vec<AndroidNamedItem>,
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
struct AndroidMusicianItem {
    name: String,
    instrument: String,
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

    let programs = overview
        .category_breakdown
        .into_iter()
        .map(|item| AndroidProgramItem {
            title: item.name,
            episode_count: item.total,
        })
        .collect();

    let singers = overview
        .top_singers
        .into_iter()
        .take(8)
        .map(|item| AndroidNamedItem { name: item.name })
        .collect();

    let dastgahs = mode_lookup
        .rows
        .into_iter()
        .map(|item| AndroidNamedItem { name: item.name })
        .collect();

    let mut seen_musicians = HashSet::new();
    let mut musicians = Vec::new();
    let mut top_tracks = Vec::new();

    for program in overview.recent_programs {
        let Some(detail) = core
            .get_program_detail(program.id)
            .map_err(|error| error.to_string())?
        else {
            continue;
        };

        if !detail.audio_url.as_deref().unwrap_or("").trim().is_empty() && top_tracks.len() < 5 {
            let artist = detail
                .singers
                .first()
                .cloned()
                .or_else(|| detail.performers.first().map(|item| item.name.clone()))
                .unwrap_or_else(|| detail.category_name.clone());
            top_tracks.push(AndroidTrackItem {
                title: detail.title.clone(),
                artist,
                duration: "نامشخص".to_string(),
            });
        }

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
            });

            if musicians.len() >= 8 {
                break;
            }
        }

        if musicians.len() >= 8 && top_tracks.len() >= 5 {
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
