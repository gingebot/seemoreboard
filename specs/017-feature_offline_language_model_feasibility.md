# Feature 017 — Offline Language Model and Data Feasibility

**Status: Provisional algorithm recommendation recorded; validation exit criteria are not met because representative prediction/correction quality evidence is still missing.**

**Relationship**

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
- Evaluate bounded input of up to four preceding words for the initial n-gram approach, within the current sentence and before the cursor, with a maximum character count selected from measured quality, latency, and memory results. Longer context may be evaluated separately for a model that can use it.

## Out of scope

- Production IME integration, settings UI, or candidate-row UI.
- Network access, cloud inference, telemetry, or downloading resources after installation.
- User-specific learning, vocabulary collection, or retaining typed evaluation data.
- Expanding beyond British English.

## Architecture constraints

- The production design must expose a model-independent suggestion-engine contract.
- Model code receives only bounded, already-authorized context; it does not access `InputConnection` or host-editor APIs.
- Current-word correction and next-word prediction may use different implementations.
- No approach may require network access or persist editor context.
- Reject any resource whose redistribution rights are unclear.

## Provisional algorithm recommendation

These recommendations define the next prototype/evaluation baseline. They are **not production approval** because representative quality evaluation and model/resource clearance remain outstanding.

#### Next-word prediction

- Start with a local, model-independent n-gram engine using counts for the current word plus up to four preceding words in the same sentence. This corresponds to a 5-gram predicting the next token.
- Rank up to three candidates using a backoff/interpolated scorer. Fall back to lower-order evidence when a longer context is unseen; report coverage so fallback performance is not hidden.
- Compare a simple compact Kotlin implementation (for example, sorted tables or a bounded local index) against KenLM only if the simple implementation’s latency, memory, or model-size results justify native C++ integration.
- Keep unigram-frequency fallback as a baseline, not the final context model. A word-frequency list alone is not conditional next-word prediction.
- Do not train or bundle from the single Google Ngram shard or use its same-shard temporal score as quality evidence.

#### Current-word suggestions and autocorrect

- Use a British-English Hunspell/SCOWL-derived dictionary as the leading vocabulary candidate, subject to review of its full data notices.
- Evaluate Hunspell affix and `REP` rule handling rather than treating `.dic` as a flat word list. Use a separate ranking layer for candidate ordering and confidence.
- Compare Hunspell-aware candidate generation with plain edit-distance ranking. Plain edit distance is a control baseline only; its temporary smoke test missed several intuitive corrections.
- Automatic correction must use a conservative threshold selected from false-positive evaluation. Keep the original spelling available for the specified undo behavior; no learning or personalization is in scope.
- Do not infer that dictionary membership, a high top-three recall on synthetic examples, or a typo smoke test proves safe automatic correction.

#### Recommendation status

Adopt the above as the **provisional prototype direction**: n-gram backoff for next-word prediction; Hunspell-aware candidates plus conservative ranking for correction. Keep the model-independent engine interface, algorithm choice revisable, and Features 021–023 gated on valid quality evidence and data/resource decisions. A small neural predictor is only a follow-up comparison if an evaluated n-gram baseline is inadequate; a generative LLM remains a non-default comparison.

## Deliverables

- A comparison report with candidate resources, licences, quality results, size, memory, latency, Android integration cost, and limitations.
- A recommendation for current-word correction resources/algorithm.
- A recommendation for next-word prediction (n-gram, neural, or no viable model yet).
- A documented maximum context character count and confirmation of the four-word sentence-local bound for initial n-gram prediction, or a rationale and proposal to revise it.
- A decision record listing unresolved risks, thresholds requiring product approval, and the feature specs unblocked by the outcome.

## Acceptance criteria / exit conditions

1. At least one offline approach has been evaluated for spelling correction and one for next-word prediction, or the report explains why no viable candidate was available.
2. Licence and redistribution status is documented for every shortlisted library and data resource; no unclear resource is recommended for bundling.
3. Prediction and correction are separately assessed on a documented `en-GB` test set; report prediction coverage/top-1/top-3 and correction precision/false-positive risk, including unseen contexts and valid-word hard negatives.
4. Resource/package size, runtime memory, cold-start behavior, and response time are measured on the named available Android device, with results clearly limited to that configuration.
5. The feasibility report compares n-gram prediction with a small neural alternative when the baseline quality warrants it; generative LLM consideration remains comparative rather than assumed.
6. A strict maximum context character count is selected from a valid evaluation, and initial n-gram context stays before-cursor, within the current sentence, and no more than four preceding words.
7. A recommendation is made without prematurely changing the model-independent architecture contract.
8. No production code, live editor integration, network requirement, or user-text persistence is introduced by this spike.

