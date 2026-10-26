# Feature 021 — Current-Word Suggestions

## Summary

Provide local British-English completions and spelling alternatives for the word currently being entered. Users choose a candidate to replace the current word span. This feature does not automatically correct text; automatic correction and undo are specified in Feature 023.

## Scope

- Implement current-word candidate generation through the model-independent suggestion-engine contract established by Feature 017.
- Use the approved local `en-GB` dictionary/resource and ranking approach selected by Feature 017.
- Request suggestions after relevant character edits and present up to three candidates in Feature 020’s row.
- Support a cursor inside a word or at its end. Use the bounded current-token context provided by Feature 019; do not broaden prediction history to post-cursor document text.
- Selecting a candidate replaces only the current word, preserving adjacent text and the cursor position expected by the feature behavior.
- Honor the independent predictive-text toggle. If off, clear current-word candidates and do not query the engine.
- Handle composing text and asynchronous generation without presenting stale candidates.

## Candidate and replacement behavior

- Rank candidates using the approved `en-GB` data, the entered prefix/current word, and capitalization context.
- Do not return the unchanged input as a suggestion.
- Return no candidates when the token is not a supported word, context is unavailable, the cursor/selection is ambiguous, or no candidate passes the engine’s confidence threshold.
- Candidate selection must verify that the editor/session and target word context still match the context used to generate the candidate; otherwise, discard the action and refresh suggestions.
- Do not commit a trailing space for a current-word candidate unless the user had already typed one; preserve existing suffix punctuation/spacing.

## Out of scope

- Next-word prediction (Feature 022).
- Automatic replacement on space/punctuation and correction undo (Feature 023).
- Sentence capitalization and double-space punctuation (Feature 024).
- User vocabulary learning, network inference, or languages other than British English.

## Acceptance criteria

1. Typing a supported current word produces up to three local `en-GB` completion/correction candidates when predictive text is enabled.
2. Disabling predictive text clears candidates and stops current-word engine requests without changing the autocorrect setting.
3. Selecting a candidate replaces exactly the intended word span, preserving surrounding text, punctuation, and unrelated selection/document contents.
4. Stale candidates are rejected after typing, cursor movement, selection changes, editor/session changes, or input-connection replacement.
5. Unsupported or unavailable context yields no candidates and no editor mutation.
6. Candidate row remains present with blank slots when candidates are absent.
7. No automatic correction occurs from candidate generation or display alone.
8. Unit tests cover common prefixes, no-match/low-confidence results, British spelling, capitalization preservation, punctuation adjacency, and cursor at token start/middle/end; bridge tests verify safe replacement.
9. Integration tests confirm prediction is offline, bounded, and unavailable for sensitive fields as enforced by Feature 019.

## Dependencies

- Depends on Feature 017 for licensed data and engine recommendation.
- Depends on Features 018, 019, and 020 for settings, safe context/replacement, and candidate UI.
- Feature 023 may reuse current-word lookup and correction ranking but owns automatic correction behavior.
