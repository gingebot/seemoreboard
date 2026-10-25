# Feature 016 — Predictive Text & Autocorrect: Planning Specification

**Status:** Planning only. This document defines the intended feature, architecture, constraints, risks, and delivery sequence. It is not authorization to implement the complete feature in one pass. Each delivery milestone below should be refined into a smaller feature spec before implementation.

## Summary

Add offline predictive text and autocorrect to SeeMoreBoard, an Android IME designed for people with low vision. The feature will provide up to three suggestions in a persistent row above the keyboard. Suggestions will complete or correct the word around the cursor, or predict the next word based on the immediately preceding word. Selecting a suggestion replaces the relevant text; selecting a next-word prediction adds a trailing space.

Autocorrect will be enabled by default and will automatically correct likely misspellings when the user types a space or punctuation. Users can independently enable or disable predictive text and autocorrect in the existing global Settings activity.

The initial language is **British English (`en-GB`) only**. The feature must work entirely offline, must not learn from users’ typing, and must suppress suggestions and text-context access in password fields.

## Goals

- Provide useful next-word predictions and current-word completions/corrections as the user types.
- Automatically correct likely misspellings at word boundaries.
- Preserve an accessible, high-contrast keyboard experience, including a persistent candidate row with independently adjustable text size.
- Keep suggestion generation and text processing local to the device.
- Keep input mutation behind the existing `InputConnectionBridge`.
- Define incremental implementation goals and testable acceptance criteria.

## Non-goals

- Gesture or swipe typing.
- Multilingual input or a language selector in this release.
- Cloud services, network access, telemetry, or remotely downloaded language data.
- Learning from typed text, personalized ranking, or user-managed vocabulary.
- Per-app suppression controls.
- Automatic punctuation beyond the agreed two-space-to-period behavior.
- Implementation of the whole roadmap as a single feature.

## User-visible behavior

### Suggestions

- Display a dedicated suggestion row **above the keyboard keys**.
- Keep the row visible at all times while the regular keyboard is visible. When there are no candidates, show three blank candidate slots.
- Show up to three candidates.
- While the cursor is inside a word or at its end, suggest completions or corrections for that word. If the current word is misspelled and autocorrect applies a correction, expose the original spelling as the first suggestion so the user can undo it.
- After a word boundary, predict the next word using up to five preceding words from the current sentence as context. Context must be read only before the cursor and be subject to a strict character limit established during feasibility testing.
- Selecting a current-word candidate replaces the word around the cursor.
- Selecting a next-word candidate inserts the predicted word followed by a space.
- Include appropriate capitalization and punctuation in candidate generation and rendering.

### Autocorrect and capitalization

- Autocorrect is enabled by default and is independently switchable in settings.
- When enabled, correct likely misspellings when the user types a space or punctuation key. Do not autocorrect in response to arbitrary cursor movement or while the cursor is not at a supported word position.
- After an automatic correction, offer the original spelling as the first candidate. Tapping it restores the original spelling. Keep this undo candidate available while it still refers to the most recent corrected word; clear it when the user edits or moves away from that word.
- Apply sentence-start capitalization and capitalize the standalone “I” where the engine identifies those cases.
- When the user types two consecutive spaces after a word, replace them with a period and a single trailing space, following common keyboard behavior. This behavior follows the autocorrect setting; it does not introduce a third punctuation-specific setting.

### Settings

Add controls near the bottom of the existing Settings activity:

- **Predictive text:** on/off.
- **Autocorrect:** on/off.
- **Suggestion text size:** slider.

The switches are global, persisted in DataStore, and enabled by default. Suggestion text size is independent of keyboard zoom. The initial release uses British English without a language selector; the architecture should allow language configuration to be added later.

### Accessibility and layout

- Candidates must have accessible labels and be individually focusable/selectable through accessibility services.
- Do not announce every changing prediction automatically through TalkBack. Candidate focus and selection should provide appropriate spoken feedback; changing suggestions may use a polite announcement only when needed to make the updated choices discoverable without excessive interruption.
- The row has its own text-size setting and must remain usable with magnification and large text.
- The row must work with existing themes, keyboard-height behavior, pan/zoom, portrait and landscape layouts, and Android extract mode.
- When all candidates cannot fit horizontally, the user can pan the candidate row left or right independently of the keyboard pan/zoom area. The slider controls suggestion text size, not keyboard zoom.

### Privacy and editor compatibility

- Disable prediction and autocorrection for password, visible-password, and web-password fields.
- Do not query surrounding text in those sensitive fields.
- Do not suppress suggestions in other fields in this release, and do not add per-app suppression.
- For prediction, use only the current word (or its prefix while being typed) and up to five preceding words from the current sentence. Read context only before the cursor, stop at sentence boundaries, and apply a strict character limit established during feasibility testing. Do not retain editor text after processing it.
- Clear candidate and undo state when the editor changes, the input session ends, or context becomes unreliable.
- If the cursor is neither inside a word nor at its end, show no suggestions.
- Do not infer language switching or support mixed-language typing.

