use std::{env, io, path::PathBuf};

use radiogolha_tauri_adapter::SearchProgramsPayload;
use serde::Deserialize;
use serde_json::Value;

#[derive(Debug, Deserialize)]
#[serde(rename_all = "camelCase")]
struct LimitPayload {
    limit: Option<usize>,
}

#[derive(Debug, Deserialize)]
#[serde(rename_all = "camelCase")]
struct TopSearchPayload {
    query: String,
    limit: Option<i64>,
}

#[derive(Debug, Deserialize)]
#[serde(rename_all = "camelCase")]
struct ArtistPayload {
    artist_id: i64,
}

#[derive(Debug, Deserialize)]
#[serde(rename_all = "camelCase")]
struct ProgramPayload {
    program_id: i64,
}

fn resolve_db_path() -> Result<String, String> {
    if let Ok(path) = env::var("RADIOGOLHA_DB_PATH") {
        let candidate = PathBuf::from(path);
        if candidate.exists() {
            return Ok(candidate.to_string_lossy().to_string());
        }
    }

    let current_dir = env::current_dir().map_err(|error| error.to_string())?;
    let candidates = [
        current_dir.join("database/golha_database.db"),
        current_dir.join("../database/golha_database.db"),
        current_dir.join("../../database/golha_database.db"),
        current_dir.join("../../../database/golha_database.db"),
        current_dir.join("../../../../database/golha_database.db"),
    ];

    candidates
        .into_iter()
        .find(|path| path.exists())
        .map(|path| path.to_string_lossy().to_string())
        .ok_or_else(|| "golha_database.db not found. Set RADIOGOLHA_DB_PATH or run from the project workspace.".to_string())
}

fn payload_arg() -> Result<Value, String> {
    env::args()
        .nth(2)
        .map(|raw| serde_json::from_str(&raw).map_err(|error| error.to_string()))
        .unwrap_or_else(|| Ok(Value::Object(Default::default())))
}

fn parse_payload<T: for<'de> Deserialize<'de>>() -> Result<T, String> {
    serde_json::from_value(payload_arg()?).map_err(|error| error.to_string())
}

fn write_json<T: serde::Serialize>(value: &T) -> Result<(), String> {
    serde_json::to_writer(io::stdout(), value).map_err(|error| error.to_string())
}

fn run() -> Result<(), String> {
    let command = env::args().nth(1).ok_or_else(|| "Missing bridge command".to_string())?;
    let db_path = resolve_db_path()?;

    match command.as_str() {
        "get-home-data" => write_json(&radiogolha_tauri_adapter::get_home_data(&db_path)?)?,
        "get-top-tracks" => {
            let payload: LimitPayload = parse_payload()?;
            write_json(&radiogolha_tauri_adapter::get_top_tracks(&db_path, payload.limit.unwrap_or(10))?)?
        }
        "get-singers" => write_json(&radiogolha_tauri_adapter::get_singers(&db_path)?)?,
        "get-musicians" => write_json(&radiogolha_tauri_adapter::get_musicians(&db_path)?)?,
        "get-modes" => write_json(&radiogolha_tauri_adapter::get_modes(&db_path)?)?,
        "get-artist-detail" => {
            let payload: ArtistPayload = parse_payload()?;
            write_json(&radiogolha_tauri_adapter::get_artist_detail(&db_path, payload.artist_id)?)?
        }
        "get-program-tracks" => {
            let payload: ProgramPayload = parse_payload()?;
            write_json(&radiogolha_tauri_adapter::get_program_tracks(&db_path, payload.program_id)?)?
        }
        "get-track-detail" => {
            let payload: ProgramPayload = parse_payload()?;
            write_json(&radiogolha_tauri_adapter::get_track_detail(&db_path, payload.program_id)?)?
        }
        "get-search-options" => write_json(&radiogolha_tauri_adapter::get_search_options(&db_path)?)?,
        "top-bar-search" => {
            let payload: TopSearchPayload = parse_payload()?;
            write_json(&radiogolha_tauri_adapter::top_bar_search(&db_path, &payload.query, payload.limit.unwrap_or(10))?)?
        }
        "search-programs" => {
            let payload: SearchProgramsPayload = parse_payload()?;
            write_json(&radiogolha_tauri_adapter::search_programs(&db_path, payload)?)?
        }
        _ => return Err(format!("Unknown bridge command: {command}")),
    }

    Ok(())
}

fn main() {
    if let Err(error) = run() {
        eprintln!("{error}");
        std::process::exit(1);
    }
}
