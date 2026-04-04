use radiogolha_core::RadioGolhaCore;

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
