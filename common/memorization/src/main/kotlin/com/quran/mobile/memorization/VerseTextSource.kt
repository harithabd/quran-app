package com.quran.mobile.memorization

/** The verse currently being tested, and the following verse if one exists (null at the very end of the Quran). */
data class VerseQuestionData(val current: VerseText, val next: VerseText?)

/**
 * Supplies verse text for memorization questions. Implemented in the app module (which has
 * access to the actual Quran text database) and consumed by the `feature:memorization` UI
 * module, which otherwise has no knowledge of how verse text is stored or fetched.
 */
interface VerseTextSource {
  /** Picks a verse (implementation-defined selection) and returns it plus the following verse. */
  suspend fun randomQuestionVerses(): VerseQuestionData
}
