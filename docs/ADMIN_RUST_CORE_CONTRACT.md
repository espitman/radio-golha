# Golha Admin Rust Core Contract

## Status

Active contract for the current Admin-to-Rust bridge.

This document defines:

- which Rust commands the Admin may call
- what arguments are passed
- what JSON payload shapes are expected back
- what responsibilities belong to the Admin adapter vs the Rust core

## Purpose

The Admin Panel no longer reads SQLite directly.

Instead, it uses:

- Vite middleware for `/api/*`
- thin TypeScript service functions
- a Node-to-Rust bridge in `admin/src/api/rust/runCoreQuery.ts`
- the Rust CLI in `core/src/main.rs`

The Rust core is now the single source of truth for archive querying and response shaping.

## Bridge Rule

The Admin may only consume JSON returned by Rust.

The Admin adapter must not:

- execute SQL
- reshape raw rows into domain objects
- reimplement archive rules

The Admin adapter may:

- parse HTTP query parameters
- forward them to Rust
- map process errors to HTTP errors
- return Rust JSON as-is

## Runtime Resolution

The Admin bridge resolves Rust in this order:

1. compiled binary at `core/target/debug/radiogolha-core-cli`
2. fallback to `cargo run --quiet --bin radiogolha-core-cli -- ...`

Database path passed to Rust:

- `database/golha_database.db`

## Supported Admin Commands

### 1. `admin-dashboard`

#### CLI

```bash
cd /Users/espitman/Documents/Projects/radioGolha/core
cargo run --quiet -- admin-dashboard
```

#### Used by

- `/api/dashboard`

#### Output shape

```json
{
  "summary": {
    "totalPrograms": 1440,
    "totalArtists": 671,
    "totalSegments": 28577,
    "totalModes": 17,
    "programsWithAudio": 1440,
    "programsWithTimeline": 1395,
    "totalCategories": 6,
    "totalOrchestras": 2,
    "totalInstruments": 16
  },
  "categoryBreakdown": [
    { "name": "گلهای رنگارنگ", "total": 492 }
  ],
  "topSingers": [
    { "name": "حسین قوامی (فاخته‌ای)", "total": 252 }
  ],
  "topModes": [
    { "name": "سه گاه", "total": 276 }
  ],
  "topOrchestras": [
    { "name": "ارکستر گل‌ها", "total": 1377 }
  ],
  "recentPrograms": [
    {
      "id": 1440,
      "title": "...",
      "category_name": "گلهای رنگارنگ",
      "no": 247,
      "sub_no": null
    }
  ]
}
```

## 2. `admin-programs`

#### CLI

```bash
cd /Users/espitman/Documents/Projects/radioGolha/core
cargo run --quiet -- admin-programs --page 1
```

#### Arguments

- `--search <string>`
- `--page <number>`
- `--category-id <number>` optional
- `--singer-id <number>` optional

#### Used by

- `/api/programs`

#### Output shape

```json
{
  "rows": [
    {
      "id": 18,
      "title": "برگ سبز ۱",
      "category_name": "برگ سبز",
      "no": 1,
      "sub_no": null
    }
  ],
  "categories": [
    { "id": 1, "title_fa": "گلهای جاویدان" }
  ],
  "singers": [
    { "id": 1, "name": "الهه" }
  ],
  "total": 1440,
  "page": 1,
  "totalPages": 60,
  "activeCategoryId": null,
  "activeSingerId": null
}
```

## 3. `admin-program-detail`

#### CLI

```bash
cd /Users/espitman/Documents/Projects/radioGolha/core
cargo run --quiet -- admin-program-detail 1251
```

#### Arguments

- positional `id`

#### Used by

- `/api/program/:id`

#### Output shape

Returns either:

- a full `ProgramDetail` object
- or `null` when not found

#### Key fields