## Dependencies and follow-up

- Depends on Feature 016 requirements.
- Its data/model decision is an input to Features 021 and 022, and may affect Feature 023’s correction resource choice.
- Findings that change user-visible behavior, language, privacy bounds, or resource distribution requirements must be proposed as a revision to Feature 016 before dependent feature implementation.

## Initial feasibility findings (2026-10-26)

These are desk-research findings plus a one-shard host-side smoke test. They are not a full model evaluation, Android benchmark, or legal opinion.

### Candidate spelling data

- The `wooorm/dictionaries` `dictionary-en-gb` package identifies itself as an English (United Kingdom) Hunspell dictionary generated from SCOWL. Its repository lists the dictionary and affix data under **MIT AND BSD** and separately includes a detailed source/licence notice. The current repository API reports `index.dic` at 552,106 bytes and `index.aff` at 3,086 bytes (555,192 bytes total, about 542 KiB), before any runtime/index overhead.
- Its detailed notice describes SCOWL-derived `en_GB-ise` and `en_GB-ize` variants, says the British and large dictionaries are experimental in that SCOWL release, and identifies upstream/component licences and attribution requirements. Do not rely on package metadata alone: retain and review the full notice and each applicable component condition before bundling.
- This is word/affix data for spell checking and candidate generation; it has no word-sequence probabilities and cannot provide next-word prediction by itself.
- A separate `LibreOffice/dictionaries` `en_GB` Hunspell dictionary says its word list is based on Kevin Atkinson’s English word list and covered by his LGPL licence; its affix file is also stated to be LGPL. Its README documents ongoing edits and sources. It is a possible alternative, but its terms and integration suitability need their own review.
- The Common Voice `cv-dataset` source repository is MPL-2.0, but that is the metadata/code repository licence, not a demonstrated licence grant for the dataset text. The current Corpus 27 release manifest lists an `en` locale with an aggregate `size` of 94,956,262,763 bytes, 2,826.8 validated hours, and accent-labelled clips from England, Scotland, Wales, Ireland, and other regions; it does not list an `en-GB` locale and contains no licence field. The English datasheet describes English generally and does not state the sentence prompts’ source licence. Mozilla Data Collective’s Terms of Service (last updated 2026-05-06) state that a consumer may use a Dataset only for purposes in its dataset-specific “Data Consumer License”; dataset terms must be accepted before download; and the general terms prohibit scraping, mirroring, or redistributing the Dataset. The terms discuss derivative datasets/model outputs but do not grant general model-training or model-redistribution rights. Common Voice could only be considered after obtaining its Corpus 27 `en` Data Consumer License and confirming rights in the sentence text and intended model use. It is scripted speech and mixed-accent English, so even if licensed it is supplementary rather than a representative natural-typing corpus.

- Google Books Ngram publishes a **British English** corpus. The latest linked export on the checked dataset index is version `20200217`; it offers 1- through 5-gram exports, each split into shards. The 3-gram index lists 1,283 shards, and 2-, 4-, and 5-gram indices are also large. HTTP HEAD requests for sampled 2020 shards returned approximately 434–873 MB **per shard**. Downloading or bundling the complete current corpus is not a reasonable first experiment.
- The export page shows records as an n-gram followed by year and count fields; current exports also contain POS-tagged tokens and boundary tags such as `_START_`, requiring normalization and filtering for a user-facing word model. Some content reflects historical book language and OCR noise, so it may not model contemporary keyboard input well.
- Google’s Ngram documentation says data can be used freely with source acknowledgement; the export page also displays a Creative Commons Attribution 3.0 notice. Before distributing a derived model, preserve attribution and confirm the applicable terms for the exact downloaded version and transformation. This research does not make a legal determination about model derivatives.
- The Oxford BNC overview describes a 100-million-word, 90% written / 10% spoken corpus of late-20th-century British English across written and spoken registers. Oxford’s product page requires users to agree to a separate licence. The user-supplied licence text and its analysis are recorded in “User-supplied BNC licence review” below.
- The official UK Parliament API landing page says that it exposes “some of the procedural data for the UK parliament” and that its data is licensed under the Open Parliament Licence. That licence is an attribution licence; its terms allow reuse/adaptation of covered material subject to attribution and third-party-rights exclusions. However, the reviewed API description does not establish that full Hansard transcript text is available from those endpoints. Parliament copyright pages were blocked during research. Treat this as a data-discovery lead only, not a confirmed language corpus or permission to redistribute a model trained on transcripts.
- Project Gutenberg is not a blanket UK-cleared corpus. Its own licence explains that users outside the United States must check the law where they are located; for copyrighted works, permission given to Project Gutenberg does not automatically extend to downstream redistribution. A book-by-book UK rights and licence audit would be required before use, so do not treat Gutenberg as an approved source for this spike.
- KenLM is an n-gram scoring/building toolkit, not a British-English corpus or ready-made model. Its documentation describes binary models and memory mapping, but Android integration would require native build/ABI work. No NDK was installed, so the experiment used a temporary Java/ART harness instead of KenLM; this device measurement is not a KenLM benchmark.
- **Context order:** the initial context limit is now four preceding words. This matches a conventional 5-gram next-word model and the maximum order available in the located Google British-English corpus. Any future model using longer context needs separate evaluation and a spec revision.

