# Feature 022 — Next-Word Prediction

## Summary

Show up to three likely British-English next words after a word boundary, using the approved offline prediction implementation and bounded context. Selecting a candidate inserts the word followed by a space.

## Scope

- Implement next-word candidate generation behind the model-independent suggestion-engine contract.
- Use the model/data recommendation and licence approval from Feature 017. The selected approach may be an n-gram model or a small neural predictor if feasibility evidence supports it.
- Use up to five preceding words from the current sentence, only before the cursor, subject to Feature 017’s strict character cap and Feature 019’s secure context boundary.
- Trigger/recompute on word boundaries and relevant preceding-text changes; clear candidates on unsupported cursor positions or unavailable context.
- Present up to three candidates in Feature 020’s row when predictive text is enabled.
- On selection, insert the selected next word followed by exactly one trailing space, preserving the preceding sentence and cursor semantics.
- Apply capitalization appropriate to sentence position without adding punctuation except where explicitly supplied by the candidate policy.

## Ranking and safety

- Candidate ranking must be deterministic for identical model, context, and configuration.
- Filter empty tokens, whitespace-containing output, malformed tokens, and unsafe/unlicensed generated content. Model output is treated as untrusted data.
- Do not allow generated candidates to mutate text until the user selects one.
- Verify context/session freshness at selection; discard stale candidate actions.
- Context ends at a sentence boundary and never includes text after the cursor. Do not retain context after ranking.

## Out of scope

- Current-word completion/correction (Feature 021).
- Automatic autocorrection and undo (Feature 023).
- Sentence-start capitalization rules and double-space punctuation (Feature 024).
- Multilingual prediction, personalization, or cloud inference.

## Acceptance criteria

1. After a supported word boundary, the local engine returns up to three next-word candidates when predictive text is enabled.
2. Predictions use no more than five preceding words from the same sentence and never exceed the approved character cap.
3. The engine receives only context before the cursor, via Feature 019; it cannot call host-editor APIs.
4. Sentence-boundary, start-of-field, punctuation, empty-context, selection, and unsupported-cursor cases produce defined candidates or blank slots without unsafe reads.
5. Selecting a candidate inserts the word and one trailing space, without replacing preceding text or duplicating existing separator characters.
6. Disabling predictive text clears candidates and prevents new prediction requests.
7. Stale candidates cannot insert text after edits, cursor movement, editor changes, or session teardown.
8. Tests cover model-independent engine contract, candidate filtering, deterministic ordering, sentence termination, capitalization input, trailing-space behavior, and privacy bounds.
9. Device tests measure latency and memory against Feature 017’s recorded evaluation method and assess approved thresholds.

## Dependencies

- Depends on Features 017, 018, 019, and 020.
- Can be developed independently from Feature 021 once the shared engine contract and candidate model are agreed.
- Feature 025 validates end-to-end integration with current-word suggestions and autocorrect.
