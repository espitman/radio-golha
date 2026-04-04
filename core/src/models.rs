#[derive(Debug, Clone)]
pub struct DashboardSummary {
    pub total_programs: i64,
    pub total_artists: i64,
    pub total_segments: i64,
    pub total_modes: i64,
    pub programs_with_audio: i64,
    pub programs_with_timeline: i64,
}

#[derive(Debug, Clone)]
pub struct CategoryStat {
    pub name: String,
    pub total: i64,
}

#[derive(Debug, Clone)]
pub struct RankedNameStat {
    pub name: String,
    pub total: i64,
}

#[derive(Debug, Clone)]
pub struct ProgramListItem {
    pub id: i64,
    pub title: String,
    pub category_name: String,
    pub no: i64,
    pub sub_no: Option<String>,
}

#[derive(Debug, Clone)]
pub struct PerformerCredit {
    pub name: String,
    pub instrument: Option<String>,
}

#[derive(Debug, Clone)]
pub struct OrchestraLeaderCredit {
    pub orchestra: String,
    pub leader: String,
}

#[derive(Debug, Clone)]
pub struct TimelineSegment {
    pub id: i64,
    pub start_time: Option<String>,
    pub end_time: Option<String>,
    pub mode_name: Option<String>,
    pub singers: Vec<String>,
    pub poets: Vec<String>,
    pub announcers: Vec<String>,
    pub orchestras: Vec<String>,
    pub orchestra_leaders: Vec<OrchestraLeaderCredit>,
    pub performers: Vec<PerformerCredit>,
}

#[derive(Debug, Clone)]
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
}
