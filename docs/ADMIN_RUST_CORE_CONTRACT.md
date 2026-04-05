# Golha Admin Rust Core Contract

## Status

Active contract for the current Admin-to-Rust `napi-rs` bridge.

This document defines:

- which HTTP endpoints the browser-facing Admin uses
- which `RustCoreClient` methods back those endpoints
- which native addon functions are invoked through `napi-rs`
- what JSON payload shapes are expected back
- what responsibilities belong to the Admin adapter vs the Rust core

## Purpose

The Admin Panel no longer reads SQLite directly.

Instead, it uses:

- Vite middleware for `/api/*`
- thin TypeScript service functions
- a Node-to-Rust bridge in `admin/src/api/rust/runCoreQuery.ts`
- the native Node addon crate in `core/adapters/node/`
- the shared Rust domain/query crate in `core/`

The Rust core is now the single source of truth for archive querying and response shaping.

## Bridge Rule

The browser-facing Admin may only consume JSON returned by the local `/api/*` adapter.

The Admin adapter must not:

- execute SQL
- reshape raw rows into domain objects
- reimplement archive rules

The Admin adapter may:

- parse HTTP query parameters
- call `RustCoreClient`
- map addon/runtime errors to HTTP errors
- return Rust JSON as-is

## Runtime Resolution

The Admin bridge resolves Rust by loading a native `.node` addon built from:

- `core/adapters/node/`

Database path passed to Rust:

- `database/golha_database.db`

Addon resolution:

1. `core/adapters/node/target/debug/radiogolha_core.node`
2. `core/adapters/node/target/release/radiogolha_core.node`

## Browser Adapter Note

The `/api/*` layer still exists and is still required.

This is not a separate backend server. It is a local browser adapter because:

- the React UI runs in the browser
- the browser cannot call the Node native addon directly
- only the Node/Vite side can load the `napi-rs` addon

So the real flow is:

```text
Browser UI -> /api/* -> Vite middleware -> RustCoreClient -> napi-rs addon -> Rust core
```

## Supported Admin Endpoints and Native Calls

### 1. Dashboard Overview

- HTTP: `/api/dashboard`
- TypeScript: `rustCoreClient.getDashboardOverview()`
- Native addon: `dashboardOverview(dbPath)`

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

## 2. Programs List

- HTTP: `/api/programs`
- TypeScript: `rustCoreClient.listPrograms({ search, page, categoryId, singerId })`
- Native addon: `listPrograms(dbPath, search, page, categoryId?, singerId?)`

#### Query arguments

- `--search <string>`
- `--page <number>`
- `--category-id <number>` optional
- `--singer-id <number>` optional

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

## 3. Program Detail

- HTTP: `/api/program/:id`
- TypeScript: `rustCoreClient.getProgramDetail(id)`
- Native addon: `getProgramDetail(dbPath, id)`

#### Path arguments

