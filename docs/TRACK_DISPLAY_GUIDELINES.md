# Track Display Guidelines (RadioGolha)

This document defines the unified "Single Source of Truth" for displaying track rows across the entire RadioGolha application, ensuring a premium and consistent user experience.

## 1. The Canonical UI Component
All track lists (Home Page Top Tracks, Category Programs, Artist Details, Search results, etc.) **MUST** use the `ProgramTrackRow` component defined in:
`mobile/composeApp/src/commonMain/kotlin/com/radiogolha/mobile/ui/programs/ProgramComponents.kt`

## 2. Visual Rules
- **Layout Direction**: Always forced to **LTR** (Left-to-Right) regardless of the screen's main direction.
- **Left Box (Placeholder)**:
    - Displays the **first letter** of the track title.
    - Size: 58x58 dp.
    - Style: Rounded corners (16dp) with a subtle border.
- **Text Hierarchy**:
    - **Header (Title)**: Precise Track Title (e.g., "گلهای رنگارنگ ۵۶۹"). Font: `SemiBold`, `bodyLarge`.
    - **Sub-header (Artist)**: Singer names ONLY. Font: `Normal`, `bodySmall`.
    - **DO NOT** display "Dastgah" or other metadata in the main list row unless explicitly requested.
- **Right Action**:
    - Play/Pause circular button (36x36 dp).
    - Duration label (optional) provided to the left of the play button.

## 3. Data Loading Rules
- **Title Construction**:
    - Data loaders should prioritize the `title` or `title_fa` field from the JNI payload.
    - Manual construction (e.g., "$Category $No") is only a fallback and should be avoided if possible.
    - Avoid generic prefixes like "برنامه" unless it's part of the official title.
- **Conversion**: Use the `.toTrackUiModel()` extension on `CategoryProgramUiModel` to ensure consistent mapping of titles and artists before passing data to the UI.

## 4. Maintenance
Any visual tweaks to track rows **MUST** be applied to `ProgramComponents.kt` only. Local overrides in screen-specific files are strictly forbidden to prevent UI regressions.
