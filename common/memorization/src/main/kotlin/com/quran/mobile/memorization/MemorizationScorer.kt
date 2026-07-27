package com.quran.mobile.memorization

object MemorizationScorer {

  /**
   * Scores [userWords] (one entry per hidden blank, in the same order as
   * [MemorizationQuestion.hiddenWordIndices] sorted ascending) against [question].
   *
   * Comparison ignores Arabic diacritics (harakat) and tatweel, since users typically type
   * without them, but does not attempt any deeper linguistic normalization (e.g. hamza-seat
   * variants are treated as distinct).
   */
  fun scoreHiddenWords(question: MemorizationQuestion, userWords: List<String>): MemorizationResult {
    val hiddenIndices = question.hiddenWordIndices.sorted()
    var correct = 0
    val incorrect = mutableSetOf<Int>()
    hiddenIndices.forEachIndexed { position, wordIndex ->
      val expected = normalize(question.words[wordIndex])
      val actual = userWords.getOrNull(position)?.let { normalize(it) } ?: ""
      if (expected.isNotEmpty() && expected == actual) {
        correct++
      } else {
        incorrect += wordIndex
      }
    }
    return MemorizationResult(
      sura = question.sura,
      ayah = question.ayah,
      mode = question.mode,
      totalBlanks = hiddenIndices.size,
      correctBlanks = correct,
      incorrectWordIndices = incorrect
    )
  }

  /** Scores a [MemorizationMode.NEXT_AYAH_RECALL] answer. */
  fun scoreNextAyah(question: MemorizationQuestion, userSura: Int, userAyah: Int): MemorizationResult {
    val expected = question.expectedNextAyah
    val isCorrect = expected != null && expected.sura == userSura && expected.ayah == userAyah
    return MemorizationResult(
      sura = question.sura,
      ayah = question.ayah,
      mode = question.mode,
      totalBlanks = 1,
      correctBlanks = if (isCorrect) 1 else 0,
      incorrectWordIndices = emptySet()
    )
  }

  private fun normalize(word: String): String {
    val stripped = buildString {
      for (c in word.trim()) {
        if (c == TATWEEL) continue
        if (Character.getType(c) == Character.NON_SPACING_MARK.toInt()) continue
        append(c)
      }
    }
    return stripped
  }

  private const val TATWEEL = '\u0640'
}
