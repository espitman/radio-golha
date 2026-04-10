use serde::Serialize;

#[derive(Debug, Clone, Serialize)]
pub struct DashboardSummary {
    #[serde(rename = "totalPrograms")]
    pub total_programs: i64,
    #[serde(rename = "totalArtists")]
    pub total_artists: i64,
    #[serde(rename = "totalSegments")]
    pub total_segments: i64,
    #[serde(rename = "totalModes")]
    pub total_modes: i64,
    #[serde(rename = "programsWithAudio")]
    pub programs_with_audio: i64,
    #[serde(rename = "programsWithTimeline")]
    pub programs_with_timeline: i64,
    #[serde(rename = "totalCategories")]
    pub total_categories: i64,
    #[serde(rename = "totalOrchestras")]
    pub total_orchestras: i64,
    #[serde(rename = "totalInstruments")]
    pub total_instruments: i64,
}

#[derive(Debug, Clone, Serialize)]
pub struct CategoryStat {
    pub name: String,
    pub total: i64,
}

#[derive(Debug, Clone, Serialize)]
pub struct RankedNameStat {
    pub name: String,
    pub avatar: Option<String>,
    pub total: i64,
}

#[derive(Debug, Clone, Serialize)]
pub struct RankedPerformerStat {
    pub name: String,
    pub avatar: Option<String>,
    pub instrument: Option<String>,
    pub total: i64,
}

#[derive(Debug, Clone, Serialize)]
pub struct CategoryOption {
    pub id: i64,
    pub title_fa: String,
}

#[derive(Debug, Clone, Serialize)]
pub struct SearchOption {
    pub id: i64,
    pub name: String,
}

#[derive(Debug, Clone, Serialize)]
pub struct SingerOption {
    pub id: i64,
    pub name: String,
}

#[derive(Debug, Clone, Serialize)]
pub struct ArtistListItem {
    pub id: i64,
    pub name: String,
    pub avatar: Option<String>,
    pub is_singer: i64,
    pub is_performer: i64,
    pub is_poet: i64,
    pub is_announcer: i64,
    pub is_composer: i64,
    pub is_arranger: i64,
}

#[derive(Debug, Clone, Serialize)]
pub struct ArtistStats {
    pub total_artists: i64,
    pub singers: i64,
    pub performers: i64,
    pub poets: i64,
}

#[derive(Debug, Clone, Serialize)]
#[serde(rename_all = "camelCase")]
pub struct ArtistListResponse {
    pub rows: Vec<ArtistListItem>,
    pub stats: ArtistStats,
    pub total: i64,
    pub page: i64,
    pub total_pages: i64,
    pub active_role: Option<String>,
}

#[derive(Debug, Clone, Serialize)]
pub struct LookupListItem {
    pub id: i64,
    pub name: String,
    pub usage_count: i64,
}

#[derive(Debug, Clone, Serialize)]
pub struct LookupStats {
    pub total_items: i64,
    pub total_usage: i64,
}

#[derive(Debug, Clone, Serialize)]
#[serde(rename_all = "camelCase")]
pub struct LookupListResponse {
    pub rows: Vec<LookupListItem>,
    pub stats: LookupStats,
    pub total: i64,
    pub page: i64,
    pub total_pages: i64,
}

#[derive(Debug, Clone, Serialize)]
pub struct ProgramListItem {
    pub id: i64,
    pub title: String,
    pub category_name: String,
    pub no: i64,
    pub sub_no: Option<String>,
    pub duration: Option<String>,
}

#[derive(Debug, Clone, Serialize)]
#[serde(rename_all = "camelCase")]
pub struct ProgramListResponse {
    pub rows: Vec<ProgramListItem>,
    pub categories: Vec<CategoryOption>,
    pub singers: Vec<SingerOption>,
    pub total: i64,
    pub page: i64,
    pub total_pages: i64,
    pub active_category_id: Option<i64>,
    pub active_singer_id: Option<i64>,
}

#[derive(Debug, Clone, Serialize)]
#[serde(rename_all = "camelCase")]
pub struct ProgramSearchOptions {
    pub categories: Vec<CategoryOption>,
    pub singers: Vec<SearchOption>,
    pub poets: Vec<SearchOption>,
    pub announcers: Vec<SearchOption>,
    pub composers: Vec<SearchOption>,
    pub arrangers: Vec<SearchOption>,
    pub performers: Vec<SearchOption>,
    pub orchestra_leaders: Vec<SearchOption>,
    pub modes: Vec<SearchOption>,
    pub orchestras: Vec<SearchOption>,
    pub instruments: Vec<SearchOption>,
}

#[derive(Debug, Clone, Serialize)]
#[serde(rename_all = "camelCase")]
pub struct ProgramSearchResponse {
    pub rows: Vec<ProgramListItem>,
    pub total: i64,
    pub page: i64,
    pub total_pages: i64,
}

#[derive(Debug, Clone, Serialize)]
#[serde(rename_all = "camelCase")]
pub struct DashboardOverview {
    pub summary: DashboardSummary,
    pub category_breakdown: Vec<CategoryStat>,
    pub top_singers: Vec<RankedNameStat>,
    pub top_modes: Vec<RankedNameStat>,
    pub top_orchestras: Vec<RankedNameStat>,
    pub recent_programs: Vec<ProgramListItem>,
}

#[derive(Debug, Clone, Serialize)]
pub struct PerformerCredit {
    pub name: String,
    pub avatar: Option<String>,
    pub instrument: Option<String>,
}

#[derive(Debug, Clone, Serialize)]
pub struct OrchestraLeaderCredit {
    pub orchestra: String,
    #[serde(rename = "name")]
    pub leader: String,
}

#[derive(Debug, Clone, Serialize)]
pub struct TimelineSegment {
    pub id: i64,
    pub start_time: Option<String>,
    pub end_time: Option<String>,
    pub mode_name: Option<String>,
    pub singers: Vec<String>,
    pub poets: Vec<String>,
    pub announcers: Vec<String>,
    pub orchestras: Vec<String>,
    #[serde(rename = "orchestraLeaders")]
    pub orchestra_leaders: Vec<OrchestraLeaderCredit>,
    pub performers: Vec<PerformerCredit>,
}

#[derive(Debug, Clone, Serialize)]
pub struct TranscriptVerse {
    pub segment_order: i64,
    pub verse_order: i64,
    pub text: String,
}

#[derive(Debug, Clone, Serialize)]
pub struct ProgramDetail {
    pub id: i64,
    pub title: String,
    pub category_name: String,
    pub no: i64,
    pub sub_no: Option<String>,
    pub audio_url: Option<String>,
    pub singers: Vec<String>,
    pub poets: Vec<String>,
    pub announcers: Vec<String>,
    pub composers: Vec<String>,
    pub arrangers: Vec<String>,
    pub modes: Vec<String>,
    pub orchestras: Vec<String>,
    pub orchestra_leaders: Vec<OrchestraLeaderCredit>,
    pub performers: Vec<PerformerCredit>,
    pub timeline: Vec<TimelineSegment>,
    pub transcript: Vec<TranscriptVerse>,
}

#[derive(Debug, Clone, Serialize)]
pub struct ArtistDetail {
    pub id: i64,
    pub name: String,
    pub avatar: Option<String>,
}

#[derive(Debug, Clone, serde::Deserialize)]
pub struct ArtistUpdatePayload {
    pub name: String,
    pub avatar: Option<String>,
}
