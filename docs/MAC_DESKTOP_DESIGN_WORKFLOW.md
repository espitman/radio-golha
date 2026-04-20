# Mac Desktop Design Workflow (Figma + Stitch)

## 1) Purpose
This document defines the UI implementation standard for the macOS app in `radioGolha` so that:
- SwiftUI output stays aligned with the design reference.
- Page direction (Left-to-Right) is applied in a controlled way.
- Font sizes are managed with a fixed, repeatable rule.

This guide applies to:
- `/Users/espitman/Documents/Projects/radioGolha/mac-desktop-app`

---

## 2) Design Sources
We use two design sources for implementation:

1. `Figma` (primary source)
- Component structure
- Spacing, dimensions, colors, typography

2. `Stitch` (fast operational output)
- Generated screens
- HTML/CSS output for quick extraction of spacing and hierarchy

### Decision rule
- If Figma and Stitch conflict, **Figma wins**.
- If Figma is temporarily unavailable (for example rate limit), use Stitch as fallback and re-check with Figma later.

---

## 3) Recommended Generic Source Structure
To keep the project ready for future features (not only Home):

```text
Sources/RadioGolhaDesktop/
  App/
    AppMain.swift
  Core/
    Theme/
      Palette.swift
  Shared/
    Components/
      FigmaAssetImage.swift
  Features/
    Home/
      Views/
      Sections/
      Components/
      Models/
```

### Why this structure
- `Core`: app-wide foundation (theme/design tokens)
- `Shared`: reusable cross-feature components
- `Features/*`: feature-first modules, isolated per screen/domain

---

## 4) Standard UI Implementation Flow

## Step 1: Pick the reference
- Identify the target node/screen in Figma or Stitch.
- If using Stitch, download and inspect the HTML for that exact screen.

## Step 2: Extract design tokens
- Primary/secondary/surface/border colors
- Radius values
- Spacing scale (for example 8/12/16/24/32/48)
- Typography scale

Store tokens in:
- `/Users/espitman/Documents/Projects/radioGolha/mac-desktop-app/Sources/RadioGolhaDesktop/Core/Theme/Palette.swift`

## Step 3: Implement page structure
- Page container in `Features/<Feature>/Views`
- Major page blocks in `Sections`
- Repeated UI pieces in `Components`

## Step 4: Wire data
- UI models in `Models`
- For prototype stage, keep `MockData` in the same feature

## Step 5: Visual QA
- Verify spacing
- Verify alignments
- Verify typography hierarchy
- Verify item order in header/sidebar/cards/lists

---

## 5) Page Direction Rule (LTR)
In the current desktop app, the **main page direction** should be **Left-to-Right**.

That means:
- Column order (for example `main content` and `sidebar`) must be controlled by LTR.
- Do not let root-level RTL accidentally swap sidebar position.

### Practical rule
1. Keep the page root container (`HStack` at root layout) LTR.
2. If a specific component needs a different direction, override only that subtree.
3. Avoid conflicting direction overrides at the root level.

### Architecture guideline
- Root LTR to preserve structural column placement.
- Text alignment should be controlled explicitly in each component (`leading`/`trailing`).

---

## 6) Font Size Rule (Global Font Scale)
Project rule:

`FontSizeFinal = FontSizeDesign * 0.75`

### Important note
- This scale applies to **text**.
- Icon sizes (commonly `.system(size:)` for SF Symbols) should only change when the design explicitly requires it.

### Examples
- Design title `30` -> code `22.5`
- Design body `14` -> code `10.5`

### Number format
- Decimal values are allowed (`10.5`, `22.5`).
- If needed, the team can add a rounding rule later (for example nearest `0.5`).

---

## 7) Pre-Handoff Checklist
- [ ] Feature-first structure is respected
- [ ] Root page direction is LTR
- [ ] Text alignment is explicit (not default-dependent)
- [ ] All text font sizes follow the `0.75` scale
- [ ] Naming is generic (no feature-specific hardcoded naming in app root)
- [ ] `swift build` passes with no errors

---

## 8) Run the App
Standard run script:
- `/Users/espitman/Documents/Projects/radioGolha/scripts/mac_desktop_run.sh`

Direct run:
```bash
cd /Users/espitman/Documents/Projects/radioGolha/mac-desktop-app
swift run RadioGolhaDesktop
```

---

## 9) Common Issues and Fixes

## A) Build fails after rename with `SwiftShims` / `ModuleCache`
Cause:
- Old build cache still points to previous path.

Fix:
```bash
cd /Users/espitman/Documents/Projects/radioGolha/mac-desktop-app
rm -rf .build
swift build
```

## B) Sidebar moves to wrong side unexpectedly
Cause:
- Applying RTL at root container level.

Fix:
- Keep root LTR.
- Override only the subtree that needs direction changes.

## C) Visual mismatch with design
Fix:
- First sync spacing and typography with reference.
- Then fix element ordering (icon/text/action).

## D) App does not appear in Dock / App Switcher when run via Bash
Common causes:
- Running with `swift run` attached to terminal process behavior
- Missing proper activation policy for macOS app lifecycle

Project-standard fix:
1. In `AppMain` at launch:
   - `NSApp.setActivationPolicy(.regular)`
   - `NSApp.activate(ignoringOtherApps: true)`
2. In script, run detached built binary (not `swift run`)

Reference script:
- `/Users/espitman/Documents/Projects/radioGolha/scripts/mac_desktop_run.sh`

---

## 10) Policy for Future Features
When adding a new feature:
1. Create `Features/<FeatureName>/Views|Sections|Components|Models`
2. Reuse `Core/Theme` and `Shared/Components`
3. Follow this document rules as-is (LTR + Font Scale + QA checklist)
