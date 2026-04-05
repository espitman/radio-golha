use anyhow::Result;
use clap::{Parser, Subcommand, ValueEnum};
use radiogolha_core::{
    LookupKind, ProgramSearchFilters, ProgramSortField, RadioGolhaCore, SearchMatchMode,
    SortDirection,
};
use serde_json::to_string;

#[derive(Debug, Parser)]
#[command(name = "radiogolha-core-cli")]
#[command(about = "Smoke-test CLI for the RadioGolha Rust core")]
struct Cli {
    #[arg(long, default_value = "../database/golha_database.db")]
    db: String,
    #[command(subcommand)]
    command: Command,
}

#[derive(Debug, Subcommand)]
enum Command {
    Dashboard,
    DashboardOverview,
    Programs {
        #[arg(long, default_value_t = 10)]
        limit: usize,
        #[arg(long, default_value_t = 0)]
        offset: usize,
    },
    BrowsePrograms {
        #[arg(long, default_value = "")]
        search: String,
        #[arg(long, default_value_t = 1)]
        page: i64,
        #[arg(long)]
        category_id: Option<i64>,
        #[arg(long)]
        singer_id: Option<i64>,
        #[arg(long, default_value = "no")]
        sort_field: String,
        #[arg(long, default_value = "asc")]
        sort_direction: String,
    },
    ProgramDetailJson {
        id: i64,
    },
    BrowseArtists {
        #[arg(long, default_value = "")]
        search: String,
        #[arg(long, default_value_t = 1)]
        page: i64,
        #[arg(long)]
        role: Option<String>,
    },
    BrowseLookup {
        #[arg(value_enum)]
        kind: LookupKindArg,
        #[arg(long, default_value = "")]
        search: String,
        #[arg(long, default_value_t = 1)]
        page: i64,
    },
    ProgramSearchOptions,
    SearchPrograms {
        #[arg(long)]
        transcript_query: Option<String>,
        #[arg(long, default_value_t = 1)]
        page: i64,
        #[arg(long, value_delimiter = ',')]
        category_ids: Vec<i64>,
        #[arg(long, value_delimiter = ',')]
        mode_ids: Vec<i64>,
        #[arg(long, value_enum, default_value_t = MatchModeArg::Any)]
        mode_match: MatchModeArg,
        #[arg(long, value_delimiter = ',')]
        orchestra_ids: Vec<i64>,
        #[arg(long, value_enum, default_value_t = MatchModeArg::Any)]
        orchestra_match: MatchModeArg,
        #[arg(long, value_delimiter = ',')]
        instrument_ids: Vec<i64>,
        #[arg(long, value_enum, default_value_t = MatchModeArg::Any)]
        instrument_match: MatchModeArg,
        #[arg(long, value_delimiter = ',')]
        singer_ids: Vec<i64>,
        #[arg(long, value_enum, default_value_t = MatchModeArg::Any)]
        singer_match: MatchModeArg,
        #[arg(long, value_delimiter = ',')]
        poet_ids: Vec<i64>,
        #[arg(long, value_enum, default_value_t = MatchModeArg::Any)]
        poet_match: MatchModeArg,
        #[arg(long, value_delimiter = ',')]
        announcer_ids: Vec<i64>,
        #[arg(long, value_enum, default_value_t = MatchModeArg::Any)]
        announcer_match: MatchModeArg,
        #[arg(long, value_delimiter = ',')]
        composer_ids: Vec<i64>,
        #[arg(long, value_enum, default_value_t = MatchModeArg::Any)]
        composer_match: MatchModeArg,
        #[arg(long, value_delimiter = ',')]
        arranger_ids: Vec<i64>,
        #[arg(long, value_enum, default_value_t = MatchModeArg::Any)]
        arranger_match: MatchModeArg,
        #[arg(long, value_delimiter = ',')]
        performer_ids: Vec<i64>,
        #[arg(long, value_enum, default_value_t = MatchModeArg::Any)]
        performer_match: MatchModeArg,
        #[arg(long, value_delimiter = ',')]
        orchestra_leader_ids: Vec<i64>,
        #[arg(long, value_enum, default_value_t = MatchModeArg::Any)]
        orchestra_leader_match: MatchModeArg,
        #[arg(long, default_value = "no")]
        sort_field: String,
        #[arg(long, default_value = "asc")]
        sort_direction: String,
    },
    ProgramDetail {
        id: i64,
    },
}