- positional `id`

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
  ],
  "transcript": [
    {
      "segment_order": 1,
      "verse_order": 1,
      "text": "چشم بگشا که جلوه دلدار..."
    },
    {
      "segment_order": 1,
      "verse_order": 2,
      "text": "این تماشا چو بنگری..."
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

### Transcript note

The top-level `ProgramDetail` payload now also includes:

- `transcript`

This field is an ordered array of verse rows and intentionally keeps the database naming:

- `segment_order`
- `verse_order`
- `text`

The Admin UI uses this payload to render the transcript section below the metadata and timeline panels.

## 4. Artists List

- HTTP: `/api/artists`
- TypeScript: `rustCoreClient.listArtists({ search, page, role })`
- Native addon: `listArtists(dbPath, search, page, role?)`

#### Query arguments

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

## 5. Program Search Options

- HTTP: `/api/program-search/options`
- TypeScript: `rustCoreClient.getProgramSearchOptions()`
- Native addon: `getProgramSearchOptions(dbPath)`

#### Output shape

```json
{
  "categories": [
    { "id": 1, "title_fa": "برگ سبز" }
  ],
  "singers": [
    { "id": 1, "name": "غلامحسین بنان" }
  ],
  "poets": [
    { "id": 3, "name": "محیط قمی" }
  ],
  "announcers": [
    { "id": 1, "name": "بهرام سلطانی" }
  ],
  "composers": [
    { "id": 1, "name": "حسین یاحقی" }
  ],
  "arrangers": [
    { "id": 1, "name": "جواد معروفی" }
  ],
  "performers": [
    { "id": 1, "name": "فرهنگ شریف" }
  ],
  "orchestraLeaders": [
    { "id": 1, "name": "روح‌الله خالقی" }
  ],
  "modes": [
    { "id": 1, "name": "سه گاه" }
  ],
  "orchestras": [
    { "id": 1, "name": "ارکستر گل‌ها" }
  ],
  "instruments": [
    { "id": 1, "name": "سنتور" }
  ]
}
```

## 6. Program Search

- HTTP: `/api/program-search`
- TypeScript: `rustCoreClient.searchPrograms({...})`
- Runtime implementation: `RustCoreClient` currently executes the Rust CLI subcommand `admin-program-search`

#### Search rule

- groups are combined with `AND`
- category IDs are always matched with `OR`
- transcript query is full-text against transcript verses only
- the following groups support per-group `any` / `all` matching:
  - modes
  - orchestras
  - instruments
  - singers
  - poets
  - announcers
  - composers
  - arrangers
  - performers
  - orchestra leaders

#### Query arguments

- `page`
- `transcriptQuery`
- `categoryIds`
- `modeIds`, `modeMatch`
- `orchestraIds`, `orchestraMatch`
- `instrumentIds`, `instrumentMatch`
- `singerIds`, `singerMatch`
- `poetIds`, `poetMatch`
- `announcerIds`, `announcerMatch`
- `composerIds`, `composerMatch`
- `arrangerIds`, `arrangerMatch`
- `performerIds`, `performerMatch`
- `orchestraLeaderIds`, `orchestraLeaderMatch`

#### Output shape

```json
{
  "rows": [
    {
      "id": 1,
      "title": "برگ سبز ۸۳",
      "category_name": "برگ سبز",
      "no": 83,
      "sub_no": null
    }
  ],
  "total": 1,
  "page": 1,
  "totalPages": 1
}
```

## 7. Lookup Lists

- HTTP: `/api/orchestras`, `/api/instruments`, `/api/modes`
- TypeScript: `rustCoreClient.listLookupItems(kind, { search, page })`
- Native addon: `listLookupItems(dbPath, kind, search, page)`

#### Query arguments

- `kind` positional at the adapter level
- `search <string>`
- `page <number>`

Allowed `kind` values:

- `orchestras`
- `instruments`
- `modes`

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

| HTTP endpoint | TypeScript bridge | Native addon |
| --- | --- | --- |
| `/api/dashboard` | `rustCoreClient.getDashboardOverview()` | `dashboardOverview(dbPath)` |
| `/api/programs` | `rustCoreClient.listPrograms(...)` | `listPrograms(dbPath, ...)` |
| `/api/program/:id` | `rustCoreClient.getProgramDetail(id)` | `getProgramDetail(dbPath, id)` |
| `/api/artists` | `rustCoreClient.listArtists(...)` | `listArtists(dbPath, ...)` |
| `/api/program-search/options` | `rustCoreClient.getProgramSearchOptions()` | `getProgramSearchOptions(dbPath)` |
| `/api/program-search` | `rustCoreClient.searchPrograms(...)` | `admin-program-search` CLI fallback |
| `/api/orchestras` | `rustCoreClient.listLookupItems('orchestras', ...)` | `listLookupItems(dbPath, kind, ...)` |
| `/api/instruments` | `rustCoreClient.listLookupItems('instruments', ...)` | `listLookupItems(dbPath, kind, ...)` |
| `/api/modes` | `rustCoreClient.listLookupItems('modes', ...)` | `listLookupItems(dbPath, kind, ...)` |

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

1. run `pnpm build:core-addon` in `admin/`
2. load the addon through `RustCoreClient` or run `pnpm test:core-addon`
3. compare the returned JSON with the expected shape in this document
4. only then inspect the React route

This keeps debugging centered on the real source of truth.
