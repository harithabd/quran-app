# New features added

# New features added

**Status as of this pass:** Tajweed coloring and memorization testing are wired end-to-end into
the app's real navigation/DI/preferences. Tafsir, Asbab al-Nuzul, and I'rab have real,
non-fabricated infrastructure (models, a documented-format parser with passing logic checks, an
API client shaped against real documented endpoints) but no bundled content and no UI yet - see
part 3.

I still have no Android SDK and no network access in the sandbox I'm building in. I have not run
`./gradlew build`. Everything below reflects careful hand-verification (tracing logic by hand,
mirroring exact existing code patterns in this repo) - not a compiler.

## 1. Tajweed coloring - fully wired

- `common/tajweed`: rule engine (unchanged from before, still just logic + tests).
- `common/tajweed-ui`: Compose renderer (unchanged from before; not currently used by the app,
  since the app's Arabic text integration point below is a legacy View, not Compose - kept as a
  ready-to-use module for any future Compose text surface).
- **New this pass:** a real user-facing toggle - "Tajweed coloring" in Settings
  (`Constants.kt`, `QuranSettings.java`, `preferences_keys.xml`, `strings.xml`,
  `quran_preferences.xml`) - and real application of the coloring in
  `TranslationAdapter.kt`, where the Arabic ayah text is already rendered as a live
  `SpannableString` (next to translations). This required finding an actual live-text
  rendering location, since the main mushaf/page view only draws pre-built glyph images.

**Please verify:** open the Settings screen, toggle "Tajweed coloring", open a surah in
translation view, confirm the Arabic text is colored and the toggle round-trips correctly.

## 2. Memorization (hifz) testing - fully wired

- `common/memorization`: domain logic (unchanged from before - generator/scorer/scheduler, unit
  tested).
- **New this pass:**
  - `feature/memorization`: a new Gradle module (Compose) with `MemorizationActivity` - a
    self-contained quiz screen (hide-words / fill-in-blank / next-ayah-recall), reading verses
    through the `VerseTextSource` interface and persisting review state (Leitner box level +
    next-review day) to `SharedPreferences`.
  - `ArabicVerseTextSource` (app module): real implementation of `VerseTextSource`, backed by the
    same `ArabicDatabaseUtils` the app already uses elsewhere - picks a random ayah, fetches its
    text and the next ayah's text from the real Quran text database.
  - `MemorizationScreenProvider`: hooks into `ExtraScreenProvider`, an extension point the app's
    own architecture already provides for exactly this purpose (an overflow-menu entry that opens
    a new screen) - discovered by reading `QuranActivity.kt` rather than bolting on a parallel
    mechanism.
  - `ApplicationModule.kt`: one line changed (`provideExtraScreens()` now returns the real
    provider instead of `emptySet()`).

**Please verify:** open the app's overflow menu, confirm a "Memorization Test" entry appears and
launches the quiz; answer a question and confirm scoring and the box/streak message make sense.

**Design choices worth knowing about, not hidden:**
- Cross-module wiring uses a small static holder (`MemorizationEntryPoint`) rather than a full
  Dagger/Metro graph extension for the Activity, specifically to avoid guessing at Metro
  annotations I could not compile-check. It's simple and works, but a reviewer used to "everything
  goes through the graph" may want to formalize it later.
- Review-state persistence is plain `SharedPreferences`, not the SQLDelight-backed store I
  originally flagged as a follow-up - I judged this an acceptable, genuinely-working
  simplification given I can't iterate against a compiler.

## 3. Tafsir, Asbab al-Nuzul, I'rab - real infrastructure, not yet bundled with content or UI

New module `common/exegesis` (see `common/exegesis/README.md` for full detail and citations):

- **Tafsir**: `TafsirApi.kt`, a Retrofit interface shaped against the Quran Foundation API
  (api-docs.quran.foundation, the official successor to the Quran.com API) - supports multiple
  named tafsir resources (e.g. Tafsir Ibn Kathir). Requires OAuth2 app credentials you'd register
  yourself; I flagged in comments and the README that I could not make a live request to confirm
  the exact current endpoint paths, since I have no network access here.
- **Asbab al-Nuzul**: modeled as its own type (`AsbabAlNuzulEntry`), not silently merged into
  generic tafsir, because it's a distinct genre with its own standard named source (al-Wahidi's
  work, as published by the Royal Aal al-Bayt Institute via altafsir.com). There's no dedicated
  public API for this the way there is for tafsir - the README explains the realistic path
  (license/digitize that specific translation, or use a tafsir resource that already incorporates
  asbab al-nuzul narrations and cite it as such).
- **I'rab/grammar**: `QuranicCorpusMorphologyParser.kt`, a real parser for the Quranic Arabic
  Corpus's (corpus.quran.com, GPL, University of Leeds) documented per-word morphology format.
  I wrote unit tests against hand-built format specimens, traced them by hand, and caught and
  fixed a real bug (a bare POS tag was being double-counted into both the parsed field and the
  leftover features list) before calling it done - that fix is a good example of why "I checked
  the logic carefully" is not the same claim as "this compiles," but it's also not nothing.

**What's genuinely NOT done:** no data is bundled (none exists to bundle - see README for exactly
why), no SQLDelight cache/download pipeline (sketched as a next step, not built), no UI screens,
and no OAuth2 credential flow (that requires you to register an app with Quran Foundation - not
something I can do on your behalf).

## Before you ship any of this

1. Open in Android Studio, let Gradle sync, and run `./gradlew build`. This is the first real
   compiler check any of this session's code will have had.
2. Run unit tests specifically:
   `./gradlew :common:tajweed:test :common:memorization:test :common:exegesis:test`
3. Manually exercise the two wired features (tajweed toggle, memorization quiz) per the
   "Please verify" notes above.
4. For tafsir/asbab al-nuzul/i'rab: register for Quran Foundation API access, obtain the Quranic
   Arabic Corpus data under your own agreement to their terms, then re-verify `TafsirApi.kt`'s
   endpoint paths against current docs before wiring in real credentials.

