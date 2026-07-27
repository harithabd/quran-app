package com.quran.mobile.memorization

/** A single verse's text, independent of any particular data/storage layer. */
data class VerseText(val sura: Int, val ayah: Int, val text: String)

enum class MemorizationMode {
  /** Some words in the ayah are hidden; the user must recall and type them. */
  HIDE_WORDS,

  /** The whole ayah is hidden except for a few anchor words; the user recalls the rest. */
  FILL_IN_BLANK,

  /** Given one ayah, the user must recall/select the next ayah in sequence. */
  NEXT_AYAH_RECALL
}

/**
 * A single memorization test question.
 *
 * @property words the ayah tokenized by whitespace, in order.
 * @property hiddenWordIndices indices into [words] that are hidden and must be recalled. Empty
 * for [MemorizationMode.NEXT_AYAH_RECALL], where [expectedNextAyah] is checked instead.
 * @property expectedNextAyah only set for [MemorizationMode.NEXT_AYAH_RECALL].
 */
data class MemorizationQuestion(
  val sura: Int,
  val ayah: Int,
  val mode: MemorizationMode,
  val words: List<String>,
  val hiddenWordIndices: Set<Int>,
  val expectedNextAyah: VerseText? = null
) {
  /** [words] with hidden positions replaced by a blank placeholder, for display. */
  fun promptWords(blankPlaceholder: String = "____"): List<String> =
    words.mapIndexed { index, word -> if (index in hiddenWordIndices) blankPlaceholder else word }
}

data class MemorizationResult(
  val sura: Int,
  val ayah: Int,
  val mode: MemorizationMode,
  val totalBlanks: Int,
  val correctBlanks: Int,
  val incorrectWordIndices: Set<Int>
) {
  val isPerfect: Boolean get() = totalBlanks > 0 && correctBlanks == totalBlanks
  val accuracy: Double get() = if (totalBlanks == 0) 0.0 else correctBlanks.toDouble() / totalBlanks
}
