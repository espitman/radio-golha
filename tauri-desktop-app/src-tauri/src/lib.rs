mod commands;

#[cfg_attr(mobile, tauri::mobile_entry_point)]
pub fn run() {
    tauri::Builder::default()
        .plugin(tauri_plugin_opener::init())
        .invoke_handler(tauri::generate_handler![
            commands::bootstrap::get_bootstrap_payload,
            commands::core::core_get_home_data,
            commands::core::core_get_top_tracks,
            commands::core::core_get_singers,
            commands::core::core_get_musicians,
            commands::core::core_get_modes,
            commands::core::core_get_artist_detail,
            commands::core::core_get_program_tracks,
            commands::core::core_get_track_detail,
            commands::core::core_get_search_options,
            commands::core::core_search_programs
        ])
        .run(tauri::generate_context!())
        .expect("error while running tauri application");
}
