package com.quran.mobile.exegesis

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class QuranicCorpusMorphologyParserTest {

  @Test
  fun `parses a segment with explicit POS, lemma, and root tags`() {
    val line = "(6:76:7:1)\tqaAla\tSTEM\tPOS:V PERF LEM:qaAla ROOT:qwl 3MS"
    val segment = QuranicCorpusMorphologyParser.parseLine(line)

    assertThat(segment).isNotNull()
    segment!!
    assertThat(segment.sura).isEqualTo(6)
    assertThat(segment.ayah).isEqualTo(76)
    assertThat(segment.wordPosition).isEqualTo(7)
    assertThat(segment.segmentPosition).isEqualTo(1)
    assertThat(segment.transliteration).isEqualTo("qaAla")
    assertThat(segment.partOfSpeech).isEqualTo(PartOfSpeech.VERB)
    assertThat(segment.lemma).isEqualTo("qaAla")
    assertThat(segment.root).isEqualTo("qwl")
    assertThat(segment.features).containsExactly("STEM", "PERF", "3MS")
  }

  @Test
  fun `parses a pronoun suffix segment with no explicit POS colon tag`() {
    val line = "(6:76:9:2)\tY\tPRON\tSUFFIX PRON:1S"
    val segment = QuranicCorpusMorphologyParser.parseLine(line)

    assertThat(segment).isNotNull()
    segment!!
    assertThat(segment.wordPosition).isEqualTo(9)
    assertThat(segment.segmentPosition).isEqualTo(2)
    assertThat(segment.partOfSpeech).isEqualTo(PartOfSpeech.PRONOUN)
    assertThat(segment.features).contains("SUFFIX")
  }

  @Test
  fun `flattens pipe-separated feature tokens`() {
    val line = "(1:1:1:1)\tbi\tP\tPREFIX|bi+"
    val segment = QuranicCorpusMorphologyParser.parseLine(line)

    assertThat(segment).isNotNull()
    segment!!
    assertThat(segment.partOfSpeech).isEqualTo(PartOfSpeech.PREPOSITION)
    assertThat(segment.features).containsExactly("PREFIX", "bi+")
  }

  @Test
  fun `unrecognized POS tag maps to UNKNOWN rather than guessing`() {
    val line = "(2:5:3:1)\txyz\tWEIRDTAG\tSOMETHING"
    val segment = QuranicCorpusMorphologyParser.parseLine(line)

    assertThat(segment).isNotNull()
    assertThat(segment!!.partOfSpeech).isEqualTo(PartOfSpeech.UNKNOWN)
  }

  @Test
  fun `malformed location token returns null`() {
    assertThat(QuranicCorpusMorphologyParser.parseLine("not-a-location\tx\tN\tFOO")).isNull()
  }

  @Test
  fun `line with too few tokens returns null`() {
    assertThat(QuranicCorpusMorphologyParser.parseLine("(1:1:1:1)\tonly")).isNull()
  }

  @Test
  fun `parse over multiple lines skips comments blanks and malformed lines`() {
    val text = """
      # Quranic Arabic Corpus (morphology, version 0.4)
      # Copyright (C) 2011 Kais Dukes

      (1:1:1:1)	bi	P	PREFIX|bi+
      not-a-valid-line
      (1:1:3:1)	{ll~ah	PN	LEM:{ll~ah
    """.trimIndent().lines()

    val result = QuranicCorpusMorphologyParser.parse(text.asSequence())

    assertThat(result.segments).hasSize(2)
    assertThat(result.skippedLineCount).isEqualTo(1)
    assertThat(result.segments[1].partOfSpeech).isEqualTo(PartOfSpeech.PROPER_NOUN)
    assertThat(result.segments[1].lemma).isEqualTo("{ll~ah")
  }
}