### User-supplied BNC licence review

The user supplied a copy of the BNC User Licence. This text has not been independently authenticated as the current licence version. Relevant terms supplied by the user:

- Clause 2(a) grants a non-exclusive, non-transferable licence to use BNC Processed Material for “linguistic research and/or the development of language products”.
- Clauses 2(b)–(e) restrict access to the licensee/research group, prohibit giving third parties access to the material, and require care over copies and security.
- Clause 2(f) says there is “no restriction on the use of the Licensee’s Results” except that BNC extracts may not be published or commercially exploited beyond fair dealing.
- Clause 2(g) expressly grants no right to reproduce BNC Texts or use any part of them in commercial products/services except as fair dealing permits.
- Clause 3 describes fees, while the user reports the current corpus is free to access. The current online licence/fee position must therefore be confirmed.

**Assessment:** The supplied wording appears to allow private linguistic research and development of language products, but it does not clearly authorize shipping model weights trained on the corpus in a commercial keyboard, especially given clause 2(g). Keep the BNC **uncleared for model distribution** until Oxford/OTA confirms the current licence version, fee status, and whether a statistical language model embedded in SeeMoreBoard is permitted. This is a rights-risk assessment, not legal advice.

### Single-device scope and result

- The owner has confirmed that only one physical test device is available. Feature 017’s device-performance acceptance criterion therefore applies to the **Samsung SM-A566B (Android 16/API 36, arm64-v8a)** only. The result is device-specific; no second-device comparison is required or claimed.
- The temporary Java/ART benchmark already recorded above provides the single-device cold-load, lookup, package-fixture, and process-memory observations. Do not label its process PSS as incremental model memory or generalize it to other Android devices.
- Feature 025 is aligned to measure end-to-end results on this same single device. Existing plans to test multiple editor applications and device configurations are separate integration compatibility checks; they do not impose a second physical-device requirement.

### Limited local smoke test

