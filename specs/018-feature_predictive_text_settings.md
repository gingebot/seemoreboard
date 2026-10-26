# Feature 018 — Predictive Text Settings

## Summary

Add global, persisted controls for predictive text, autocorrect, and suggestion-row text size to SeeMoreBoard’s existing Settings activity. This feature creates settings only; it does not generate predictions or change host-editor text.

## User story

As a SeeMoreBoard user, I want to independently control predictive text and autocorrect and adjust suggestion text size, so I can tailor the keyboard’s suggestions to my needs.

## Scope

- Add **Predictive text** and **Autocorrect** switches near the bottom of the existing Settings activity.
- Add a **Suggestion text size** slider, independent from keyboard zoom and emoji size.
- Persist values globally through existing DataStore settings patterns.
- Add values to `KeyboardSettings`, repository mapping/setters, `SettingsUseCases`, and the settings UI state flow.
- Make controls available to the IME view model through the existing reactive settings flow.

## Defaults and size behavior

- Predictive text: enabled by default.
- Autocorrect: enabled by default.
- Suggestion text-size default, minimum, maximum, and slider steps are not predetermined in this planning spec. Select and document values through legibility/usability testing across supported screen densities, magnification, and keyboard heights before implementation; clamp persisted values to the chosen supported range.
- Toggling predictive text off clears ordinary current-word and next-word suggestions, but does not implicitly change the autocorrect switch. If autocorrect remains enabled, the one-shot original-spelling undo candidate after a correction remains available.
- Toggling autocorrect off stops automatic spelling correction, autocorrect-specific undo candidates, sentence capitalization, and two-space punctuation behavior; it does not implicitly disable predictive suggestions.
- The suggestion row remains present with blank slots when there are no ordinary candidates or correction-undo candidate.

## Accessibility and behavior

- All controls have clear labels, state descriptions, and touch targets consistent with existing settings controls.
- TalkBack must announce labels and current switch/slider values.
- Changing a setting updates the displayed value immediately and persists it across process restart.
- Settings are global; there is no language or per-app setting in this feature.

## Out of scope

- Suggestion generation, model selection, language selection, per-app profiles, learned vocabulary, and network settings.
- Implementing the candidate row or applying text corrections.

## Acceptance criteria

1. Predictive text and autocorrect appear as separate switches near the bottom of Settings.
2. Both switches default to enabled on a fresh installation and persist independently.
3. The suggestion-size slider displays its current value; its default, range, and step size are chosen and documented through legibility testing before implementation.
4. All three settings persist across application process restart and are exposed through the existing settings Flow.
5. Existing settings retain their values and behavior when the settings model and DataStore mapping are extended.
6. Switch and slider controls are operable and understandable using TalkBack and magnification.
7. This feature does not read editor text, perform prediction, or mutate host text.

## Dependencies and validation

- Depends on Feature 016.
- Uses existing DataStore settings architecture in `KeyboardSettings`, `DataStoreSettingsRepository`, `SettingsUseCases`, and `SettingsActivity`.
- Add unit/instrumentation coverage for defaults, independent toggles, size clamping, persistence, reactive updates, and settings accessibility semantics.
- Validate the selected size range and default with magnification and representative screen densities before fixing them as acceptance values.
