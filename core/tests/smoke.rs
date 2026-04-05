use radiogolha_core::{LookupKind, ProgramSearchFilters, RadioGolhaCore};

fn open_core() -> RadioGolhaCore {
    RadioGolhaCore::open("../database/golha_database.db").expect("database should open")
}

#[test]
fn dashboard_summary_reads_real_counts() {
    let core = open_core();
    let summary = core.dashboard_summary().expect("summary should load");

    assert_eq!(summary.total_programs, 1440);
    assert_eq!(summary.total_artists, 671);
    assert!(summary.total_segments > 20_000);
    assert!(summary.programs_with_audio > 1000);
}

#[test]
fn program_detail_exposes_sub_number_and_orchestra_leaders() {
    let core = open_core();
    let detail = core
        .get_program_detail(1251)
        .expect("detail query should succeed")
        .expect("program should exist");

    assert_eq!(detail.no, 247);
    assert!(detail.title.contains("گلهای رنگارنگ"));
    assert_eq!(detail.orchestras.len(), 1);
    assert_eq!(detail.orchestra_leaders.len(), 2);
    assert!(detail
        .orchestra_leaders
        .iter()
        .any(|item| item.leader == "جواد معروفی"));
    assert!(detail.timeline.iter().any(|segment| !segment.orchestra_leaders.is_empty()));
}

#[test]
fn admin_artist_list_returns_stats_and_rows() {
    let core = open_core();
    let response = core
        .admin_artist_list("", 1, Some("singer"))
        .expect("artist list should succeed");

    assert!(response.stats.total_artists > 100);
    assert!(!response.rows.is_empty());
    assert!(response.rows.iter().all(|row| row.is_singer == 1));
}

#[test]
fn admin_lookup_list_returns_program_counts() {
    let core = open_core();
    let response = core
        .admin_lookup_list(LookupKind::Modes, "", 1)
        .expect("lookup list should succeed");

    assert!(response.stats.total_items > 0);
    assert!(!response.rows.is_empty());
    assert!(response.rows.iter().all(|row| row.usage_count >= 0));
}

#[test]
fn program_search_options_and_filters_work_with_and_logic() {
    let core = open_core();
    let options = core
        .program_search_options()
        .expect("search options should load");

    assert!(!options.categories.is_empty());
    assert!(!options.singers.is_empty());
    assert!(!options.instruments.is_empty());
    assert!(!options.modes.is_empty());

    let singer_id = options
        .singers
        .iter()
        .find(|item| item.name == "غلامحسین بنان")
        .map(|item| item.id)
        .expect("expected بنان to exist");
    let poet_id = options
        .poets
        .iter()
        .find(|item| item.name == "محیط قمی")
        .map(|item| item.id)
        .expect("expected محیط قمی to exist");
    let instrument_id = options
        .instruments
        .iter()
        .find(|item| item.name == "سنتور")
        .map(|item| item.id)
        .expect("expected سنتور to exist");

    let filters = ProgramSearchFilters {
        transcript_query: Some("چشم بگشا".to_string()),
        singer_ids: vec![singer_id],
        poet_ids: vec![poet_id],
        instrument_ids: vec![instrument_id],
        ..ProgramSearchFilters::default()
    };

    let response = core
        .admin_program_search(&filters, 1)
        .expect("program search should succeed");

    assert_eq!(response.rows.first().map(|row| row.id), Some(1));
}