```json
{
  "id": 1251,
  "title": "گلهای رنگارنگ ۲۴۷",
  "category_name": "گلهای رنگارنگ",
  "no": 247,
  "sub_no": null,
  "audio_url": "https://...",
  "singers": ["الهه"],
  "poets": ["معینی کرمانشاهی"],
  "announcers": ["بهرام سلطانی"],
  "composers": ["حسین یاحقی"],
  "arrangers": ["جواد معروفی"],
  "modes": ["افشاری"],
  "orchestras": ["ارکستر گل‌ها"],
  "orchestra_leaders": [
    { "orchestra": "ارکستر گل‌ها", "name": "جواد معروفی" }
  ],
  "performers": [
    { "name": "فرهنگ شریف", "instrument": "تار" }
  ],
  "timeline": [
    {
      "id": 1,
      "start_time": "00:00",
      "end_time": "02:10",
      "mode_name": "افشاری",
      "singers": [],
      "poets": [],
      "announcers": [],
      "orchestras": [],
      "orchestraLeaders": [],
      "performers": []
    }
  ]
}
```

### Naming note

The top-level field remains:

- `orchestra_leaders`

Inside `timeline`, the field is:

- `orchestraLeaders`

This asymmetry is intentional because the current Admin UI already depends on these names.

## 4. `admin-artists`

#### CLI

```bash
cd /Users/espitman/Documents/Projects/radioGolha/core
cargo run --quiet -- admin-artists --page 1
```

#### Arguments

- `--search <string>`
- `--page <number>`
- `--role <string>` optional

Allowed role values:

- `singer`
- `performer`
- `poet`
- `announcer`
- `composer`
- `arranger`

#### Used by

- `/api/artists`

#### Output shape

```json
{
  "rows": [
    {
      "id": 303,
      "name": "الهه",
      "is_singer": 1,
      "is_performer": 0,
      "is_poet": 0,
      "is_announcer": 0,
      "is_composer": 0,
      "is_arranger": 0
    }
  ],
  "stats": {
    "total_artists": 671,
    "singers": 99,
    "performers": 170,
    "poets": 131
  },
  "total": 99,
  "page": 1,
  "totalPages": 5,
  "activeRole": "singer"
}
```

## 5. `admin-lookup`

#### CLI

```bash
cd /Users/espitman/Documents/Projects/radioGolha/core
cargo run --quiet -- admin-lookup modes --page 1
```

#### Arguments

- positional `kind`
- `--search <string>`
- `--page <number>`

Allowed `kind` values:

- `orchestras`
- `instruments`
- `modes`

#### Used by

- `/api/orchestras`
- `/api/instruments`
- `/api/modes`

#### Output shape

```json
{
  "rows": [
    {
      "id": 4,
      "name": "ابوعطا",
      "usage_count": 127
    }
  ],
  "stats": {
    "total_items": 17,
    "total_usage": 1840
  },
  "total": 17,
  "page": 1,
  "totalPages": 1
}
```

## HTTP Mapping

Current mapping in the Admin:

| HTTP endpoint | Rust command |
| --- | --- |
| `/api/dashboard` | `admin-dashboard` |
| `/api/programs` | `admin-programs` |
| `/api/program/:id` | `admin-program-detail` |
| `/api/artists` | `admin-artists` |
| `/api/orchestras` | `admin-lookup orchestras` |
| `/api/instruments` | `admin-lookup instruments` |
| `/api/modes` | `admin-lookup modes` |

## Error Contract

At the Admin HTTP layer:

- successful Rust JSON returns HTTP `200`
- `null` from `admin-program-detail` returns HTTP `404`
- process failures or parse failures return HTTP `500`

The Admin currently does not expose a richer structured error schema.

## Compatibility Rule

Any Rust-side change that alters:

- field names
- nullability
- pagination fields
- nested object names

must be treated as a contract change and must be validated against the Admin UI before merging.

## Recommended Debug Workflow

When an Admin page looks wrong:

1. run the Rust command directly in `core/`
2. inspect the JSON payload
3. compare it with the expected shape in this document
4. only then inspect the React route

This keeps debugging centered on the real source of truth.
