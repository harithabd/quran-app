package com.quran.mobile.memorization

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class MemorizationScorerTest {

  private val verse = VerseText(sura = 1, ayah = 2, text = "الحمد لله رب العالمين")

  @Test
  fun `all correct answers yield a perfect result`() {
    val question = MemorizationTestGenerator.generateFillInBlankQuestion(verse, anchorWordCount = 2)
    val result = MemorizationScorer.scoreHiddenWords(question, listOf("رب", "العالمين"))
    assertThat(result.isPerfect).isTrue()
    assertThat(result.correctBlanks).isEqualTo(2)
    assertThat(result.totalBlanks).isEqualTo(2)
  }

  @Test
  fun `comparison ignores diacritics`() {
    val diacritized = VerseText(sura = 1, ayah = 2, text = "اَلْحَمْدُ لِلَّهِ رَبِّ الْعَالَمِينَ")
    val question = MemorizationTestGenerator.generateFillInBlankQuestion(diacritized, anchorWordCount = 3)
    // user types the last word with no diacritics at all
    val result = MemorizationScorer.scoreHiddenWords(question, listOf("العالمين"))
    assertThat(result.isPerfect).isTrue()
  }

  @Test
  fun `wrong answer is recorded as incorrect`() {
    val question = MemorizationTestGenerator.generateFillInBlankQuestion(verse, anchorWordCount = 3)
    val result = MemorizationScorer.scoreHiddenWords(question, listOf("wrong"))
    assertThat(result.isPerfect).isFalse()
    assertThat(result.correctBlanks).isEqualTo(0)
    assertThat(result.incorrectWordIndices).containsExactly(3)
  }

  @Test
  fun `missing answer counts as incorrect rather than throwing`() {
    val question = MemorizationTestGenerator.generateFillInBlankQuestion(verse, anchorWordCount = 2)
    val result = MemorizationScorer.scoreHiddenWords(question, emptyList())
    assertThat(result.correctBlanks).isEqualTo(0)
    assertThat(result.totalBlanks).isEqualTo(2)
  }

  @Test
  fun `next ayah scoring matches on sura and ayah`() {
    val next = VerseText(sura = 1, ayah = 3, text = "الرحمن الرحيم")
    val question = MemorizationTestGenerator.generateNextAyahQuestion(verse, next)

    val correct = MemorizationScorer.scoreNextAyah(question, userSura = 1, userAyah = 3)
    assertThat(correct.isPerfect).isTrue()

    val incorrect = MemorizationScorer.scoreNextAyah(question, userSura = 1, userAyah = 4)
    assertThat(incorrect.isPerfect).isFalse()
  }
}
