package com.quran.mobile.exegesis

/**
 * Parses the Quranic Arabic Corpus (corpus.quran.com) morphology data format into
 * [MorphologicalSegment]s. This does not download or embed any corpus data - point it at a file
 * you've obtained directly from https://corpus.quran.com/download/ under their terms. See the
 * module README for licensing (GPL, verbatim redistribution only, attribution required).
 *
 * Expected line format (whitespace/tab separated), one morphological segment per line:
 * ```
 * (sura:ayah:word:segment)<TAB>transliteration<TAB>TAG<TAB>FEATURE1 FEATURE2:VALUE ...
 * ```
 * for example (a single line shown purely as a format specimen, not corpus content):
 * ```
 * (1:1:1:1)	bi	P	PREFIX|bi+
 * (1:1:3:1)	{ll~ah	PN	LEM:{ll~ah
 * ```
 * Lines starting with `#` (the corpus's license header block) and blank lines are skipped.
 * Lines that don't match the expected shape are skipped rather than throwing, since a morphology
 * file is large and a handful of malformed/unexpected lines shouldn't abort the whole parse -
 * callers can inspect [ParseResult.skippedLineCount] to see how many were skipped.
 */
object QuranicCorpusMorphologyParser {

  private val LOCATION_REGEX = Regex("""^\((\d+):(\d+):(\d+):(\d+)\)$""")

  // Maps the corpus's documented POS tag abbreviations onto our (smaller) PartOfSpeech enum.
  // Unlisted/unrecognized tags map to UNKNOWN rather than a guess.
  private val POS_TAG_MAP: Map<String, PartOfSpeech> = mapOf(
    "N" to PartOfSpeech.NOUN,
    "PN" to PartOfSpeech.PROPER_NOUN,
    "V" to PartOfSpeech.VERB,
    "ADJ" to PartOfSpeech.ADJECTIVE,
    "DEM" to PartOfSpeech.DEMONSTRATIVE,
    "REL" to PartOfSpeech.RELATIVE_PRONOUN,
    "PRON" to PartOfSpeech.PRONOUN,
    "P" to PartOfSpeech.PREPOSITION,
    "CONJ" to PartOfSpeech.CONJUNCTION,
    "SUB" to PartOfSpeech.CONJUNCTION,
    "NEG" to PartOfSpeech.NEGATIVE_PARTICLE,
    "VOC" to PartOfSpeech.VOCATIVE_PARTICLE
  )

  data class ParseResult(val segments: List<MorphologicalSegment>, val skippedLineCount: Int)

  fun parse(lines: Sequence<String>): ParseResult {
    val segments = mutableListOf<MorphologicalSegment>()
    var skipped = 0
    for (line in lines) {
      val trimmed = line.trim()
      if (trimmed.isEmpty() || trimmed.startsWith("#")) continue
      val segment = parseLine(trimmed)
      if (segment != null) {
        segments += segment
      } else {
        skipped++
      }
    }
    return ParseResult(segments, skipped)
  }

  fun parseLine(line: String): MorphologicalSegment? {
    val tokens = line.split(Regex("\\s+"))
    if (tokens.size < 3) return null

    val locationMatch = LOCATION_REGEX.find(tokens[0]) ?: return null
    val (sura, ayah, word, segment) = locationMatch.destructured

    val transliteration = tokens[1]
    val remainder = tokens.drop(2)

    // Feature tokens may themselves be pipe-separated (e.g. "PREFIX|bi+"); flatten them.
    val featureTokens = remainder.flatMap { it.split("|") }.filter { it.isNotBlank() }

    // The corpus sometimes puts the POS tag as a bare leading token (no "POS:" prefix) rather
    // than inside the feature list - if so, consume it here so it isn't also left in [features].
    var posTag: String? = null
    val startIndex: Int
    if (featureTokens.isNotEmpty() && !featureTokens[0].contains(":") && POS_TAG_MAP.containsKey(featureTokens[0])) {
      posTag = featureTokens[0]
      startIndex = 1
    } else {
      startIndex = 0
    }

    var lemma: String? = null
    var root: String? = null
    val rawFeatures = mutableListOf<String>()

    for (i in startIndex until featureTokens.size) {
      val token = featureTokens[i]
      when {
        token.startsWith("POS:") -> posTag = token.removePrefix("POS:")
        token.startsWith("LEM:") -> lemma = token.removePrefix("LEM:")
        token.startsWith("ROOT:") -> root = token.removePrefix("ROOT:")
        else -> rawFeatures += token
      }
    }

    return MorphologicalSegment(
      sura = sura.toInt(),
      ayah = ayah.toInt(),
      wordPosition = word.toInt(),
      segmentPosition = segment.toInt(),
      transliteration = transliteration,
      partOfSpeech = POS_TAG_MAP[posTag] ?: PartOfSpeech.UNKNOWN,
      lemma = lemma,
      root = root,
      features = rawFeatures
    )
  }
}
