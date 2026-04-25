use serde::Serialize;

#[derive(Serialize)]
pub struct HomeProgram {
    id: i64,
    title: String,
    count: i64,
}

#[derive(Serialize)]
pub struct HomeTrack {
    id: i64,
    title: String,
    artist: String,
    duration: String,
}

#[derive(Serialize)]
#[serde(rename_all = "camelCase")]
pub struct BootstrapPayload {
    app_name: String,
    programs: Vec<HomeProgram>,
    tracks: Vec<HomeTrack>,
}

#[tauri::command]
pub fn get_bootstrap_payload() -> BootstrapPayload {
    BootstrapPayload {
        app_name: "رادیو گل‌ها".to_string(),
        programs: vec![
            HomeProgram { id: 1, title: "گل‌های تازه".to_string(), count: 15 },
            HomeProgram { id: 2, title: "برگ سبز".to_string(), count: 32 },
            HomeProgram { id: 3, title: "یک شاخه گل".to_string(), count: 468 },
            HomeProgram { id: 4, title: "گل‌های جاویدان".to_string(), count: 101 },
        ],
        tracks: vec![
            HomeTrack { id: 1, title: "گل‌های صحرایی ۵۰".to_string(), artist: "ویگن".to_string(), duration: "14:02".to_string() },
            HomeTrack { id: 2, title: "یک شاخه گل ۶۷".to_string(), artist: "ناصر مسعودی".to_string(), duration: "14:32".to_string() },
            HomeTrack { id: 3, title: "گل‌های جاویدان ۱۴۰".to_string(), artist: "حسین قوامی".to_string(), duration: "47:36".to_string() },
        ],
    }
}
