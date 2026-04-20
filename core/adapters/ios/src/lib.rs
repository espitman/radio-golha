use std::ffi::{CStr, CString};
use std::os::raw::c_char;
use radiogolha_core::{ProgramSearchFilters, ProgramSortField, RadioGolhaCore, SearchMatchMode, SortDirection, user_data::{UserDataStore, CreatePlaylistRequest}};
use serde::{Deserialize, Serialize};
use serde_json::{json, to_string};

// --- Helper Functions for String Management ---

fn rust_str_to_c(s: String) -> *mut c_char {
    match CString::new(s) {
        Ok(cstr) => cstr.into_raw(),
        Err(_) => {
            // Ensure FFI boundary never panics on interior NUL bytes.
            match CString::new("[]") {
                Ok(fallback) => fallback.into_raw(),
                Err(_) => std::ptr::null_mut(),
            }
        }
    }
}

#[no_mangle]
pub extern "C" fn radiogolha_free_string(s: *mut c_char) {
    if s.is_null() { return; }
    unsafe { 
        let _ = CString::from_raw(s); 
    }
}

fn get_path(c_str: *const c_char) -> String {
    if c_str.is_null() { return String::new(); }
    unsafe { CStr::from_ptr(c_str).to_string_lossy().into_owned() }
}

// --- DTOs (Coordinated with KMP models in HomeDataModels.kt) ---

#[derive(Serialize)]
#[serde(rename_all = "camelCase")]
struct IosProgramDto {
    title: String,
    episode_count: i64,
}

#[derive(Serialize)]
#[serde(rename_all = "camelCase")]
struct IosCategoryProgramDto {
    id: i64,
    title: Option<String>,
    no: i64,
    artist: String,
    mode: Option<String>,
    duration: Option<String>,
    audio_url: Option<String>,
}

#[derive(Serialize)]
#[serde(rename_all = "camelCase")]
struct IosArtistDetailResponse {
    id: i64,
    name: String,
    avatar: Option<String>,
    instrument: Option<String>,
    track_count: i64,
    programs: Vec<IosCategoryProgramDto>,
}

#[derive(Deserialize, Default)]
#[serde(rename_all = "camelCase")]
pub struct IosSearchRequest {
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

fn parse_match_mode(s: &Option<String>) -> SearchMatchMode {
    match s.as_deref() {
        Some("all") => SearchMatchMode::All,
        _ => SearchMatchMode::Any,
    }
}

// --- Bridge Implementation ---

#[no_mangle]
pub extern "C" fn get_home_feed_json(db_path: *const c_char) -> *mut c_char {
    let path = get_path(db_path);
    match RadioGolhaCore::open(&path) {
        Ok(core) => {
            let conn = core.connection();
            
            let categories = core.program_categories().unwrap_or_default();
            let cat_breakdown = core.category_breakdown().unwrap_or_default();
            let programs: Vec<_> = cat_breakdown.iter().map(|item| {
                IosProgramDto { title: item.name.clone(), episode_count: item.total }
            }).collect();

            let mut singers_stmt = conn.prepare(
                "SELECT a.id, a.name, a.avatar, COUNT(DISTINCT ps.program_id) AS total
                 FROM program_singers ps
                 JOIN singer s ON s.id = ps.singer_id
                 JOIN artist a ON a.id = s.artist_id
                 GROUP BY s.id ORDER BY total DESC LIMIT 12"
            ).unwrap();
            let singers: Vec<_> = singers_stmt.query_map([], |row| {
                Ok(json!({ 
                    "id": row.get::<_, i64>(0)?, 
                    "name": row.get::<_, String>(1)?, 
                    "avatar": row.get::<_, Option<String>>(2)?,
                    "programCount": row.get::<_, i64>(3)?
                }))
            }).unwrap().filter_map(|r| r.ok()).collect();

            let dastgahs = core.top_modes(10).unwrap_or_default();
            
            let mut musicians_stmt = conn.prepare(
                "SELECT a.id, a.name, a.avatar, COUNT(DISTINCT pp.program_id) AS total
                 FROM program_performers pp
                 JOIN performer p ON p.id = pp.performer_id
                 JOIN artist a ON a.id = p.artist_id
                 GROUP BY p.id ORDER BY total DESC LIMIT 12"
            ).unwrap();
            let musicians: Vec<_> = musicians_stmt.query_map([], |row| {
                Ok(json!({ 
                    "id": row.get::<_, i64>(0)?, 
                    "name": row.get::<_, String>(1)?, 
                    "avatar": row.get::<_, Option<String>>(2)?, 
                    "instrument": "نوازنده",
                    "programCount": row.get::<_, i64>(3)?
                }))
            }).unwrap().filter_map(|r| r.ok()).collect();

            let top_tracks = core.random_vocal_track_summaries(10).unwrap_or_default();
            
            let duets_json = core.get_duet_pairs_raw().unwrap_or_else(|_| "[]".to_string());
            let duets: serde_json::Value = serde_json::from_str(&duets_json).unwrap_or(json!([]));

            let payload = json!({
                "programs": programs,
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
                    "id": t.id, "title": t.title, "artist": t.artist, "duration": t.duration.clone().unwrap_or_else(|| "00:00".to_string()), "audioUrl": t.audio_url
                })).collect::<Vec<_>>(),
                "duets": duets
            });
            rust_str_to_c(payload.to_string())
        },
        Err(e) => {
            eprintln!("Error opening database at {}: {:?}", path, e);
            rust_str_to_c(json!({ "error": e.to_string() }).to_string())
        }
    }
}

