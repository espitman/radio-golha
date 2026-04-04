# 🏛️ Golha Programme Digital Archive: Master Technical Specification v5.3

This document serves as the "Technical DNA" for the Golha Digital Archive Management Panel, intended for full contextual awareness by AI engines (Codex/LLM) and future developers.

---

## 1. 📂 Project Folder Structure
- **`admin/`**: Main Frontend directory (React 18 + Vite + TailwindCSS v4).
- **`database/golha_database.db`**: Primary normalized SQLite database (Disk-based instance).
- **`scraper/`**: Python-based ingestion engine and data refinement scripts.
- **`docs/`**: Technical documentation and architectural guides (Current Folder).
- **`admin/src/api/`**: Contains the N-Tier logic: `/services/` (Orchestrators) and `/repositories/` (SQL DAL).

---

## 2. 🏗️ Software Architecture (N-Tier Standard)
The project is built on a clean separation of concerns:
- **Middleware (Vite Configuration)**: Acts as a lightweight API Gateway in `vite.config.ts`. It routes `/api/*` calls directly to the Service layer using the `sqlite-api` custom plugin.
- **Service Layer (`src/api/services/ProgramService.ts`)**: Orchestrates data flow, manages high-level business logic, and ensures database connection cleanup (`close()`).
- **Repository Layer (`src/api/repositories/ProgramRepository.ts`)**: The dedicated Data Access Layer (DAL). It handles complex SQL joins, recursive fetches for timeline segments, and strict role-based data retrieval.
- **Frontend Layer**: React 18 with **TanStack Router** for file-based routing and **Lucide Icons** for UI visual system.

---

## 3. 💿 Database Schema (Deep Forensic Mapping)
The database is fully normalized to handle deep recursive metadata for classical Persian music.

### A. Core Entities:
- **`program`**: ID, Title, No (Catalogue Number), URL (Audio source).
- **`category`**: Classification (e.g., Gole-haye Javidan, Rangarang).
- **`artist`**: Universal artist repository (Linked via `artist_id`).
- **`instrument`**: Musical instrument taxonomy.
- **`mode`**: Dastgah/Avaz (Radif-based classification).

### B. Global Metadata Junctions (Overall Programme):
These tables define the primary artistic identity of an entire archive entry:
- `program_singers`, `program_poets`, `program_performers`, `program_composers`, `program_arrangers`, `program_announcers`, `program_orchestras`.

### C. Interactive Timeline Engine (Granular Segments):
Timeline data is highly granular, specifying artists active at specific timestamps:
- **`program_timeline`**: Primary segment mapping (`start_time`, `end_time`, `mode_id`).
- **Segment Junctions**:
  - `program_timeline_singers` -> Links a segment to a specific singer.
  - `program_timeline_performers` -> Links a segment to a musician (Linked to `program_performers` for instrument data).
  - `program_timeline_poets` -> Links a segment to a poet for specific verses.
  - `program_timeline_announcers` -> Segment-specific announcer credits.

---

## 4. 🎨 Design & UX Standards
- **Direction**: Global **RTL (Right-to-Left)** support for Persian typography.
- **Style**: "Studio Premium" aesthetic.
  - **Primary Color**: Deep Turquoise (`#0c242c`).
  - **Secondary Brand Color**: Slate/Teal variants with Gold highlights (`Secondary`).
  - **UI Language**: High-density typography using **Glassmorphism** for data cards.
- **RTL Layout**: Grid-based positioning where primary content (Timeline) resides in the right-most columns, and secondary navigation (Artists) is aligned left.

---

## 5. 🛠️ Critical Developer Notes (Code Logic & Fixes)
### ⚠️ Fixed Architecture Paradigms:
- **Poet/Performer Distinction**: In the Repository layer, artists must be fetched through their specific `poet` or `performer` role tables before joining the `artist` table. This prevents metadata "Role Bleeding" (e.g., showing a Poet as a Musician).
- **Segment Music Mapping**: Musician data in segments uses a `LEFT JOIN` on the overall `program_performers` table to safely map the instrument name to the performer ID for that specific programme context.
- **Resource Management**: Database connections are established once per request and MUST be closed using the `close()` method in the Repository to prevent SQLite locking in a live Vite environment.

