use napi::Result as NapiResult;
use napi::bindgen_prelude::Error as NapiError;
use napi_derive::napi;
use radiogolha_core::{
    LookupKind, ProgramSearchFilters, ProgramSortField, RadioGolhaCore, SearchMatchMode,
    SortDirection,
};
use serde::Deserialize;

#[derive(Debug, Deserialize, Default)]
#[serde(rename_all = "camelCase")]
struct SearchProgramsPayload {
    transcript_query: Option<String>,
    page: Option<i64>,
    category_ids: Option<Vec<i64>>,
    mode_ids: Option<Vec<i64>>,
    mode_match: Option<String>,
    orchestra_ids: Option<Vec<i64>>,
    orchestra_match: Option<String>,
    instrument_ids: Option<Vec<i64>>,
    instrument_match: Option<String>,
    singer_ids: Option<Vec<i64>>,
    singer_match: Option<String>,
    poet_ids: Option<Vec<i64>>,
    poet_match: Option<String>,
    announcer_ids: Option<Vec<i64>>,
    announcer_match: Option<String>,
    composer_ids: Option<Vec<i64>>,
    composer_match: Option<String>,
    arranger_ids: Option<Vec<i64>>,
    arranger_match: Option<String>,
    performer_ids: Option<Vec<i64>>,
    performer_match: Option<String>,
    orchestra_leader_ids: Option<Vec<i64>>,
    orchestra_leader_match: Option<String>,
}

fn open_core(db_path: String) -> NapiResult<RadioGolhaCore> {
    RadioGolhaCore::open(db_path).map_err(|error| NapiError::from_reason(error.to_string()))
}

fn serialize<T: serde::Serialize>(value: &T) -> NapiResult<String> {
    serde_json::to_string(value).map_err(|error| NapiError::from_reason(error.to_string()))
}

fn parse_lookup_kind(kind: String) -> NapiResult<LookupKind> {
    match kind.as_str() {
        "orchestras" => Ok(LookupKind::Orchestras),
        "instruments" => Ok(LookupKind::Instruments),
        "modes" => Ok(LookupKind::Modes),
        _ => Err(NapiError::from_reason(format!("Unsupported lookup kind: {kind}"))),
    }
}

fn parse_match_mode(mode: Option<String>) -> SearchMatchMode {
    match mode.as_deref() {
        Some("all") => SearchMatchMode::All,
        _ => SearchMatchMode::Any,
    }
}

#[napi(js_name = "dashboardOverview")]
pub fn dashboard_overview(db_path: String) -> NapiResult<String> {
    let core = open_core(db_path)?;
    serialize(&core.dashboard_overview().map_err(|error| NapiError::from_reason(error.to_string()))?)
}

#[napi(js_name = "listPrograms")]
pub fn list_programs(
    db_path: String,
    search: String,
    page: i64,
    category_id: Option<i64>,
    singer_id: Option<i64>,
    sort_field: Option<String>,
    sort_direction: Option<String>,
) -> NapiResult<String> {
    let core = open_core(db_path)?;
    serialize(
        &core
            .browse_programs(
                &search,
                page,
                category_id,
                singer_id,
                ProgramSortField::from_str(sort_field.as_deref().unwrap_or("no")),
                SortDirection::from_str(sort_direction.as_deref().unwrap_or("asc")),
            )
            .map_err(|error| NapiError::from_reason(error.to_string()))?,
    )
}

#[napi(js_name = "getProgramDetail")]
pub fn get_program_detail(db_path: String, id: i64) -> NapiResult<String> {
    let core = open_core(db_path)?;
    serialize(
        &core
            .get_program_detail(id)
            .map_err(|error| NapiError::from_reason(error.to_string()))?,
    )
}

#[napi(js_name = "listArtists")]
pub fn list_artists(db_path: String, search: String, page: i64, role: Option<String>) -> NapiResult<String> {
    let core = open_core(db_path)?;
    serialize(
        &core
            .browse_artists(&search, page, role.as_deref())
            .map_err(|error| NapiError::from_reason(error.to_string()))?,
    )
}

#[napi(js_name = "listLookupItems")]
pub fn list_lookup_items(db_path: String, kind: String, search: String, page: i64) -> NapiResult<String> {
    let core = open_core(db_path)?;
    let kind = parse_lookup_kind(kind)?;
    serialize(
        &core
            .browse_lookup_items(kind, &search, page)
            .map_err(|error| NapiError::from_reason(error.to_string()))?,
    )
}

#[napi(js_name = "getProgramSearchOptions")]
pub fn get_program_search_options(db_path: String) -> NapiResult<String> {
    let core = open_core(db_path)?;
    serialize(
        &core
            .program_search_options()
            .map_err(|error| NapiError::from_reason(error.to_string()))?,
    )
}

#[napi(js_name = "searchPrograms")]
pub fn search_programs(db_path: String, payload_json: String) -> NapiResult<String> {
    let core = open_core(db_path)?;
    let payload: SearchProgramsPayload =
        serde_json::from_str(&payload_json).map_err(|error| NapiError::from_reason(error.to_string()))?;
    let filters = ProgramSearchFilters {
        transcript_query: payload
            .transcript_query
            .filter(|value| !value.trim().is_empty()),
        category_ids: payload.category_ids.unwrap_or_default(),
        mode_ids: payload.mode_ids.unwrap_or_default(),
        mode_match: parse_match_mode(payload.mode_match),
        orchestra_ids: payload.orchestra_ids.unwrap_or_default(),
        orchestra_match: parse_match_mode(payload.orchestra_match),
        instrument_ids: payload.instrument_ids.unwrap_or_default(),
        instrument_match: parse_match_mode(payload.instrument_match),
        singer_ids: payload.singer_ids.unwrap_or_default(),
        singer_match: parse_match_mode(payload.singer_match),
        poet_ids: payload.poet_ids.unwrap_or_default(),
        poet_match: parse_match_mode(payload.poet_match),
        announcer_ids: payload.announcer_ids.unwrap_or_default(),
        announcer_match: parse_match_mode(payload.announcer_match),
        composer_ids: payload.composer_ids.unwrap_or_default(),
        composer_match: parse_match_mode(payload.composer_match),
        arranger_ids: payload.arranger_ids.unwrap_or_default(),
        arranger_match: parse_match_mode(payload.arranger_match),
        performer_ids: payload.performer_ids.unwrap_or_default(),
        performer_match: parse_match_mode(payload.performer_match),
        orchestra_leader_ids: payload.orchestra_leader_ids.unwrap_or_default(),
        orchestra_leader_match: parse_match_mode(payload.orchestra_leader_match),
        sort_field: ProgramSortField::No,
        sort_direction: SortDirection::Asc,
    };
    serialize(
        &core
            .search_programs(&filters, payload.page.unwrap_or(1))
            .map_err(|error| NapiError::from_reason(error.to_string()))?,
    )
}

#[napi(js_name = "getArtistDetail")]
pub fn get_artist_detail(db_path: String, id: i64) -> NapiResult<String> {
    let core = open_core(db_path)?;
    serialize(
        &core
            .get_artist_detail(id)
            .map_err(|error| NapiError::from_reason(error.to_string()))?,
    )
}

#[napi(js_name = "updateArtist")]
pub fn update_artist(db_path: String, id: i64, name: String, avatar: Option<String>) -> NapiResult<()> {
    let core =
        RadioGolhaCore::open_rw(db_path).map_err(|error| NapiError::from_reason(error.to_string()))?;
    core.update_artist(id, &name, avatar.as_deref())
        .map_err(|error| NapiError::from_reason(error.to_string()))?;
    Ok(())
}