#[no_mangle]
pub extern "C" fn get_artist_detail_json(db_path: *const c_char, artist_id: i64) -> *mut c_char {
    let path = get_path(db_path);
    match RadioGolhaCore::open(&path) {
        Ok(core) => {
            let conn = core.connection();
            let artist_info = conn.query_row(
                "SELECT name, avatar FROM artist WHERE id = ?1", [artist_id],
                |row| Ok((row.get::<_, String>(0)?, row.get::<_, Option<String>>(1)?))
            ).unwrap();

            let mut stmt = conn.prepare(
                "SELECT p.id, p.title, p.no, COALESCE((SELECT GROUP_CONCAT(a2.name, ' و ') FROM program_singers ps JOIN singer s ON s.id = ps.singer_id JOIN artist a2 ON a2.id = s.artist_id WHERE ps.program_id = p.id), 'ناشناس'),
                (SELECT GROUP_CONCAT(m.name, ' و ') FROM program_modes pm JOIN mode m ON m.id = pm.mode_id WHERE pm.program_id = p.id),
                (SELECT MAX(end_time) FROM program_timeline WHERE program_id = p.id), p.audio_url
                FROM program p WHERE EXISTS (SELECT 1 FROM program_singers ps JOIN singer s ON s.id = ps.singer_id WHERE ps.program_id = p.id AND s.artist_id = ?1)
                OR EXISTS (SELECT 1 FROM program_performers pp JOIN performer pf ON pf.id = pp.performer_id WHERE pp.program_id = p.id AND pf.artist_id = ?1)
                ORDER BY p.no ASC"
            ).unwrap();
            
            let programs: Vec<_> = stmt.query_map([artist_id], |row| {
                Ok(IosCategoryProgramDto {
                    id: row.get(0)?, title: row.get(1)?, no: row.get(2)?, artist: row.get(3)?, mode: row.get(4)?, duration: row.get(5)?, audio_url: row.get(6)?,
                })
            }).unwrap().filter_map(|r| r.ok()).collect();

            let payload = IosArtistDetailResponse {
                id: artist_id, name: artist_info.0, avatar: artist_info.1, instrument: None, track_count: programs.len() as i64, programs
            };
            rust_str_to_c(to_string(&payload).unwrap())
        },
        Err(e) => {
            eprintln!("Error opening database: {:?}", e);
            rust_str_to_c(json!({ "error": e.to_string() }).to_string())
        }
    }
}

