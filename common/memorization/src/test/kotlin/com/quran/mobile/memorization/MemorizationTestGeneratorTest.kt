package com.quran.mobile.memorization

import com.google.common.truth.Truth.assertThat
import kotlin.random.Random
import org.junit.Test

class MemorizationTestGeneratorTest {

  private val verse = VerseText(sura = 1, ayah = 2, text = "الحمد لله رب العالمين")

  @Test
  fun `hide words tokenizes on whitespace`() {
    val question = MemorizationTestGenerator.generateHideWordsQuestion(verse, random = Random(1))
    assertThat(question.words).containsExactly("الحمد", "لله", "رب", "العالمين").inOrder()
  }

  @Test
  fun `hide words never hides every word when there is more than one`() {
    val question = MemorizationTestGenerator.generateHideWordsQuestion(
      verse,
      hideFraction = 1.0,
      random = Random(2)
    )
    assertThat(question.hiddenWordIndices.size).isLessThan(question.words.size)
  }

  @Test
  fun `hide words hides at least one word when there is more than one`() {
    val question = MemorizationTestGenerator.generateHideWordsQuestion(
      verse,
      hideFraction = 0.01,
      random = Random(3)
    )
    assertThat(question.hiddenWordIndices).isNotEmpty()
  }

  @Test
  fun `single word verse hides that one word`() {
    val single = VerseText(sura = 112, ayah = 1, text = "قل")
    val question = MemorizationTestGenerator.generateHideWordsQuestion(single, random = Random(4))
    assertThat(question.hiddenWordIndices).containsExactly(0)
  }

  @Test
  fun `fill in blank keeps only the anchor words visible`() {
    val question = MemorizationTestGenerator.generateFillInBlankQuestion(verse, anchorWordCount = 1)
    assertThat(question.hiddenWordIndices).containsExactly(1, 2, 3)
  }

  @Test
  fun `next ayah question carries the expected next verse`() {
    val next = VerseText(sura = 1, ayah = 3, text = "الرحمن الرحيم")
    val question = MemorizationTestGenerator.generateNextAyahQuestion(verse, next)
    assertThat(question.mode).isEqualTo(MemorizationMode.NEXT_AYAH_RECALL)
    assertThat(question.expectedNextAyah).isEqualTo(next)
    assertThat(question.hiddenWordIndices).isEmpty()
  }

  @Test
  fun `prompt words replaces hidden indices with a placeholder`() {
    val question = MemorizationTestGenerator.generateFillInBlankQuestion(verse, anchorWordCount = 2)
    assertThat(question.promptWords()).containsExactly("الحمد", "لله", "____", "____").inOrder()
  }
}
