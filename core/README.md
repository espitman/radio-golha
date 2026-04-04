# RadioGolha Core

Rust core library for shared, read-only archive logic across native clients.

## Scope

- Open the canonical SQLite archive
- Expose stable query/use-case APIs
- Keep domain rules out of platform UI code
- Provide a small CLI for local smoke testing on macOS

## Initial surface

- `dashboard_summary`
- `category_breakdown`
- `top_singers`
- `top_modes`
- `list_programs`
- `get_program_detail`

## Local development

```bash
cd core
cargo run -- dashboard
cargo run -- programs --limit 5
cargo run -- program-detail 1251
```

Default database path:

```text
../database/golha_database.db
```
