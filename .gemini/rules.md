# Radio Golha Project Specific Rules

## 1. Git Management
- **ABSOLUTE RESTRICTION:** The AI (Antigravity) is NOT permitted to execute `git commit` or `git push` or make any changes to the Git repository without **explicit, direct approval** from the user for each instance.

## 2. Technical Stack
- **Frontend:** React + TanStack Router (File-based) + Tailwind 4 + shadcn/ui.
- **Package Manager:** Use ONLY `pnpm` (`pnpx`).
- **Styling:** Full RTL support and **Vazirmatn** font across all components.
- **Dev Port:** The web panel must always run on port **3336**.

## 3. Data and Security
- All audio files must be streamed from the dedicated CDN.
- Local SQLite database must be configured with Full-Text Search (FTS) capability.
- Data from `golha.co.uk` should be normalized during extraction.