#[no_mangle]
pub extern "C" fn get_singers_json(db_path: *const c_char) -> *mut c_char {
    let path = get_path(db_path);
    match RadioGolhaCore::open(&path) {
        Ok(core) => {
            let conn = core.connection();
            let mut stmt = conn.prepare("SELECT a.id, a.name, a.avatar, COUNT(DISTINCT ps.program_id) FROM program_singers ps JOIN singer s ON s.id = ps.singer_id JOIN artist a ON a.id = s.artist_id GROUP BY s.id ORDER BY a.name ASC").unwrap();
            let rows: Vec<_> = stmt.query_map([], |row| {
                Ok(json!({ "id": row.get::<_, i64>(0)?, "name": row.get::<_, String>(1)?, "avatar": row.get::<_, Option<String>>(2)?, "programCount": row.get::<_, i64>(3)? }))
            }).unwrap().filter_map(|r| r.ok()).collect();
            rust_str_to_c(to_string(&rows).unwrap())
        },
        Err(_) => rust_str_to_c("[]".to_string())
    }
}

#[no_mangle]
pub extern "C" fn get_musicians_json(db_path: *const c_char) -> *mut c_char {
    let path = get_path(db_path);
    match RadioGolhaCore::open(&path) {
        Ok(core) => {
            let conn = core.connection();
            let stmt_result = conn.prepare("SELECT a.id, a.name, a.avatar, COALESCE(i.name, 'نوازنده'), COUNT(DISTINCT pp.program_id) FROM program_performers pp JOIN performer p ON p.id = pp.performer_id JOIN artist a ON a.id = p.artist_id LEFT JOIN instrument i ON i.id = pp.instrument_id GROUP BY p.id ORDER BY a.name ASC");
            match stmt_result {
                Ok(mut stmt) => {
                    let rows: Vec<_> = match stmt.query_map([], |row| {
                        Ok(json!({ "id": row.get::<_, i64>(0)?, "name": row.get::<_, String>(1)?, "avatar": row.get::<_, Option<String>>(2)?, "instrument": row.get::<_, String>(3)?, "programCount": row.get::<_, i64>(4)? }))
                    }) {
                        Ok(mapped) => mapped.filter_map(|r| r.ok()).collect(),
                        Err(_) => vec![],
                    };
                    rust_str_to_c(to_string(&rows).unwrap_or_else(|_| "[]".to_string()))
                },
                Err(_) => rust_str_to_c("[]".to_string())
            }
        },
        Err(_) => rust_str_to_c("[]".to_string())
    }
}

#[no_mangle]
pub extern "C" fn get_search_options_json(db_path: *const c_char) -> *mut c_char {
    let path = get_path(db_path);
    match RadioGolhaCore::open(&path) {
        Ok(core) => {
            let options = core.program_search_options().unwrap();
            rust_str_to_c(to_string(&options).unwrap_or_else(|_| "{}".to_string()))
        },
        Err(_) => rust_str_to_c("{}".to_string())
    }
}

#[no_mangle]
pub extern "C" fn search_programs_json(db_path: *const c_char, filters_json: *const c_char) -> *mut c_char {
    let path = get_path(db_path);
    let filters_raw = get_path(filters_json);
    let req: IosSearchRequest = serde_json::from_str(&filters_raw).unwrap_or_default();
    
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
    
    match RadioGolhaCore::open(&path) {
        Ok(core) => {
            let result = core.search_programs(&filters, req.page.unwrap_or(1)).unwrap();
            rust_str_to_c(to_string(&result).unwrap_or_else(|_| "{}".to_string()))
        },
        Err(_) => rust_str_to_c("{}".to_string())
    }
}

