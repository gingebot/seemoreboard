# Feature 017 — Offline Language Model and Data Feasibility

## Status and relationship

This is a **research spike**, not a user-facing feature and not an instruction to integrate a model. It is the first child spec of [Feature 016](016-feature_predictive_text_and_autocorrect_plan.md). Its findings gate model-dependent work in Features 021–023.

## Summary

Determine whether SeeMoreBoard can provide useful British-English current-word corrections and next-word predictions entirely offline with appropriately licensed resources and acceptable quality, package size, memory use, and response time. Keep autocorrect and next-word prediction as separate evaluated capabilities; they may share a vocabulary resource but need not share an algorithm.

## Scope

- Identify candidate `en-GB` dictionaries, corpora, and prediction resources, documenting provenance, licence, redistribution terms, update/maintenance status, and suitability for commercial or GPL-3.0 distribution.
- Compare dictionary-based candidate generation/edit-distance or equivalent ranking for current-word spelling with compact n-gram next-word ranking.
- Consider a small neural next-word model only if an n-gram baseline fails the agreed quality assessment.
- Include a small generative LLM only as a comparative option; do not assume its inference/runtime overhead is warranted for three word candidates.
- Evaluate KenLM or another n-gram scorer as a possible implementation, without treating a scoring library as a ready-made `en-GB` model or dictionary.
- Prototype or benchmark offline inference as needed. Any prototype must not be integrated into the production IME or access live host-editor text.
- Evaluate bounded input of up to five preceding words in the current sentence, read before the cursor, with a maximum character count selected from measured quality, latency, and memory results.

## Out of scope

- Production IME integration, settings UI, or candidate-row UI.
- Network access, cloud inference, telemetry, or downloading resources after installation.
- User-specific learning, vocabulary collection, or retaining typed evaluation data.
- Expanding beyond British English.

## Evaluation method

1. Define a small, documented British-English test set for word correction and next-word prediction. Use appropriately licensed/public test material or synthetic examples; do not collect private user text.
2. Include common British spellings and punctuation, sentence starts, one-word and longer contexts, sentence boundaries, unknown words, names, and ambiguous contexts.
3. Compare candidate approaches on top-three usefulness/accuracy, correction false positives, and ability to return a stable bounded list.
4. Measure packaged resource size, peak/runtime memory, cold-start cost, and suggestion latency on representative minimum- and mid-range Android devices. Record device, Android version, ABI, build mode, and measurement method.
5. Compare several bounded-context limits, including the proposed maximum of five preceding words, and choose a strict character cap from measured results.
6. Review native/runtime implications, supported Android ABIs, build maintenance, and licence obligations for libraries and model data independently.

## Architecture constraints

- The production design must expose a model-independent suggestion-engine contract.
- Model code receives only bounded, already-authorized context; it does not access `InputConnection` or host-editor APIs.
- Current-word correction and next-word prediction may use different implementations.
- No approach may require network access or persist editor context.
- Reject any resource whose redistribution rights are unclear.

## Deliverables

- A comparison report with candidate resources, licences, quality results, size, memory, latency, Android integration cost, and limitations.
- A recommendation for current-word correction resources/algorithm.
- A recommendation for next-word prediction (n-gram, neural, or no viable model yet).
- A documented maximum context character count and confirmation of the five-word sentence-local bound, or a rationale and proposal to revise it.
- A decision record listing unresolved risks, thresholds requiring product approval, and the feature specs unblocked by the outcome.

## Acceptance criteria / exit conditions

1. At least one offline approach has been evaluated for spelling correction and one for next-word prediction, or the report explains why no viable candidate was available.
2. Licence and redistribution status is documented for every shortlisted library and data resource; no unclear resource is recommended for bundling.
3. Quality is assessed on a documented `en-GB` test set, including false-positive autocorrection risk.
4. Resource/package size, runtime memory, cold-start behavior, and response time are measured on named Android device configurations.
5. The feasibility report compares n-gram prediction with a small neural alternative when the baseline quality warrants it; generative LLM consideration remains comparative rather than assumed.
6. A strict maximum context character count is selected based on measurements, and the accepted context stays before-cursor, within the current sentence, and no more than five preceding words.
7. A recommendation is made without prematurely changing the model-independent architecture contract.
8. No production code, live editor integration, network requirement, or user-text persistence is introduced by this spike.

## Dependencies and follow-up

- Depends on Feature 016 requirements.
- Its data/model decision is an input to Features 021 and 022, and may affect Feature 023’s correction resource choice.
- Findings that change user-visible behavior, language, privacy bounds, or resource distribution requirements must be proposed as a revision to Feature 016 before dependent feature implementation.
