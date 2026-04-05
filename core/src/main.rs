use anyhow::Result;
use clap::{Parser, Subcommand, ValueEnum};
use radiogolha_core::{LookupKind, ProgramSearchFilters, RadioGolhaCore};
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
    AdminDashboard,
    Programs {
        #[arg(long, default_value_t = 10)]
        limit: usize,
        #[arg(long, default_value_t = 0)]
        offset: usize,
    },
    AdminPrograms {
        #[arg(long, default_value = "")]
        search: String,
        #[arg(long, default_value_t = 1)]
        page: i64,
        #[arg(long)]
        category_id: Option<i64>,
        #[arg(long)]
        singer_id: Option<i64>,
    },
    AdminProgramDetail {
        id: i64,
    },
    AdminArtists {
        #[arg(long, default_value = "")]
        search: String,
        #[arg(long, default_value_t = 1)]
        page: i64,
        #[arg(long)]
        role: Option<String>,
    },
    AdminLookup {
        #[arg(value_enum)]
        kind: LookupKindArg,
        #[arg(long, default_value = "")]
        search: String,
        #[arg(long, default_value_t = 1)]
        page: i64,
    },
    AdminProgramSearchOptions,
    AdminProgramSearch {
        #[arg(long)]
        transcript_query: Option<String>,
        #[arg(long, default_value_t = 1)]
        page: i64,
        #[arg(long, value_delimiter = ',')]
        category_ids: Vec<i64>,
        #[arg(long, value_delimiter = ',')]
        mode_ids: Vec<i64>,
        #[arg(long, value_delimiter = ',')]
        orchestra_ids: Vec<i64>,
        #[arg(long, value_delimiter = ',')]
        instrument_ids: Vec<i64>,
        #[arg(long, value_delimiter = ',')]
        singer_ids: Vec<i64>,
        #[arg(long, value_delimiter = ',')]
        poet_ids: Vec<i64>,
        #[arg(long, value_delimiter = ',')]
        announcer_ids: Vec<i64>,
        #[arg(long, value_delimiter = ',')]
        composer_ids: Vec<i64>,
        #[arg(long, value_delimiter = ',')]
        arranger_ids: Vec<i64>,
        #[arg(long, value_delimiter = ',')]
        performer_ids: Vec<i64>,
        #[arg(long, value_delimiter = ',')]
        orchestra_leader_ids: Vec<i64>,
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
        Command::AdminDashboard => {
            println!("{}", to_string(&core.admin_dashboard_overview()?)?);
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
        Command::AdminPrograms {
            search,
            page,
            category_id,
            singer_id,
        } => {
            println!(
                "{}",
                to_string(&core.admin_program_list(&search, page, category_id, singer_id)?)?
            );
        }
        Command::AdminProgramDetail { id } => {
            println!("{}", to_string(&core.get_program_detail(id)?)?);
        }
        Command::AdminArtists { search, page, role } => {
            println!(
                "{}",
                to_string(&core.admin_artist_list(&search, page, role.as_deref())?)?
            );
        }
        Command::AdminLookup { kind, search, page } => {
            println!(
                "{}",
                to_string(&core.admin_lookup_list(kind.into(), &search, page)?)?
            );
        }
        Command::AdminProgramSearchOptions => {
            println!("{}", to_string(&core.program_search_options()?)?);
        }
        Command::AdminProgramSearch {
            transcript_query,
            page,
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
        } => {
            let filters = ProgramSearchFilters {
                transcript_query,
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
            println!("{}", to_string(&core.admin_program_search(&filters, page)?)?);
        }
        Command::ProgramDetail { id } => {
            println!("{:#?}", core.get_program_detail(id)?);
        }
    }

    Ok(())
}