#[no_mangle]
pub extern "C" fn get_duet_programs_json(db_path: *const c_char, singer1: *const c_char, singer2: *const c_char) -> *mut c_char {
    let path = get_path(db_path);
    let s1 = get_path(singer1);
    let s2 = get_path(singer2);
    match RadioGolhaCore::open(&path) {
        Ok(core) => {
            let conn = core.connection();
            let mut stmt = conn.prepare("
                SELECT p.id, p.title, p.no,
                       COALESCE((SELECT GROUP_CONCAT(a.name, ' و ') FROM program_singers ps2 JOIN singer s2 ON s2.id = ps2.singer_id JOIN artist a ON a.id = s2.artist_id WHERE ps2.program_id = p.id), 'ناشناس'),
                       (SELECT GROUP_CONCAT(m.name, ' و ') FROM program_modes pm JOIN mode m ON m.id = pm.mode_id WHERE pm.program_id = p.id),
                       (SELECT MAX(end_time) FROM program_timeline WHERE program_id = p.id),
                       p.audio_url
                FROM program p
                WHERE (SELECT COUNT(*) FROM program_singers ps WHERE ps.program_id = p.id) = 2
                  AND EXISTS (SELECT 1 FROM program_singers ps JOIN singer s ON s.id = ps.singer_id JOIN artist a ON a.id = s.artist_id WHERE ps.program_id = p.id AND a.name = ?1)
                  AND EXISTS (SELECT 1 FROM program_singers ps JOIN singer s ON s.id = ps.singer_id JOIN artist a ON a.id = s.artist_id WHERE ps.program_id = p.id AND a.name = ?2)
                ORDER BY p.no ASC
            ").unwrap();
            let rows: Vec<_> = stmt.query_map([s1, s2], |row| {
                Ok(IosCategoryProgramDto { id: row.get(0)?, title: row.get(1)?, no: row.get(2)?, artist: row.get(3)?, mode: row.get(4)?, duration: row.get(5)?, audio_url: row.get(6)? })
            }).unwrap().filter_map(|r| r.ok()).collect();
            rust_str_to_c(to_string(&rows).unwrap_or_else(|_| "[]".to_string()))
        },
        Err(_) => rust_str_to_c("[]".to_string())
    }
}

#[no_mangle]
pub extern "C" fn get_programs_by_mode_json(db_path: *const c_char, mode_id: i64) -> *mut c_char {
    let path = get_path(db_path);
    match RadioGolhaCore::open(&path) {
        Ok(core) => {
            let conn = core.connection();
            let mut stmt = conn.prepare("SELECT p.id, p.title, p.no, COALESCE((SELECT GROUP_CONCAT(a.name, ' و ') FROM program_singers ps JOIN singer s ON s.id = ps.singer_id JOIN artist a ON a.id = s.artist_id WHERE ps.program_id = p.id), 'ناشناس'), (SELECT GROUP_CONCAT(m2.name, ' و ') FROM program_modes pm2 JOIN mode m2 ON m2.id = pm2.mode_id WHERE pm2.program_id = p.id), (SELECT MAX(end_time) FROM program_timeline WHERE program_id = p.id), p.audio_url FROM program p JOIN program_modes pm ON pm.program_id = p.id WHERE pm.mode_id = ?1 ORDER BY p.no ASC").unwrap();
            let rows: Vec<_> = stmt.query_map([mode_id], |row| {
                Ok(IosCategoryProgramDto { id: row.get(0)?, title: row.get(1)?, no: row.get(2)?, artist: row.get(3)?, mode: row.get(4)?, duration: row.get(5)?, audio_url: row.get(6)? })
            }).unwrap().filter_map(|r| r.ok()).collect();
            rust_str_to_c(to_string(&rows).unwrap_or_else(|_| "[]".to_string()))
        },
        Err(_) => rust_str_to_c("[]".to_string())
    }
}

#[no_mangle]
pub extern "C" fn get_programs_by_ids_json(db_path: *const c_char, ids_json: *const c_char) -> *mut c_char {
    let path = get_path(db_path);
    let ids_str = get_path(ids_json);
    let ids: Vec<i64> = serde_json::from_str(&ids_str).unwrap_or_default();
    if ids.is_empty() { return rust_str_to_c("[]".to_string()); }
    let id_list = ids.iter().map(|id| id.to_string()).collect::<Vec<_>>().join(",");
    let sql = format!("SELECT p.id, p.title, p.no, COALESCE((SELECT GROUP_CONCAT(a.name, ' و ') FROM program_singers ps JOIN singer s ON s.id = ps.singer_id JOIN artist a ON a.id = s.artist_id WHERE ps.program_id = p.id), 'ناشناس'), (SELECT GROUP_CONCAT(m.name, ' و ') FROM program_modes pm JOIN mode m ON m.id = pm.mode_id WHERE pm.program_id = p.id), (SELECT MAX(end_time) FROM program_timeline WHERE program_id = p.id), p.audio_url FROM program p WHERE p.id IN ({}) ORDER BY p.no ASC", id_list);
    match RadioGolhaCore::open(&path) {
        Ok(core) => {
            let mut stmt = core.connection().prepare(&sql).unwrap();
            let rows: Vec<_> = stmt.query_map([], |row| {
                Ok(IosCategoryProgramDto { id: row.get(0)?, title: row.get(1)?, no: row.get(2)?, artist: row.get(3)?, mode: row.get(4)?, duration: row.get(5)?, audio_url: row.get(6)? })
            }).unwrap().filter_map(|r| r.ok()).collect();
            rust_str_to_c(to_string(&rows).unwrap_or_else(|_| "[]".to_string()))
        },
        Err(_) => rust_str_to_c("[]".to_string())
    }
}

#[no_mangle]
pub extern "C" fn get_programs_by_category_json(db_path: *const c_char, category_id: i64) -> *mut c_char {
    let path = get_path(db_path);
    match RadioGolhaCore::open(&path) {
        Ok(core) => {
            let conn = core.connection();
            let mut stmt = conn.prepare("SELECT p.id, p.title, p.no, COALESCE((SELECT GROUP_CONCAT(a.name, ' و ') FROM program_singers ps JOIN singer s ON s.id = ps.singer_id JOIN artist a ON a.id = s.artist_id WHERE ps.program_id = p.id), 'ناشناس'), (SELECT GROUP_CONCAT(m.name, ' و ') FROM program_modes pm JOIN mode m ON m.id = pm.mode_id WHERE pm.program_id = p.id), (SELECT MAX(end_time) FROM program_timeline WHERE program_id = p.id), p.audio_url FROM program p WHERE p.category_id = ?1 ORDER BY p.no ASC").unwrap();
            let rows: Vec<_> = stmt.query_map([category_id], |row| {
                Ok(IosCategoryProgramDto { id: row.get(0)?, title: row.get(1)?, no: row.get(2)?, artist: row.get(3)?, mode: row.get(4)?, duration: row.get(5)?, audio_url: row.get(6)? })
            }).unwrap().filter_map(|r| r.ok()).collect();
            rust_str_to_c(to_string(&rows).unwrap_or_else(|_| "[]".to_string()))
        },
        Err(_) => rust_str_to_c("[]".to_string())
    }
}

#[no_mangle]
pub extern "C" fn get_program_detail_json(db_path: *const c_char, program_id: i64) -> *mut c_char {
    let path = get_path(db_path);
    match RadioGolhaCore::open(&path) {
        Ok(core) => {
            match core.get_program_detail(program_id) {
                Ok(Some(detail)) => {
                    rust_str_to_c(to_string(&detail).unwrap_or_else(|_| String::new()))
                }
                Ok(None) => rust_str_to_c(String::new()),
                Err(_) => rust_str_to_c(String::new()),
            }
        },
        Err(_) => rust_str_to_c(String::new())
    }
}

#[no_mangle]
pub extern "C" fn get_orchestras_json(db_path: *const c_char) -> *mut c_char {
    let path = get_path(db_path);
    match RadioGolhaCore::open(&path) {
        Ok(core) => {
            let conn = core.connection();
            let mut stmt = conn.prepare("
                SELECT o.id, o.name, COUNT(DISTINCT po.program_id)
                FROM orchestra o
                LEFT JOIN program_orchestras po ON po.orchestra_id = o.id
                GROUP BY o.id
                ORDER BY o.name ASC
            ").unwrap();
            let rows: Vec<_> = stmt.query_map([], |row| {
                Ok(json!({ "id": row.get::<_, i64>(0)?, "name": row.get::<_, String>(1)?, "programCount": row.get::<_, i64>(2)? }))
            }).unwrap().filter_map(|r| r.ok()).collect();
            rust_str_to_c(to_string(&rows).unwrap_or_else(|_| "[]".to_string()))
        },
        Err(_) => rust_str_to_c("[]".to_string())
    }
}

#[no_mangle]
pub extern "C" fn get_programs_by_orchestra_json(db_path: *const c_char, orchestra_id: i64) -> *mut c_char {
    let path = get_path(db_path);
    match RadioGolhaCore::open(&path) {
        Ok(core) => {
            let conn = core.connection();
            let mut stmt = conn.prepare("SELECT p.id, p.title, p.no, COALESCE((SELECT GROUP_CONCAT(a.name, ' و ') FROM program_singers ps JOIN singer s ON s.id = ps.singer_id JOIN artist a ON a.id = s.artist_id WHERE ps.program_id = p.id), 'ناشناس'), (SELECT GROUP_CONCAT(m.name, ' و ') FROM program_modes pm JOIN mode m ON m.id = pm.mode_id WHERE pm.program_id = p.id), (SELECT MAX(end_time) FROM program_timeline WHERE program_id = p.id), p.audio_url FROM program p JOIN program_orchestras po ON po.program_id = p.id WHERE po.orchestra_id = ?1 ORDER BY p.no ASC").unwrap();
            let rows: Vec<_> = stmt.query_map([orchestra_id], |row| {
                Ok(IosCategoryProgramDto { id: row.get(0)?, title: row.get(1)?, no: row.get(2)?, artist: row.get(3)?, mode: row.get(4)?, duration: row.get(5)?, audio_url: row.get(6)? })
            }).unwrap().filter_map(|r| r.ok()).collect();
            rust_str_to_c(to_string(&rows).unwrap_or_else(|_| "[]".to_string()))
        },
        Err(_) => rust_str_to_c("[]".to_string())
    }
}

#[no_mangle]
pub extern "C" fn get_top_tracks_json(db_path: *const c_char) -> *mut c_char {
    let path = get_path(db_path);
    match RadioGolhaCore::open(&path) {
        Ok(core) => {
            let tracks = core.random_vocal_track_summaries(20).unwrap_or_default();
            rust_str_to_c(to_string(&tracks).unwrap_or_else(|_| "[]".to_string()))
        },
        Err(_) => rust_str_to_c("[]".to_string())
    }
}

#[no_mangle]
pub extern "C" fn get_ordered_modes_json(db_path: *const c_char) -> *mut c_char {
    let path = get_path(db_path);
    match RadioGolhaCore::open(&path) {
        Ok(core) => rust_str_to_c(to_string(&core.get_ordered_modes().unwrap_or_default()).unwrap_or_else(|_| "[]".to_string())),
        Err(_) => rust_str_to_c("[]".to_string())
    }
}

#[no_mangle]
pub extern "C" fn get_duet_pairs_config_json(db_path: *const c_char) -> *mut c_char {
    let path = get_path(db_path);
    match RadioGolhaCore::open(&path) {
        Ok(core) => rust_str_to_c(core.get_duet_pairs_raw().unwrap_or_else(|_| "[]".to_string())),
        Err(_) => rust_str_to_c("[]".to_string())
    }
}

#[no_mangle]
pub extern "C" fn get_config_json(db_path: *const c_char, key: *const c_char) -> *mut c_char {
    let path = get_path(db_path);
    let k = get_path(key);
    match RadioGolhaCore::open(&path) {
        Ok(core) => {
            let val = core.get_config(&k).unwrap_or_default().unwrap_or_default();
            rust_str_to_c(to_string(&val).unwrap_or_else(|_| "\"\"".to_string()))
        },
        Err(_) => rust_str_to_c("\"\"".to_string())
    }
}

// --- User Data Implementation ---

#[no_mangle]
pub extern "C" fn get_all_playlists_json(user_db_path: *const c_char) -> *mut c_char {
    let path = get_path(user_db_path);
    match UserDataStore::open(&path) {
        Ok(store) => rust_str_to_c(to_string(&store.get_all_playlists().unwrap_or_default()).unwrap_or_else(|_| "[]".to_string())),
        Err(_) => rust_str_to_c("[]".to_string())
    }
}

#[no_mangle]
pub extern "C" fn get_playlist_json(user_db_path: *const c_char, id: i64) -> *mut c_char {
    let path = get_path(user_db_path);
    match UserDataStore::open(&path) {
        Ok(store) => {
            let p = store.get_playlist(id).unwrap_or(None);
            rust_str_to_c(to_string(&p).unwrap_or_else(|_| "{}".to_string()))
        },
        Err(_) => rust_str_to_c("{}".to_string())
    }
}

#[no_mangle]
pub extern "C" fn create_playlist_bridge(user_db_path: *const c_char, request_json: *const c_char) -> *mut c_char {
    let path = get_path(user_db_path);
    let req_str = get_path(request_json);
    let req: CreatePlaylistRequest = serde_json::from_str(&req_str).unwrap();
    match UserDataStore::open(&path) {
        Ok(store) => {
            let id = store.create_playlist(&req.name, &req.playlist_type.unwrap_or("manual".to_string()), &req.filters_json.unwrap_or("{}".to_string())).unwrap_or(0);
            rust_str_to_c(id.to_string())
        },
        Err(_) => rust_str_to_c("0".to_string())
    }
}

#[no_mangle]
pub extern "C" fn rename_playlist_bridge(user_db_path: *const c_char, id: i64, name: *const c_char) -> *mut c_char {
    let path = get_path(user_db_path);
    let n = get_path(name);
    match UserDataStore::open(&path) {
        Ok(store) => {
            store.rename_playlist(id, &n).unwrap_or(());
            rust_str_to_c("OK".to_string())
        },
        Err(_) => rust_str_to_c("ERROR".to_string())
    }
}

#[no_mangle]
pub extern "C" fn delete_playlist_bridge(user_db_path: *const c_char, id: i64) -> *mut c_char {
    let path = get_path(user_db_path);
    match UserDataStore::open(&path) {
        Ok(store) => {
            store.delete_playlist(id).unwrap_or(());
            rust_str_to_c("OK".to_string())
        },
        Err(_) => rust_str_to_c("ERROR".to_string())
    }
}

#[no_mangle]
pub extern "C" fn add_track_to_playlist_bridge(user_db_path: *const c_char, playlist_id: i64, track_id: i64) -> *mut c_char {
    let path = get_path(user_db_path);
    match UserDataStore::open(&path) {
        Ok(store) => {
            store.add_track(playlist_id, track_id).unwrap_or(());
            rust_str_to_c("OK".to_string())
        },
        Err(_) => rust_str_to_c("ERROR".to_string())
    }
}

#[no_mangle]
pub extern "C" fn remove_track_from_playlist_bridge(user_db_path: *const c_char, playlist_id: i64, track_id: i64) -> *mut c_char {
    let path = get_path(user_db_path);
    match UserDataStore::open(&path) {
        Ok(store) => {
            store.remove_track(playlist_id, track_id).unwrap_or(());
            rust_str_to_c("OK".to_string())
        },
        Err(_) => rust_str_to_c("ERROR".to_string())
    }
}

#[no_mangle]
pub extern "C" fn get_manual_playlists_json(user_db_path: *const c_char) -> *mut c_char {
    let path = get_path(user_db_path);
    match UserDataStore::open(&path) {
        Ok(store) => rust_str_to_c(to_string(&store.get_manual_playlists().unwrap_or_default()).unwrap_or_else(|_| "[]".to_string())),
        Err(_) => rust_str_to_c("[]".to_string())
    }
}

#[no_mangle]
pub extern "C" fn add_favorite_artist_bridge(user_db_path: *const c_char, artist_id: i64, artist_type: *const c_char) -> *mut c_char {
    let path = get_path(user_db_path);
    let atype = get_path(artist_type);
    match UserDataStore::open(&path) {
        Ok(store) => {
            store.add_favorite_artist(artist_id, &atype).unwrap_or(());
            rust_str_to_c("OK".to_string())
        },
        Err(_) => rust_str_to_c("ERROR".to_string())
    }
}

#[no_mangle]
pub extern "C" fn remove_favorite_artist_bridge(user_db_path: *const c_char, artist_id: i64) -> *mut c_char {
    let path = get_path(user_db_path);
    match UserDataStore::open(&path) {
        Ok(store) => {
            store.remove_favorite_artist(artist_id).unwrap_or(());
            rust_str_to_c("OK".to_string())
        },
        Err(_) => rust_str_to_c("ERROR".to_string())
    }
}

#[no_mangle]
pub extern "C" fn is_favorite_artist_bridge(user_db_path: *const c_char, artist_id: i64) -> *mut c_char {
    let path = get_path(user_db_path);
    match UserDataStore::open(&path) {
        Ok(store) => rust_str_to_c(store.is_favorite_artist(artist_id).unwrap_or(false).to_string()),
        Err(_) => rust_str_to_c("false".to_string())
    }
}

#[no_mangle]
pub extern "C" fn get_favorite_artist_ids_json(user_db_path: *const c_char, artist_type: *const c_char) -> *mut c_char {
    let path = get_path(user_db_path);
    let atype = get_path(artist_type);
    match UserDataStore::open(&path) {
        Ok(store) => {
            let ids = if atype.is_empty() {
                store.get_favorite_artist_ids().unwrap_or_default()
            } else {
                store.get_favorite_artist_ids_by_type(&atype).unwrap_or_default()
            };
            rust_str_to_c(to_string(&ids).unwrap_or_else(|_| "[]".to_string()))
        },
        Err(_) => rust_str_to_c("[]".to_string())
    }
}

#[no_mangle]
pub extern "C" fn record_playback_bridge(user_db_path: *const c_char, track_id: i64) -> *mut c_char {
    let path = get_path(user_db_path);
    match UserDataStore::open(&path) {
        Ok(store) => {
            store.record_playback(track_id).unwrap_or(());
            rust_str_to_c("OK".to_string())
        },
        Err(_) => rust_str_to_c("ERROR".to_string())
    }
}

#[no_mangle]
pub extern "C" fn get_recently_played_ids_json(user_db_path: *const c_char, limit: i64) -> *mut c_char {
    let path = get_path(user_db_path);
    match UserDataStore::open(&path) {
        Ok(store) => rust_str_to_c(to_string(&store.get_recent_tracks(limit).unwrap_or_default()).unwrap_or_else(|_| "[]".to_string())),
        Err(_) => rust_str_to_c("[]".to_string())
    }
}

#[no_mangle]
pub extern "C" fn get_most_played_ids_json(user_db_path: *const c_char, limit: i64) -> *mut c_char {
    let path = get_path(user_db_path);
    match UserDataStore::open(&path) {
        Ok(store) => rust_str_to_c(to_string(&store.get_most_played_tracks(limit).unwrap_or_default()).unwrap_or_else(|_| "[]".to_string())),
        Err(_) => rust_str_to_c("[]".to_string())
    }
}
