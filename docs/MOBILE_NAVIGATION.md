# Mobile Navigation

This document defines the canonical Android navigation architecture for the `mobile/` app.

## Goals

- Use standard Android back stack behavior
- Avoid manual overlay-based navigation state
- Preserve bottom-tab state whenever possible
- Keep feature screens push-based and pop-based
- Make future additions like program detail and search filters predictable

## Current Entry Points

- Android activity: [MainActivity.kt](/Users/espitman/Documents/Projects/radioGolha/mobile/composeApp/src/androidMain/kotlin/com/radiogolha/mobile/MainActivity.kt)
- Android navigation host: [AndroidApp.kt](/Users/espitman/Documents/Projects/radioGolha/mobile/composeApp/src/androidMain/kotlin/com/radiogolha/mobile/AndroidApp.kt)
- Shared iOS/common app shell: [App.kt](/Users/espitman/Documents/Projects/radioGolha/mobile/composeApp/src/commonMain/kotlin/com/radiogolha/mobile/App.kt)

## Rule

Android must use `NavHost` and `NavController` as the source of truth for navigation.

Do not introduce:
- ad-hoc overlay enums
- mutable manual route stacks for Android UI flow
- fake modal navigation when a normal push screen is intended

## Route Model

Current Android routes:

- `home`
- `search`
- `library`
- `account`
- `singers`
- `musicians`

Interpretation:

- `home`, `search`, `library`, `account` are root destinations for the bottom navigation
- `singers` and `musicians` are pushed feature screens on top of the current root flow

## Bottom Navigation Behavior

Bottom-tab navigation must use this pattern:

1. navigate to the root route for the selected tab
2. `popUpTo(findStartDestination())`
3. `saveState = true`
4. `launchSingleTop = true`
5. `restoreState = true`

That gives us the standard Android behavior for root tabs:

- no duplicate tab destinations
- previously visited tab state can be restored
- back stack remains predictable

## Back Behavior

Back behavior rules:

- On `singers` or `musicians`, system back must `popBackStack()`
- On a root tab destination, system back follows normal Android task behavior
- Feature screens should not manually emulate back if `NavController` can do it

## Screen Ownership

- `HomeScreen` is only for the `home` route
- `Search` and `Library` must not reuse `HomeScreen` as fake stand-ins
- Each root tab should own its own UI state and future feature surface

## State Rules

UI state should be owned by the destination that renders it.

Examples:

- `home` owns home feed loading state
- `singers` owns singers list loading state
- `musicians` owns musicians list loading state

If a screen grows more complex later, move that destination state into a dedicated state holder or view-model-like abstraction, but keep ownership per destination.

## Data Reloading

`reloadToken` currently forces reloading after debug DB import.

Rule:

- database import may invalidate destination data
- the navigation graph should remain intact
- reloading data must not rebuild navigation as a side effect

## What To Do Next

When we add more screens, follow this order:

1. Add a typed route in `AndroidApp.kt`
2. Add a `composable(...)` destination
3. Push to it with `navController.navigate(...)`
4. Return with `navController.popBackStack()`
5. Keep bottom tabs only for root destinations

## Anti-Patterns

Avoid these:

- using one screen for multiple tabs just to fill space
- keeping navigation state in multiple places
- pushing root tabs onto feature stacks
- mixing modal behavior and push behavior without a real reason

## Verification Checklist

After any navigation change on mobile:

1. `./gradlew :composeApp:assembleDebug`
2. `adb uninstall com.radiogolha.mobile`
3. `adb install -r -t .../composeApp-debug.apk`
4. launch app
5. open a pushed screen like `singers`
6. press Android back
7. verify return to previous destination
8. switch tabs and verify selected tab and screen content stay aligned