## Architecture

The feature should fit the current Kotlin/Compose IME structure rather than introduce an unrelated input pipeline.

### Data and suggestion engine

Add a local suggestions area, aligned with the roadmap in `/home/gingebot/projects/kanboard/docs/architecture.md`:

- `data/suggestions/` owns bundled, offline British-English vocabulary and any prediction resources.
- `domain/input/` defines the suggestion-engine contract and input-processing rules.
- The engine returns a bounded candidate list from the permitted local context; it does not read or mutate host editor text. The input-context layer enforces the sentence and character bounds before passing text to the engine.
- No personal vocabulary learning or network access is permitted.

**Model and data-source choices remain feasibility tasks.** Evaluate separate approaches for the two related problems:

- **Next-word prediction:** Start by measuring a compact n-gram model, which ranks likely words from bounded sentence context. KenLM is one possible scoring implementation, not a ready-made British-English keyboard model; suitable `en-GB` model data would still need to be sourced or built and licensed.
- **Autocorrect/current-word candidates:** Evaluate a bundled British-English dictionary with correction/ranking logic. It may share vocabulary resources with prediction, but should not depend on a generative model to identify spelling corrections.
- **Alternative:** If n-gram quality is insufficient, compare a small neural next-word model, including its model/runtime size, latency, memory use, offline support, and licensing.
- **Generative small LLM:** Include only as a comparison in feasibility work, not as the assumed implementation. Assess whether its runtime and model overhead are justified for a keyboard that needs at most three word candidates.

The architecture should expose a model-independent suggestion-engine interface so feasibility results can change the implementation without changing IME/UI responsibilities. Evaluate British-English quality, licensing, maintenance, packaged resource size, runtime memory, and response time. Android spell-checker services should not be assumed to provide consistent `en-GB` resources or behavior across devices.

### IME context and text editing

- `SeeMoreBoardService` continues to receive editor metadata through `EditorInfo`.
- `KeyboardViewModel` coordinates enabled settings, editor state, candidate requests, and user actions.
- `InputConnectionBridge` remains the sole boundary for reading permitted editor context and modifying host text. It should expose narrowly scoped operations for retrieving bounded context and replacing text safely.
- The bridge must reject or avoid context reads for sensitive fields.
- Candidate generation runs off the UI thread where appropriate. Stale results must not be displayed after typing, cursor movement, field changes, or input-session changes.
- Text replacement must use editor-compatible `InputConnection` operations and preserve cursor and composing-text behavior as reliably as the host editor allows. Selection and composition edge cases are explicit test targets.

### UI and state

- `KeyboardState` carries the current candidates, current-word/next-word suggestion mode, and any temporary undo candidate.
- `KeyboardScreen` renders a dedicated candidate row above the existing keyboard layout.
- Candidate-row horizontal panning is independent from `PanContainer`’s keyboard pan and zoom.
- The row’s text size is driven by its own DataStore-backed setting; keyboard zoom continues to control the existing keyboard.
- Existing keyboard layout, editor action, haptics, emoji, and settings behavior must continue to work.

### Settings and persistence

Extend `KeyboardSettings`, `DataStoreSettingsRepository`, and `SettingsUseCases` with persisted prediction/autocorrect switches and candidate text size. Use defaults that enable both text features. There is no language preference in the first release: the bundled language is `en-GB`.

## Acceptance criteria for the complete feature

1. Predictive text and autocorrect are enabled by default and can be toggled independently.
2. Settings survive app restart and are global rather than per-app.
3. No network access is required for suggestions or correction.
4. No user-typed vocabulary is learned or persisted.
5. Suggestions appear as the user types, with at most three candidates.
6. Current-word selection replaces the word around the cursor; next-word selection inserts a trailing space. Next-word predictions use at most five preceding words from the current sentence, read only before the cursor and within a character limit established by feasibility testing.
7. A likely typo is automatically corrected on space or punctuation when autocorrect is enabled.
8. The original spelling is offered first after automatic correction and tapping it restores that spelling while the undo candidate remains valid.
9. Sentence-start and standalone “I” capitalization are handled.
10. Two spaces after a word become a period and one trailing space when autocorrect is enabled.
11. Both text features are inactive for password, visible-password, and web-password fields, and no surrounding-text query is made in those fields.
12. No suggestions are shown when the cursor is outside a supported word position.
13. Candidate state is cleared or refreshed correctly on cursor movement, editor changes, session changes, and stale asynchronous results.
14. The candidate row remains visible with blank slots when there are no candidates.
15. Candidate text size can be adjusted independently of keyboard zoom and persists across sessions.
16. The row’s horizontal panning does not interfere with keyboard panning or key activation.
17. Candidates are accessible through TalkBack and remain usable with magnification and large text.
18. Existing keyboard features continue to work across themes, keyboard heights, zoom levels, orientation changes, and editor actions.
19. Unit and instrumentation tests cover prediction, replacement, autocorrect, undo, capitalization, punctuation, privacy, editor lifecycle, and accessibility-relevant UI behavior.
20. Performance and app-size targets are measured and assessed during the data-feasibility and testing milestones; numerical limits are not assumed in this planning spec.

