use napi::Result as NapiResult;
use napi::bindgen_prelude::Error as NapiError;
use napi_derive::napi;
use radiogolha_core::{LookupKind, ProgramSearchFilters, RadioGolhaCore};

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

#[napi(js_name = "dashboardOverview")]
pub fn dashboard_overview(db_path: String) -> NapiResult<String> {
    let core = open_core(db_path)?;
    serialize(&core.admin_dashboard_overview().map_err(|error| NapiError::from_reason(error.to_string()))?)
}

#[napi(js_name = "listPrograms")]
pub fn list_programs(
    db_path: String,
    search: String,
    page: i64,
    category_id: Option<i64>,
    singer_id: Option<i64>,
) -> NapiResult<String> {
    let core = open_core(db_path)?;
    serialize(
        &core
            .admin_program_list(&search, page, category_id, singer_id)
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
            .admin_artist_list(&search, page, role.as_deref())
            .map_err(|error| NapiError::from_reason(error.to_string()))?,
    )
}

#[napi(js_name = "listLookupItems")]
pub fn list_lookup_items(db_path: String, kind: String, search: String, page: i64) -> NapiResult<String> {
    let core = open_core(db_path)?;
    let kind = parse_lookup_kind(kind)?;
    serialize(
        &core
            .admin_lookup_list(kind, &search, page)
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
#[allow(clippy::too_many_arguments)]
pub fn search_programs(
    db_path: String,
    transcript_query: Option<String>,
    page: i64,
    category_ids: Vec<i64>,
    mode_ids: Vec<i64>,
    orchestra_ids: Vec<i64>,
    instrument_ids: Vec<i64>,
    singer_ids: Vec<i64>,
    poet_ids: Vec<i64>,
    announcer_ids: Vec<i64>,
    composer_ids: Vec<i64>,
    arranger_ids: Vec<i64>,
    performer_ids: Vec<i64>,
    orchestra_leader_ids: Vec<i64>,
) -> NapiResult<String> {
    let core = open_core(db_path)?;
    let filters = ProgramSearchFilters {
        transcript_query: transcript_query.filter(|value| !value.trim().is_empty()),
        category_ids,
        mode_ids,
        orchestra_ids,
        instrument_ids,
        singer_ids,
        poet_ids,
        announcer_ids,
        composer_ids,
        arranger_ids,
        performer_ids,
        orchestra_leader_ids,
    };
    serialize(
        &core
            .admin_program_search(&filters, page)
            .map_err(|error| NapiError::from_reason(error.to_string()))?,
    )
}
