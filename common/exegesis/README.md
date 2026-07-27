# common/exegesis

Client-side infrastructure for three features: multiple tafsir interpretations, asbab al-nuzul
(reasons for revelation), and i'rab/grammar (word-by-word morphology). **No Quranic scholarship
content is bundled in this module.** Everything here is models, a documented parser, and a
network client interface pointed at real, named, external sources - the same architecture the
app already uses for translations (`common/translation`: downloads real content from a server at
runtime rather than shipping it in the APK).

## Why no content is bundled

1. I built this without network access, so I could not fetch any real tafsir/asbab al-nuzul/
   morphology data into this sandbox even if I wanted to.
2. Even with network access, bulk-reproducing licensed tafsir texts or the (GPL, verbatim-only)
   Quranic Arabic Corpus data through an AI chat tool would not be an appropriate way to populate
   a data file - it belongs in a proper ingestion pipeline talking to the real source, with its
   real license terms respected, not typed out from a chat response.

## Tafsir - Quran Foundation API (formerly the Quran.com API)

- Docs: https://api-docs.quran.foundation/
- Multiple named tafsir resources are available per verse (e.g. "Tafsir Ibn Kathir", resource id
  169 in their catalog at the time this was written - confirm current ids via
  `GET /resources/tafsirs`).
- Requires OAuth2 app credentials ("Request Access" in their docs portal) - there is no
  anonymous/keyless tier for this endpoint as of this writing.
- `TafsirApi.kt` models the documented request/response shape. **Endpoint paths and the OAuth2
  token flow should be re-verified against the current docs before shipping** - hosted API
  surfaces change, and I could not make a live request to confirm from this sandbox.

## Asbab al-Nuzul

There is no equivalent dedicated public API. The standard reference work is al-Wahidi's
*Asbab al-Nuzul*, published in English translation by the Royal Aal al-Bayt Institute for Islamic
Thought (Amman, Jordan) via their "Great Tafsirs of the Holy Qur'an" project at altafsir.com
(translation by Mokrane Guezzou). This module models asbab al-nuzul entries as their own type
(`AsbabAlNuzulEntry`, distinct from `TafsirEntry`) rather than silently folding them into generic
tafsir, since it's a distinct genre with its own named scholarly source. Populating this in
practice means either licensing/digitizing that specific published translation, or using a tafsir
resource (from the API above) that already incorporates asbab al-nuzul narrations inline and
citing it as such - don't relabel general tafsir commentary as asbab al-nuzul.

## I'rab / grammar - the Quranic Arabic Corpus

- Source: https://corpus.quran.com (Kais Dukes, University of Leeds), 77,430 words, GPL-licensed.
- Download requires submitting a contact email at https://corpus.quran.com/download/ - this
  module does not and cannot auto-download it.
- **License terms are specific: verbatim redistribution is permitted, but the data may not be
  altered, and any use must credit the Quranic Arabic Corpus with a link to corpus.quran.com.**
  Respect this - don't reformat away the attribution.
- `QuranicCorpusMorphologyParser.kt` is a real parser for their documented per-segment text
  format (e.g. `(6:76:7:1) qaAla | STEM POS:V PERF LEM:qaAla ROOT:qwl 3MS`), verified against the
  format examples published in their own documentation and in academic descriptions of the
  corpus. It has real unit tests. Once you have the actual data file (after requesting it from
  corpus.quran.com under your own agreement to their terms), point the parser at it.

## What's left to build

- The SQLDelight-backed local cache (mirroring `common/translation`'s download-and-cache
  pattern) - schema is sketched in `ExegesisModels.kt` but not wired to actual disk storage yet.
- OAuth2 client credential flow for the Quran Foundation API (needs a registered app - that's an
  account you'd create, not something I can do on your behalf).
- UI screens consuming this data (none exist yet - this PR is infrastructure only).
