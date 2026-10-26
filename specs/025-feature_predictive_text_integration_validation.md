# Feature 025 — Predictive Text Integration and Validation

## Status and relationship

This is an integration and release-readiness spec, not a standalone user-facing capability. It is the final child spec of [Feature 016](016-feature_predictive_text_and_autocorrect_plan.md). It complements (but does not replace) the focused unit, UI, bridge, and instrumentation tests required in Features 017–024.

## Summary

Validate that settings, bounded editor context, suggestion UI, current-word candidates, next-word prediction, autocorrect/undo, capitalization, and two-space punctuation operate together safely and accessibly across representative host editors and device configurations.

## Scope

- Run the full behavior matrix for independent predictive-text and autocorrect settings.
- Verify end-to-end privacy guarantees and absence of network access or text persistence.
- Verify selected resource/model licences and that packaged artifacts match Feature 017’s approved recommendation.
- Test representative editor types, secure field variants, cursor/selection conditions, punctuation, composing text, editor actions, and lifecycle changes.
- Verify accessibility and keyboard UI integration across themes, text size, zoom/pan, keyboard height, orientation, and extract mode.
- Measure end-to-end suggestion response time, memory, and packaged resource size on the single available physical device using the Feature 017 method; report device-specific results.
- Document known compatibility limitations and release-blocking defects.

## Validation matrix

### Settings and feature combinations

- Both features enabled; predictive text only; autocorrect only; both disabled.
- Settings changed while IME is active and after process restart.
- Candidate text-size minimum/default/maximum with each theme.

### Text and editor behavior

- Current-word suggestions at word start, middle, and end; next-word prediction after boundaries.
- Current-word selection replacement and next-word insertion with exactly one trailing space.
- Autocorrect at space and punctuation boundaries, original-spelling undo, and undo invalidation.
- Sentence capitalization, standalone “I”, decimal/abbreviation cases, and two/three spaces.
- Cursor moves, selections, composing text, stale asynchronous results, empty/partial `InputConnection`, editor change, and session teardown.
- Multiple host editor implementations where available (these may be tested on the same physical device), plus multiline and editor-action fields.

### Privacy and offline behavior

- Password, visible-password, and web-password fields; verify no surrounding/extracted text reads are invoked.
- Verify context is before-cursor only, sentence bounded, no more than four preceding words for initial n-gram prediction, and under the approved character limit.
- Verify no model request occurs after settings disable the applicable behavior.
- Verify airplane/offline operation and no editor text in logs, persistent storage, or network traffic.

### Accessibility and layout

- TalkBack focus order, candidate labels/actions, blank slot behavior, no excessive announcements, and selection feedback.
- Magnification, large system font, all themes, candidate-row panning, keyboard panning/zoom, keyboard heights, portrait/landscape, and extract mode.
- Verify key hitboxes remain accessible and candidate-row gestures do not activate keys or pan the keyboard.

## Acceptance criteria

1. All feature-level acceptance criteria in Features 017–024 pass or have an explicitly approved, documented exception.
2. Both settings operate independently through typing, candidate display, autocorrection, and punctuation behavior.
3. Secure-field testing confirms no surrounding-text reads and no displayed suggestions or correction behavior.
4. Context-bound tests confirm the approved sentence, word-count, and character-count limits.
5. Stale asynchronous results or operations never alter the wrong editor, word, or session.
6. Candidate selection, undo, punctuation, and capitalization work without duplicate or lost delimiters in supported host editors.
7. TalkBack, magnification, themes, size controls, pan/zoom, keyboard height, and orientation pass the documented manual/instrumented checks.
8. Offline operation is demonstrated with network unavailable; no text is transmitted, logged, or persisted.
9. Model/library/data licences and packaged size are reviewed against Feature 017’s decision record.
10. Response time and memory are measured on the named available physical device and compared with thresholds established after feasibility evaluation; results are explicitly device-specific, with deviations documented and dispositioned.
11. Existing keyboard behaviors—key entry, deletion, shift, symbols, emoji, editor actions, settings persistence, and lifecycle stability—remain intact.
12. Test results, device/editor matrix, unresolved risks, and release recommendation are recorded.

## Dependencies

- Depends on implementation and validation outcomes from Features 017–024.
- Any failed behavior must be fixed in the owning child feature spec, not hidden by weakening integration criteria without review.
- Changes to core requirements, privacy bounds, model choice, or language scope require updating Feature 016 and affected child specs before release.
