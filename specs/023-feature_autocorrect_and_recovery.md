# Feature 023 — Autocorrect and Recovery

## Summary

Automatically correct likely British-English misspellings when the user types a space or punctuation key, and make the original spelling available as the first candidate to undo a correction. Autocorrect is independently controlled by the persisted setting from Feature 018.

## Scope

- Reuse the approved local dictionary and correction/ranking approach from Feature 017 and the safe current-word context/replacement APIs from Feature 019.
- Detect the completed word immediately before a typed space or punctuation delimiter.
- Replace only the completed word when the correction confidence meets the threshold established in Feature 017; preserve the delimiter and following text.
- Do not auto-correct on every keystroke, arbitrary cursor movement, or when context/selection is ambiguous.
- Keep a transient undo record for only the most recent correction while the corrected word remains the active relevant word.
- Display the original spelling as the first suggestion after correction. Selecting it restores the original spelling and preserves delimiter/cursor position.
- Clear the undo record when the user edits the corrected word, moves away from it, changes editor/session, or the context no longer matches.
- When autocorrect is disabled, do not perform automatic corrections and do not offer a correction-specific undo item; current-word/next-word suggestions remain governed by predictive-text setting.
- Suppress autocorrect and all text-context access in password, visible-password, and web-password fields.

## Correction safeguards

- Preserve punctuation, whitespace, capitalization pattern, and any following composing text.
- Do not correct unknown names/words or low-confidence input unless the approved dictionary/ranker explicitly supports a high-confidence correction.
- Do not correct a word that is part of an ambiguous selection or whose editor context has changed since it was read.
- If the editor refuses or partially applies a replacement, clear the undo state and avoid repeated correction loops.
- Do not persist the original word, corrected word, or undo record beyond the active editing session.

## Out of scope

- Next-word prediction and user-selected ordinary completions (Features 021–022).
- Sentence capitalization and two-space punctuation (Feature 024).
- Learned vocabulary, network services, per-app suppression, or non-`en-GB` correction.

## Acceptance criteria

1. With autocorrect enabled, a high-confidence misspelling is corrected when a space or punctuation delimiter is typed.
2. The delimiter remains present exactly once and adjacent text is not altered.
3. Original spelling is the first candidate after correction; selecting it restores the original word.
4. Undo applies only while its associated correction remains current and is cleared after edits, cursor moves, session/editor changes, or mismatched context.
5. Low-confidence or unknown words remain unchanged; the confidence threshold follows Feature 017’s documented decision.
6. With autocorrect disabled, no automatic replacement or correction-undo candidate is produced, independent of predictive-text setting.
7. No surrounding-text read is performed for password, visible-password, or web-password fields.
8. Stale context cannot cause replacement; partial/failed host-editor operations fail safely without a correction loop.
9. Tests cover space, punctuation, capitalization, composing text, correction boundaries, unknown words, false-positive cases, undo lifetime, toggle combinations, and secure fields.

## Dependencies

- Depends on Features 017, 018, and 019.
- Integrates undo state into Feature 020’s candidate row.
- Reuses current-word correction resources from Feature 021 where appropriate, without coupling autocorrect to user selection.
- Does not require next-word model integration from Feature 022.
