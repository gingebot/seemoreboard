# Feature 019 — Safe Editor Context

## Summary

Define and implement the bounded, privacy-conscious editor-context boundary required by predictive text. All permitted host-editor reads and all host-text mutations remain behind `InputConnectionBridge`. This feature does not add a model or user-visible suggestions.

## Scope

- Track the active editor/session and relevant `EditorInfo` input type in the IME service/view model.
- Classify password, visible-password, and web-password input variations as sensitive before any surrounding-text request.
- Expose a narrow bridge API for bounded text-before-cursor context, selection/cursor metadata where available, and safe replacement operations needed by later specs.
- Define context structures with explicit cursor-relative spans and word-boundary classification rather than passing a raw, unbounded editor dump.
- Support prediction history only from text before the cursor, within the current sentence, at most five preceding words, and the character cap selected by Feature 017.
- Permit bounded current-token inspection on both sides of the cursor only where needed for current-word replacement; this is distinct from next-word prediction history. Never perform either read for sensitive fields.
- Clear cached context and invalidate asynchronous work when editor/session identity changes or the bridge is cleared.
- Detect unsupported selection/cursor states and return “context unavailable” rather than guessing.

## Privacy invariants

- For password, visible-password, and web-password fields, do not call any API that reads surrounding text or extracted editor content. Do not request composing text for prediction.
- Never log, persist, transmit, or retain editor text beyond the immediate bounded request/operation.
- Bound every context query before making it; enforce the limit at the bridge boundary, not only in the model.
- Do not query text after the cursor for next-word prediction.
- Do not add suppression for non-password field types or per-app suppression.

## Input and replacement behavior

- Maintain a session/generation token so a context result obtained for an old editor cannot be consumed after an editor switch.
- Represent selection separately from cursor; disable prediction/replacement when a selection makes the target ambiguous.
- Provide replacement methods that take explicit expected context/span and fail safely when current editor state no longer matches.
- Avoid broad document replacement. Use the narrowest reliable composing/commit operations supported by Android `InputConnection`.
- Host editors may reject or incompletely implement context calls; treat null, partial, or invalid results as unavailable.

## Out of scope

- Suggestions, ranking, autocorrect, candidate UI, or settings.
- Increasing context beyond Feature 016’s current sentence/five-word bound or Feature 017’s measured character cap.
- Data retention or personalization.

## Acceptance criteria

1. Secure input classification covers password, visible-password, and web-password variations.
2. Tests verify that no surrounding-text or extracted-text read is invoked for those sensitive classes.
3. Prediction history is bounded before the cursor, stops at the sentence boundary, includes at most five words, and never exceeds the Feature 017 character cap.
4. Current-token reads are separately bounded and only available for current-word operations; no post-cursor text is exposed as next-word history.
5. Context results distinguish word position, selection, cursor position, and unavailable/unsupported editor responses.
6. Editor/session changes invalidate prior context and stale operation tokens.
7. Replacement fails safely on stale/mismatched context and never replaces an unrelated or unbounded text range.
8. Context is not written to logs, persistent storage, or network requests.
9. Tests cover null/partial `InputConnection` responses, cursor at word start/middle/end, selections, punctuation/sentence boundaries, and input-view teardown.

## Dependencies

- Depends on Features 016 and 017 (for the selected character bound).
- Provides a required foundation for Features 021–023.
- Must be validated against more than one real host editor before dependent autocorrection is enabled by default in production.
