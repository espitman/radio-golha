# Android TV Focus Rules

This document tracks the directional focus contract for the Android TV client.

## Current Rules

- Focus must move between the right sidebar and the main content area only with left/right directional input.
- Pressing up on the first focusable item in a focus area must not move focus out of that area.
- Pressing down on the last focusable item in a focus area must not move focus out of that area.
- The sidebar keeps vertical focus navigation inside the sidebar.
- The main content area keeps vertical focus navigation inside the main content area.

## Implementation Notes

- Use Compose `FocusRequester` and `focusProperties` for explicit focus boundaries.
- Use `FocusRequester.Cancel` for blocked directions at the start/end of a focus area.
- Avoid global key interception unless a screen has a custom focus graph that cannot be expressed with `focusProperties`.
- Keep physical directional behavior clear: the sidebar is on the right side of the TV shell, and the main content area is on the left.
