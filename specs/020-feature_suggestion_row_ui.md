# Feature 020 — Suggestion Row UI

## Summary

Add the persistent, accessible suggestion row above the existing keyboard keys. In this increment it is a UI shell: candidate slots are blank and do not generate or insert text. The row establishes independent text sizing and horizontal pan behavior for later suggestion features.

## User-visible behavior

- Render three equal candidate slots above the regular key rows whenever the keyboard is visible.
- Render blank slots when there are no candidates; do not collapse the row. If predictive text is disabled but autocorrect is enabled, the row may still display the one-shot original-spelling undo candidate after an automatic correction.
- Provide a UI/state contract for up to three candidate labels and click callbacks, without connecting it to an engine in this feature.
- Give the row a separately controlled text size from Feature 018; do not scale candidate text with keyboard zoom.
- If content does not fit, allow horizontal panning within the candidate row, independent of keyboard panning/zoom.
- Use existing keyboard theme colors and clear, high-contrast separators/selection affordances consistent with SeeMoreBoard.

## Layout and accessibility

- Reserve row height within the configured keyboard height; do not unexpectedly increase the IME window height.
- Keep the candidate row above, and visually distinct from, the key layout.
- Avoid gesture competition: horizontal row panning must not trigger keyboard pan or key presses; each candidate remains a predictable touch target.
- Expose each populated candidate as a separately focusable accessibility element with its candidate text and action. Empty slots should not create noisy focus stops.
- Do not announce continuously changing candidate contents automatically. Candidate focus and activation should provide accessible feedback.
- Preserve row usability at maximum supported suggestion text size, magnification, landscape, extract mode, and every theme.

## Out of scope

- Prediction, candidate ranking, autocorrection, editor-context reads, or text insertion.
- Persisting text size in this spec if Feature 018 has not yet landed; use a state parameter with a temporary default and integrate with the setting when available.

## Acceptance criteria

1. Three candidate slots render above the keys whenever the normal keyboard is visible.
2. Empty slots remain blank and the row does not collapse.
3. The row fits within configured keyboard height and does not change the IME window measurement unexpectedly.
4. Candidate text size is controlled independently from keyboard zoom and can be supplied from settings.
5. Horizontal panning affects only the row; it does not pan the keyboard or activate underlying keys.
6. Candidate touch targets and populated accessibility elements are individually selectable; blank slots are not unnecessary accessibility stops.
7. Row colors, borders, focus feedback, and text remain legible across themes, large text, magnification, orientation changes, and zoom settings.
8. Existing keyboard hitboxes, pan/zoom, emoji, key actions, and settings behavior remain unchanged.

## Dependencies and validation

- Depends on Features 016 and 018 for the persistent row and size setting.
- Use existing Compose UI and theme components; do not introduce a new UI framework.
- Add Compose tests for empty/populated rendering, click dispatch, semantics, sizing, and gesture isolation; manually check TalkBack, magnifier, landscape, and device pan/zoom.
