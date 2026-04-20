use anyhow::Result;
use clap::{Parser, Subcommand};
use radiogolha_core::RadioGolhaCore;
use serde_json::json;

#[derive(Debug, Parser)]
#[command(name = "radiogolha-macos-bridge-cli")]
struct Cli {
    #[command(subcommand)]
    command: Command,
}

#[derive(Debug, Subcommand)]
enum Command {
    HomeFeedJson {
        #[arg(long)]
        db: String,
    },
    TopTracksJson {
        #[arg(long)]
        db: String,
    },
}

fn main() -> Result<()> {
    let cli = Cli::parse();

    match cli.command {
        Command::HomeFeedJson { db } => {
            println!("{}", build_home_feed_json(&db));
        }
        Command::TopTracksJson { db } => {
            println!("{}", build_top_tracks_json(&db));
        }
    }

    Ok(())
}

fn build_top_tracks_json(db_path: &str) -> String {
    match RadioGolhaCore::open(db_path) {
        Ok(core) => {
            let top_tracks = core.random_vocal_track_summaries(10).unwrap_or_default();
            json!(top_tracks.iter().map(|t| json!({
                "id": t.id,
                "title": t.title,
                "artist": t.artist,
                "duration": t.duration.clone().unwrap_or_else(|| "00:00".to_string()),
                "audioUrl": t.audio_url
            })).collect::<Vec<_>>())
            .to_string()
        }
        Err(err) => json!({ "error": err.to_string() }).to_string(),
    }
}

fn build_home_feed_json(db_path: &str) -> String {
    match RadioGolhaCore::open(db_path) {
        Ok(core) => {
            let conn = core.connection();

            let categories = core.program_categories().unwrap_or_default();
            let cat_breakdown = core.category_breakdown().unwrap_or_default();

            let mut singers_stmt = match conn.prepare(
                "SELECT a.id, a.name, a.avatar, COUNT(DISTINCT ps.program_id) AS total
                 FROM program_singers ps
                 JOIN singer s ON s.id = ps.singer_id
                 JOIN artist a ON a.id = s.artist_id
                 GROUP BY s.id, a.id, a.name, a.avatar
                 ORDER BY
                   CASE
                     WHEN a.avatar IS NOT NULL AND TRIM(a.avatar) <> '' THEN 1
                     ELSE 0
                   END DESC,
                   total DESC,
                   a.name ASC
                 LIMIT 8",
            ) {
                Ok(stmt) => stmt,
                Err(err) => return json!({ "error": err.to_string() }).to_string(),
            };
            let singers: Vec<_> = singers_stmt
                .query_map([], |row| {
                    Ok(json!({
                        "id": row.get::<_, i64>(0)?,
                        "name": row.get::<_, String>(1)?,
                        "avatar": row.get::<_, Option<String>>(2)?,
                        "programCount": row.get::<_, i64>(3)?
                    }))
                })
                .map(|rows| rows.filter_map(|item| item.ok()).collect())
                .unwrap_or_default();

            let dastgahs = core.top_modes(10).unwrap_or_default();

            let mut musicians_stmt = match conn.prepare(
                "SELECT
                   a.id,
                   a.name,
                   (
                     SELECT i.name
                     FROM program_performers pp2
                     LEFT JOIN instrument i ON i.id = pp2.instrument_id
                     WHERE pp2.performer_id = p.id AND i.name IS NOT NULL AND TRIM(i.name) <> ''
                     GROUP BY i.id, i.name
                     ORDER BY COUNT(*) DESC, i.name ASC
                     LIMIT 1
                   ) AS instrument_name,
                   a.avatar,
                   COUNT(DISTINCT pp.program_id) AS total
                 FROM program_performers pp
                 JOIN performer p ON p.id = pp.performer_id
                 JOIN artist a ON a.id = p.artist_id
                 GROUP BY p.id, a.id, a.name, a.avatar
                 ORDER BY total DESC, a.name ASC
                 LIMIT 8",
            ) {
                Ok(stmt) => stmt,
                Err(err) => return json!({ "error": err.to_string() }).to_string(),
            };
            let musicians: Vec<_> = musicians_stmt
                .query_map([], |row| {
                    let instrument: Option<String> = row.get(2)?;
                    Ok(json!({
                        "id": row.get::<_, i64>(0)?,
                        "name": row.get::<_, String>(1)?,
                        "instrument": instrument
                            .filter(|value| !value.trim().is_empty())
                            .unwrap_or_else(|| "نوازنده".to_string()),
                        "avatar": row.get::<_, Option<String>>(3)?,
                        "programCount": row.get::<_, i64>(4)?
                    }))
                })
                .map(|rows| rows.filter_map(|item| item.ok()).collect())
                .unwrap_or_default();

            let top_tracks = core.random_vocal_track_summaries(10).unwrap_or_default();
            let duets_json = core.get_duet_pairs_raw().unwrap_or_else(|_| "[]".to_string());
            let duets: serde_json::Value = serde_json::from_str(&duets_json).unwrap_or(json!([]));

            json!({
                "programs": cat_breakdown.iter().map(|item| {
                    json!({ "title": item.name, "episodeCount": item.total })
                }).collect::<Vec<_>>(),
                "categories": categories.iter().map(|c| {
                    let count = cat_breakdown.iter()
                        .find(|item| item.name == c.title_fa)
                        .map(|item| item.total)
                        .unwrap_or(0);
                    json!({ "id": c.id, "title": c.title_fa, "episodeCount": count })
                }).collect::<Vec<_>>(),
                "singers": singers,
                "dastgahs": dastgahs.iter().map(|m| json!({ "name": m.name })).collect::<Vec<_>>(),
                "musicians": musicians,
                "topTracks": top_tracks.iter().map(|t| json!({
                    "id": t.id,
                    "title": t.title,
                    "artist": t.artist,
                    "duration": t.duration.clone().unwrap_or_else(|| "00:00".to_string()),
                    "audioUrl": t.audio_url
                })).collect::<Vec<_>>(),
                "duets": duets
            })
            .to_string()
        }
        Err(err) => json!({ "error": err.to_string() }).to_string(),
    }
}
