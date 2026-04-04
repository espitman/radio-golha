use anyhow::Result;
use clap::{Parser, Subcommand};
use radiogolha_core::RadioGolhaCore;
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
    ProgramDetail {
        id: i64,
    },
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
        Command::ProgramDetail { id } => {
            println!("{:#?}", core.get_program_detail(id)?);
        }
    }

    Ok(())
}
