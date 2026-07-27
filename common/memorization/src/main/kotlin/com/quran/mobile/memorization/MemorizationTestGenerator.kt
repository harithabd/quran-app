package com.quran.mobile.memorization

import kotlin.math.max
import kotlin.math.roundToInt
import kotlin.random.Random

object MemorizationTestGenerator {

  /**
   * Hides a fraction of the words in [verse] for the user to recall.
   *
   * @param hideFraction target fraction of words to hide, clamped to leave at least one word
   * visible when the ayah has more than one word.
   */
  fun generateHideWordsQuestion(
    verse: VerseText,
    hideFraction: Double = 0.35,
    random: Random = Random.Default
  ): MemorizationQuestion {
    val words = tokenize(verse.text)
    val hiddenCount = if (words.size <= 1) {
      words.size
    } else {
      max(1, (words.size * hideFraction).roundToInt()).coerceAtMost(words.size - 1)
    }
    val hiddenIndices = words.indices.toMutableList().shuffled(random).take(hiddenCount).toSet()
    return MemorizationQuestion(
      sura = verse.sura,
      ayah = verse.ayah,
      mode = MemorizationMode.HIDE_WORDS,
      words = words,
      hiddenWordIndices = hiddenIndices
    )
  }

  /**
   * Hides everything except the first [anchorWordCount] words, requiring full recall of the
   * rest of the ayah - a harder mode than [generateHideWordsQuestion].
   */
  fun generateFillInBlankQuestion(
    verse: VerseText,
    anchorWordCount: Int = 2
  ): MemorizationQuestion {
    val words = tokenize(verse.text)
    val anchors = anchorWordCount.coerceIn(0, words.size)
    val hiddenIndices = (anchors until words.size).toSet()
    return MemorizationQuestion(
      sura = verse.sura,
      ayah = verse.ayah,
      mode = MemorizationMode.FILL_IN_BLANK,
      words = words,
      hiddenWordIndices = hiddenIndices
    )
  }

  /** Given the current ayah, tests whether the user can recall the next one. */
  fun generateNextAyahQuestion(current: VerseText, next: VerseText): MemorizationQuestion {
    return MemorizationQuestion(
      sura = current.sura,
      ayah = current.ayah,
      mode = MemorizationMode.NEXT_AYAH_RECALL,
      words = tokenize(current.text),
      hiddenWordIndices = emptySet(),
      expectedNextAyah = next
    )
  }

  private fun tokenize(text: String): List<String> =
    text.trim().split(Regex("\\s+")).filter { it.isNotBlank() }
}
