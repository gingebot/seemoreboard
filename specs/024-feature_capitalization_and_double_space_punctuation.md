# Feature 024 — Capitalization and Double-Space Punctuation

## Summary

Apply sentence-start capitalization and standalone “I” capitalization to typed text and candidates. When autocorrect is enabled, convert two consecutive spaces after a word into a period followed by one space.

## Scope

- Detect sentence starts using bounded, permitted context from Feature 019 and punctuation/whitespace already available through normal IME input events.
- Capitalize the first alphabetic character typed at a sentence start.
- Capitalize standalone lowercase `i` when it is a complete word, including when a space or punctuation delimiter completes it.
- Ensure candidate casing follows the surrounding sentence/word context while preserving user-entered uppercase or shift-lock intent.
- When autocorrect is enabled, replace exactly two consecutive spaces after a word with `. `.
- Treat the two-space behavior as part of the autocorrect toggle; do not add another setting.
- Treat sentence-start and standalone “I” capitalization as part of the autocorrect setting; do not add a separate capitalization control.
- When autocorrect is disabled, preserve both spaces as typed and do not apply automatic capitalization.
- Avoid applying sentence capitalization or two-space punctuation in unsupported/ambiguous selections and secure fields.

## Behavior constraints and edge cases

- Do not capitalize after a decimal point, abbreviation, URL-like sequence, or other non-sentence period unless sentence-start detection positively identifies a sentence boundary.
- Sentence-ending punctuation includes at minimum `.`, `?`, and `!`; exact treatment of quotes/brackets adjacent to punctuation must be documented in implementation tests.
- A third consecutive space is not part of the specified conversion; define behavior as retaining user input rather than repeatedly rewriting punctuation.
- Do not rewrite existing document text on cursor movement; capitalization is applied only to newly typed input or candidate text selected in the active context.
- Preserve the user’s shift state and avoid double-capitalizing text already transformed by the host editor.

## Out of scope

- General punctuation auto-insertion other than two-space conversion.
- Changes to autocorrect confidence or undo behavior (Feature 023).
- Language-specific rules beyond initial British English.

## Acceptance criteria

1. The first typed letter at an identified sentence start is capitalized.
2. Standalone lowercase `i` becomes `I` when completed as a word, without changing embedded letters such as “inside”.
3. Candidate casing reflects sentence context and does not override explicit uppercase/shift-lock intent.
4. With autocorrect enabled, two spaces after a word become one period and one trailing space.
5. With autocorrect enabled, sentence-start and standalone “I” capitalization are applied; with autocorrect disabled, they are not.
6. With autocorrect disabled, two spaces remain unchanged.
7. A third consecutive space does not cause repeated punctuation rewrites; behavior is documented and tested.
8. Decimal points and common abbreviation cases are not incorrectly treated as definite sentence boundaries in the supported test set.
9. Secure-field and ambiguous-selection protections from Feature 019 remain in force.
10. Tests cover sentence-ending punctuation, quotes/brackets, abbreviations, decimals, beginning of field, standalone/embedded “i”, shift states, toggle combinations, and repeated spaces.

## Dependencies

- Depends on Features 018 and 019; integrates with Features 021–023 for candidate casing and autocorrection.
- Feature 025 performs end-to-end verification across all candidate and editor flows.