#[derive(Clone, Debug, ValueEnum)]
enum LookupKindArg {
    Orchestras,
    Instruments,
    Modes,
}

#[derive(Clone, Copy, Debug, ValueEnum)]
enum MatchModeArg {
    Any,
    All,
}

impl From<MatchModeArg> for SearchMatchMode {
    fn from(value: MatchModeArg) -> Self {
        match value {
            MatchModeArg::Any => SearchMatchMode::Any,
            MatchModeArg::All => SearchMatchMode::All,
        }
    }
}

impl From<LookupKindArg> for LookupKind {
    fn from(value: LookupKindArg) -> Self {
        match value {
            LookupKindArg::Orchestras => LookupKind::Orchestras,
            LookupKindArg::Instruments => LookupKind::Instruments,
            LookupKindArg::Modes => LookupKind::Modes,
        }
    }
}

fn main() -> Result<()> {
    let cli = Cli::parse();
    let core = RadioGolhaCore::open(&cli.db)?;

    match cli.command {
        Command::Dashboard => {
            let summary = core.dashboard_summary()?;
            println!("{summary:#?}");
            println!("{:#?}", core.category_breakdown()?);
            println!("{:#?}", core.top_singers(5)?);
            println!("{:#?}", core.top_modes(5)?);
        }
        Command::DashboardOverview => {
            println!("{}", to_string(&core.dashboard_overview()?)?);
        }
        Command::Programs { limit, offset } => {
            for item in core.list_programs(limit, offset)? {
                println!(
                    "#{} | {} {} | {} | {}",
                    item.id,
                    item.no,
                    item.sub_no.unwrap_or_default(),
                    item.category_name,
                    item.title
                );
            }
        }
        Command::BrowsePrograms {
            search,
            page,
            category_id,
            singer_id,
            sort_field,
            sort_direction,
        } => {
            println!(
                "{}",
                to_string(&core.browse_programs(
                    &search,
                    page,
                    category_id,
                    singer_id,
                    ProgramSortField::from_str(&sort_field),
                    SortDirection::from_str(&sort_direction),
                )?)?
            );
        }
        Command::ProgramDetailJson { id } => {
            println!("{}", to_string(&core.get_program_detail(id)?)?);
        }
        Command::BrowseArtists { search, page, role } => {
            println!(
                "{}",
                to_string(&core.browse_artists(&search, page, role.as_deref())?)?
            );
        }
        Command::BrowseLookup { kind, search, page } => {
            println!(
                "{}",
                to_string(&core.browse_lookup_items(kind.into(), &search, page)?)?
            );
        }
        Command::ProgramSearchOptions => {
            println!("{}", to_string(&core.program_search_options()?)?);
        }
        Command::SearchPrograms {
            transcript_query,
            page,
            category_ids,
            mode_ids,
            mode_match,
            orchestra_ids,
            orchestra_match,
            instrument_ids,
            instrument_match,
            singer_ids,
            singer_match,
            poet_ids,
            poet_match,
            announcer_ids,
            announcer_match,
            composer_ids,
            composer_match,
            arranger_ids,
            arranger_match,
            performer_ids,
            performer_match,
            orchestra_leader_ids,
            orchestra_leader_match,
            sort_field,
            sort_direction,
        } => {
            let filters = ProgramSearchFilters {
                transcript_query,
                category_ids,
                mode_ids,
                mode_match: mode_match.into(),
                orchestra_ids,
                orchestra_match: orchestra_match.into(),
                instrument_ids,
                instrument_match: instrument_match.into(),
                singer_ids,
                singer_match: singer_match.into(),
                poet_ids,
                poet_match: poet_match.into(),
                announcer_ids,
                announcer_match: announcer_match.into(),
                composer_ids,
                composer_match: composer_match.into(),
                arranger_ids,
                arranger_match: arranger_match.into(),
                performer_ids,
                performer_match: performer_match.into(),
                orchestra_leader_ids,
                orchestra_leader_match: orchestra_leader_match.into(),
                sort_field: ProgramSortField::from_str(&sort_field),
                sort_direction: SortDirection::from_str(&sort_direction),
            };
            println!("{}", to_string(&core.search_programs(&filters, page)?)?);
        }
        Command::ProgramDetail { id } => {
            println!("{:#?}", core.get_program_detail(id)?);
        }
    }

    Ok(())
}
