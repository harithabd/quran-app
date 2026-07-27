package com.quran.mobile.tajweed

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class TajweedAnalyzerTest {

  // Base letters
  private val hamza = '\u0621'
  private val alef = '\u0627'
  private val ba = '\u0628'
  private val ta = '\u062A' // ikhfa letter sample
  private val lam = '\u0644'
  private val meem = '\u0645'
  private val noon = '\u0646'
  private val waw = '\u0648'
  private val ya = '\u064A'
  private val qaf = '\u0642'

  // Diacritics
  private val fathatan = '\u064B'
  private val fatha = '\u064E'
  private val shadda = '\u0651'
  private val sukun = '\u0652'

  private fun ruleNames(text: String) = TajweedAnalyzer.analyze(text).map {
    Triple(it.startIndex, it.endIndex, it.rule)
  }

  @Test
  fun `noon or meem with shaddah is ghunnah`() {
    val text = "$noon$shadda$fatha"
    assertThat(ruleNames(text)).containsExactly(Triple(0, 2, TajweedRule.GHUNNAH))
  }

  @Test
  fun `noon sakinah before ya across word boundary is idgham with ghunnah`() {
    val text = "$noon$sukun $ya$fatha"
    assertThat(ruleNames(text)).containsExactly(Triple(0, 2, TajweedRule.IDGHAM_WITH_GHUNNAH))
  }

  @Test
  fun `noon sakinah before lam across word boundary is idgham without ghunnah`() {
    val text = "$noon$sukun $lam$fatha"
    assertThat(ruleNames(text)).containsExactly(Triple(0, 2, TajweedRule.IDGHAM_WITHOUT_GHUNNAH))
  }

  @Test
  fun `noon sakinah before an ikhfa letter is ikhfa`() {
    val text = "$noon$sukun $ta$fatha"
    assertThat(ruleNames(text)).containsExactly(Triple(0, 2, TajweedRule.IKHFA))
  }

  @Test
  fun `noon sakinah before ba in the same word is iqlab`() {
    val text = "$noon$sukun$ba$fatha"
    assertThat(ruleNames(text)).containsExactly(Triple(0, 2, TajweedRule.IQLAB))
  }

  @Test
  fun `noon sakinah before a throat letter is izhar and produces no span`() {
    val text = "$noon$sukun $hamza$fatha"
    assertThat(ruleNames(text)).isEmpty()
  }

  @Test
  fun `noon sakinah before ya within the same word never triggers idgham`() {
    // Mirrors real exception words (e.g. within-word noon-ya/waw sequences): idgham requires
    // crossing a word boundary, so a same-word match yields no rule rather than a wrong one.
    val text = "$noon$sukun$ya$fatha"
    assertThat(ruleNames(text)).isEmpty()
  }

  @Test
  fun `tanween before an ikhfa letter is ikhfa`() {
    val text = "$ba$fathatan $ta$fatha"
    assertThat(ruleNames(text)).containsExactly(Triple(1, 2, TajweedRule.IKHFA))
  }

  @Test
  fun `meem sakinah before meem is idgham shafawi`() {
    val text = "$meem$sukun $meem$fatha"
    assertThat(ruleNames(text)).containsExactly(Triple(0, 2, TajweedRule.IDGHAM_SHAFAWI))
  }

  @Test
  fun `meem sakinah before ba is ikhfa shafawi`() {
    val text = "$meem$sukun $ba$fatha"
    assertThat(ruleNames(text)).containsExactly(Triple(0, 2, TajweedRule.IKHFA_SHAFAWI))
  }

  @Test
  fun `meem sakinah before other letters is izhar shafawi and produces no span`() {
    val text = "$meem$sukun $lam$fatha"
    assertThat(ruleNames(text)).isEmpty()
  }

  @Test
  fun `qalqalah letter with sukun is qalqalah`() {
    val text = "$ba$fatha$qaf$sukun"
    assertThat(ruleNames(text)).containsExactly(Triple(2, 4, TajweedRule.QALQALAH))
  }

  @Test
  fun `al followed by a shaddah-marked sun letter is lam shamsiyyah`() {
    val text = "$alef$lam$noon$shadda$fatha"
    assertThat(ruleNames(text)).containsExactly(
      Triple(0, 2, TajweedRule.LAM_SHAMSIYYAH),
      Triple(2, 4, TajweedRule.GHUNNAH)
    )
  }

  @Test
  fun `madd letter with nothing special following is natural madd`() {
    val text = "$ba$fatha$alef $meem$fatha"
    assertThat(ruleNames(text)).containsExactly(Triple(2, 3, TajweedRule.MADD_NATURAL))
  }

  @Test
  fun `madd letter followed by hamzah in the same word is madd muttasil`() {
    val text = "$ba$fatha$alef$hamza"
    assertThat(ruleNames(text)).containsExactly(Triple(2, 3, TajweedRule.MADD_MUTTASIL))
  }

  @Test
  fun `madd letter at word end before a hamzah-initial word is madd munfasil`() {
    val text = "$ba$fatha$alef $hamza$fatha"
    assertThat(ruleNames(text)).containsExactly(Triple(2, 3, TajweedRule.MADD_MUNFASIL))
  }

  @Test
  fun `madd letter followed by a sakin letter in the same word is madd lazim`() {
    val text = "$ba$fatha$alef$lam$sukun"
    assertThat(ruleNames(text)).containsExactly(Triple(2, 3, TajweedRule.MADD_LAZIM))
  }

  @Test
  fun `plain text with no diacritics produces no spans`() {
    assertThat(TajweedAnalyzer.analyze("$alef$ba$ta")).isEmpty()
  }

  @Test
  fun `empty string produces no spans`() {
    assertThat(TajweedAnalyzer.analyze("")).isEmpty()
  }
}
