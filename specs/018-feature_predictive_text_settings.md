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
- Suggestion text-size implementation values: **22 sp default**, **16–32 sp range**, **2 sp steps**. This is a provisional setting choice aligned with the existing 22 sp Settings labels; it is not evidence of validated legibility across supported densities, magnification, or keyboard heights. Persisted values are clamped to this range. Record device-specific visual checks separately; broader legibility validation remains open.
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
3. The suggestion-size slider displays its current value and uses the documented 22 sp default, 16–32 sp range, and 2 sp steps; broader legibility validation across supported densities and magnification remains a follow-up validation item.
4. All three settings persist across application process restart and are exposed through the existing settings Flow.
5. Existing settings retain their values and behavior when the settings model and DataStore mapping are extended.
6. Switch and slider controls are operable and understandable using TalkBack and magnification.
7. This feature does not read editor text, perform prediction, or mutate host text.

## Dependencies and validation

- Depends on Feature 016.
- Uses existing DataStore settings architecture in `KeyboardSettings`, `DataStoreSettingsRepository`, `SettingsUseCases`, and `SettingsActivity`.
- Add unit/instrumentation coverage for defaults, independent toggles, size clamping, persistence, reactive updates, and settings accessibility semantics.
- Implementation uses 22 sp default, 16–32 sp range, and 2 sp steps. Settings controls were visually checked at the default 22 sp on the available SM-A566B (1080×2340, 450 dpi, Android 16, system font scale 1.0). System magnification was not enabled, other densities/heights were not tested, and the candidate row is not part of this feature; suggestion-rendering legibility across those conditions remains open.
