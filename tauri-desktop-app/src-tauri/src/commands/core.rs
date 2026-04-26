use std::{env, path::PathBuf};

use radiogolha_tauri_adapter::{
    ArtistDetailPayload, HomeArtistItem, HomeModeItem, HomeMusicianItem, HomePayload,
    ProgramDetail, ProgramSearchOptions, ProgramSearchResponse, ProgramTracksPayload,
    SearchProgramsPayload, TrackItem,
};

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
    ];

    candidates
        .into_iter()
        .find(|path| path.exists())
        .map(|path| path.to_string_lossy().to_string())
        .ok_or_else(|| "golha_database.db not found. Set RADIOGOLHA_DB_PATH or run from the project workspace.".to_string())
}

#[tauri::command]
pub fn core_get_home_data() -> Result<HomePayload, String> {
    let db_path = resolve_db_path()?;
    radiogolha_tauri_adapter::get_home_data(&db_path)
}

#[tauri::command]
pub fn core_get_top_tracks(limit: Option<usize>) -> Result<Vec<TrackItem>, String> {
    let db_path = resolve_db_path()?;
    radiogolha_tauri_adapter::get_top_tracks(&db_path, limit.unwrap_or(10))
}

#[tauri::command]
pub fn core_get_singers() -> Result<Vec<HomeArtistItem>, String> {
    let db_path = resolve_db_path()?;
    radiogolha_tauri_adapter::get_singers(&db_path)
}

#[tauri::command]
pub fn core_get_musicians() -> Result<Vec<HomeMusicianItem>, String> {
    let db_path = resolve_db_path()?;
    radiogolha_tauri_adapter::get_musicians(&db_path)
}

#[tauri::command]
pub fn core_get_modes() -> Result<Vec<HomeModeItem>, String> {
    let db_path = resolve_db_path()?;
    radiogolha_tauri_adapter::get_modes(&db_path)
}

#[tauri::command]
pub fn core_get_artist_detail(artist_id: i64) -> Result<ArtistDetailPayload, String> {
    let db_path = resolve_db_path()?;
    radiogolha_tauri_adapter::get_artist_detail(&db_path, artist_id)
}

#[tauri::command]
pub fn core_get_program_tracks(program_id: i64) -> Result<ProgramTracksPayload, String> {
    let db_path = resolve_db_path()?;
    radiogolha_tauri_adapter::get_program_tracks(&db_path, program_id)
}

#[tauri::command]
pub fn core_get_track_detail(program_id: i64) -> Result<ProgramDetail, String> {
    let db_path = resolve_db_path()?;
    radiogolha_tauri_adapter::get_track_detail(&db_path, program_id)
}

#[tauri::command]
pub fn core_get_search_options() -> Result<ProgramSearchOptions, String> {
    let db_path = resolve_db_path()?;
    radiogolha_tauri_adapter::get_search_options(&db_path)
}

#[tauri::command]
pub fn core_search_programs(payload: SearchProgramsPayload) -> Result<ProgramSearchResponse, String> {
    let db_path = resolve_db_path()?;
    radiogolha_tauri_adapter::search_programs(&db_path, payload)
}
