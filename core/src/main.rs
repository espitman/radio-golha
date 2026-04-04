use anyhow::Result;
use clap::{Parser, Subcommand};
use radiogolha_core::RadioGolhaCore;

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
    Programs {
        #[arg(long, default_value_t = 10)]
        limit: usize,
        #[arg(long, default_value_t = 0)]
        offset: usize,
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
        Command::ProgramDetail { id } => {
            println!("{:#?}", core.get_program_detail(id)?);
        }
    }

    Ok(())
}