- Downloaded one Google Books British-English 5-gram shard from the older `20090715` export: `googlebooks-eng-gb-all-5gram-20090715-400.csv.zip`.
- Compressed size: **13,946,203 bytes** (about 13.3 MiB); archive reports an expanded CSV size of **115,405,223 bytes** (about 110 MiB).
- Parsed the shard locally without network access after download. Of its data, 798,511 year rows met the prototype’s simple five-word alphabetic filter. Aggregating pre-2000 counts produced 15,558 distinct four-word contexts in a 610,304-byte SQLite scratch database.
- A temporal split (pre-2000 counts for ranking; 2000–2009 counts for testing) was recomputed over **all** eligible held-out records, including contexts absent from training. On this one shard it yielded 99.95% unique-context coverage, 99.64% top-three recall over all distinct held-out records (99.68% on covered records only), and 99.61% occurrence-weighted top-three recall. These are diagnostic implementation metrics only, not a valid quality estimate: the shard is a narrow lexical slice, train and test are the same source/time series, repeated phrases can leak across periods, and book language is not representative of conversational typing. Do not use these scores to approve a model.
- A Python in-memory dictionary microbenchmark on the host reported about 6.1 million simple lookups/second over this tiny extracted map. It excludes model loading, parsing/tokenization, a production index, memory measurement, Android hardware/runtime, and real request overhead; it is not a latency result for SeeMoreBoard.
- Built and installed a temporary Java-only Android APK harness on **Samsung SM-A566B, Android 16 (API 36), arm64-v8a**, with approximately 7.6 GB reported device RAM. The harness was not integrated with SeeMoreBoard and was uninstalled after measurement.
- The harness loaded a 15,457-context TSV map (391,970-byte UTF-8 payload) in **518–553 ms** across three cold process launches. It then ran 100,000 warmed hash-map lookups in **276 ms, 80.7 ms, and 82.7 ms**. The first lookup run is a warm-up outlier; later runs imply approximately 1.21–1.24 million lookups/second on this device for this small Java map.
- The temporary APK was **325,916 bytes**; the separate test fixture was 345,735 bytes. These figures describe the prototype package only and do not estimate a production model or required prediction data.
- Process PSS was **153.6–154.7 MiB** and reported Java heap use was about **21.6 MiB** at the measurement point. This includes the Android process/runtime and test harness; it does not isolate incremental model memory. Asset file-descriptor sizes were unavailable because the harness packaged/compressed text assets, but the extracted source payload sizes above are exact.
- Earlier incomplete harness output gave a different occurrence-weighted score because it did not report coverage and all-hit denominators consistently. It is superseded by the recomputed figures above and must not be used. Neither run is representative quality evidence.
- Scratch files were created under `/tmp/seemore-feature017.*`, not in the repository. No application code, dependencies, or production model were changed.

### Dictionary-only typo smoke test

- Downloaded the English (United Kingdom) Hunspell `index.dic`, `index.aff`, and full `license` notice from `wooorm/dictionaries` into `/tmp` for a temporary smoke test; all files were removed afterwards.
- The raw dictionary parsed as 48,299 entries. This is not the number of words after Hunspell affix expansion. The `.dic` payload is 552,106 bytes and `.aff` is 3,086 bytes (source payload totals only; not runtime index size).
- Exact membership checks found examples including `colour`, `organise`, `licence`, `license`, `accommodate`, `separate`, `necessary`, and `receive`. `definitely` was not in the simple raw-entry set; affix rules were not evaluated in this smoke test.
- A deliberately simple ordinary-Levenshtein ranker over raw entries found `accommodate`, `separate`, and `necessary` as minimum-edit suggestions for selected typos, but failed to rank `the` for `teh`, `receive` for `recieve`, and `definitely` for `definately`. This does not test Hunspell `REP` rules, affix expansion, or a production ranking engine. It shows that raw dictionary membership and basic edit distance alone are insufficient evidence of useful autocorrect.
- This manually chosen smoke list has no statistical representativeness and is not a false-positive/precision evaluation. The dictionary’s source licence notices still require review before any bundle is approved.

### Focused work to validate the recommendation

- Build a reproducible `en-GB` evaluation set separated into prediction prompts, correction cases, and hard-negative valid words. Until a representative corpus is approved, manually written/synthetic cases may test code behavior only; they cannot validate real-world quality.
- Compare unigram, bigram/trigram backoff, and the four-history 5-gram algorithm. Report coverage and top-1/top-3 recall over **all** held-out prompts, including unseen contexts, plus token-weighted results. Split by source/document, not only by year or individual n-gram rows.
- Compare raw edit-distance candidates with Hunspell-aware word/affix/`REP` candidates. Report intended correction availability and false-correction rate on correctly spelled and unknown inputs. Set and document the automatic-correction confidence threshold before enabling autocorrect.
- Run context caps at 32, 48, 64, and 96 Unicode code points against that evaluation set; select a cap only when the test data supports the decision. Current cap remains unset.
- On the single SM-A566B, measure startup/load, candidate latency, incremental memory if measurable, and packaged model/resource size. Label these measurements device-specific.
- Record whether n-gram quality is adequate. Only if it is not, evaluate one small neural alternative; do not add a generative LLM to the critical path without evidence it improves this narrow three-candidate task.

### Items explicitly not closed by this recommendation

- Representative, statistically meaningful prediction quality has not been established. The one-shard Google Books temporal test is diagnostic only and must not be used to approve the algorithm.
- The current character cap is not established.
- The chosen resource/model and its distribution terms are not cleared. Licensing is tracked separately from the algorithm recommendation and remains a release gate.
- Correction false-positive rate has not been measured; therefore the autocorrect threshold is not established.
- Android measurements exist only for the SM-A566B and a small Java map, not a production model or Hunspell runtime.