## Incremental delivery plan

Each item below should become its own small feature spec with its own acceptance criteria before implementation.

### Goal 1 — Offline language-data feasibility

- Identify candidate British-English dictionary and prediction resources and their licences.
- Compare compact n-gram next-word prediction, dictionary-based autocorrect/current-word ranking, and a small neural predictor if n-gram quality is inadequate. Treat a generative small LLM as a comparison rather than a default.
- Test bounded context of up to five preceding words in the current sentence and establish a strict maximum character count from measured quality, latency, and memory use.
- Measure resource size and assess expected quality and maintenance implications.
- Decide whether to bundle a resource or use another verified offline source.
- Record prediction quality, performance, runtime memory, and package-size results for later acceptance thresholds.

**Exit condition:** An approved, legally usable offline source and documented tradeoffs.

### Goal 2 — Persisted settings

- Add the independent predictive-text and autocorrect switches.
- Add the suggestion text-size slider.
- Persist these globally using the existing DataStore approach.
- Confirm both switches default to enabled and settings survive restart.

### Goal 3 — Safe editor context

- Define supported word-boundary and cursor-context detection.
- Add bounded context access through `InputConnectionBridge`.
- Identify password, visible-password, and web-password fields and prohibit context reads there.
- Test editor changes, selection, cursor movement, composing text, and unsupported contexts.

### Goal 4 — Candidate-row UI

- Add the always-visible three-slot row above the keys, initially with blank slots.
- Apply themes and independent text-size setting.
- Add candidate-row-only horizontal panning.
- Verify no interference with keyboard zoom/pan, key hitboxes, keyboard height, and accessibility services.

### Goal 5 — Current-word suggestions

- Implement the suggestion-engine contract and local candidate lookup.
- Generate current-word completions/corrections at the cursor while typing.
- Replace the correct word span when a candidate is selected.
- Ensure stale results and unsupported cursor positions do not show misleading candidates.

### Goal 6 — Next-word prediction

- Generate next-word candidates using up to five preceding words from the current sentence, subject to the tested character limit and context-before-cursor privacy rule.
- Add a trailing space when a next-word candidate is selected.
- Evaluate the selected n-gram/model approach against the model-independent engine contract; use a small neural predictor only if feasibility results justify it.
- Verify punctuation and capitalization handling at word and sentence boundaries.

### Goal 7 — Autocorrect and recovery

- Apply likely corrections on space and punctuation when enabled.
- Show the original spelling as the first candidate after an automatic correction.
- Restore the original spelling when that candidate is selected.
- Clear the undo candidate when the edited word or relevant context changes.

### Goal 8 — Capitalization and punctuation

- Add sentence-start capitalization and standalone “I” capitalization.
- Add two-space-to-period-and-space behavior under the autocorrect setting.
- Cover capitalization, punctuation, editor-action, and cursor-position edge cases.

### Goal 9 — Full integration and validation

- Validate TalkBack and magnifier navigation/selection.
- Validate themes, independent text sizing, zoom, pan, keyboard height, rotation, and landscape/extract mode.
- Test multiple host editor types, secure field types, and lifecycle transitions.
- Measure suggestion responsiveness and resource/app-size impact, then decide whether explicit numerical thresholds should be added to follow-up specs.

## Risks and open implementation questions

- **Language resource quality/licensing:** No dictionary or prediction model data source has yet been selected. This is the first dependency to resolve.
- **Prediction-model tradeoffs:** N-gram, neural, and generative approaches differ in quality, integration complexity, runtime, and footprint. The engine interface should remain model-independent until feasibility testing selects an approach.
- **Editor variability:** `InputConnection` context and replacement behavior can vary among host apps, especially around selections and composing text. Real-editor validation is required.
- **Undo semantics:** Retain the original spelling only while the most recent correction remains the active relevant word; subsequent edits or cursor movement clear it.
- **TalkBack verbosity:** Candidates should be discoverable and selectable without announcing every prediction update. Confirm this behavior through accessibility testing.
- **Performance and size limits:** No numerical target was provided. Measure first, then set thresholds based on device testing.
- **Settings-language interpretation:** British English is fixed for this release, so there is no language selector; architecture should not prevent adding language configuration later.