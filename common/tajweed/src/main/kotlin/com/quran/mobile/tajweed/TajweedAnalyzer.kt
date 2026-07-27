package com.quran.mobile.tajweed

/**
 * Detects standard tajweed (recitation) rules in a string of Uthmani Quranic text and returns
 * them as [TajweedSpan]s that a UI layer can use to color/annotate the text.
 *
 * ## What this is
 * A deterministic, rule-based (not machine-learned, not scholarly-curated) scan of the text
 * implementing the noon-sakinah/tanween family of rules, the meem-sakinah family, ghunnah,
 * qalqalah, lam shamsiyyah, and madd. The rules themselves follow standard tajweed texts.
 *
 * ## What this is NOT
 * This is not a substitute for a scholar-reviewed, professionally color-coded mushaf. In
 * particular:
 * - Madd sub-type (muttasil/munfasil/lazim vs. natural) and qalqalah kubra genuinely depend on
 *   context this analyzer cannot see from bare text alone (e.g. where a reciter chooses to
 *   pause). Those spans are marked [TajweedConfidence.HEURISTIC] rather than asserted as fact.
 * - Word boundaries are detected via whitespace only; this analyzer has no morphological
 *   awareness beyond that.
 * - A handful of rarer rules (e.g. madd al-leen, specific idghams with a following shaddah on
 *   a repeated letter) are intentionally not implemented rather than guessed at.
 *
 * Treat the output as a helpful visual aid, and encourage users to still learn tajweed from a
 * qualified teacher.
 */
object TajweedAnalyzer {

  // Base Arabic letters (consonants + alef/waw/ya as letters).
  private const val HAMZA = '\u0621'
  private const val ALEF_MADDA = '\u0622'
  private const val ALEF_HAMZA_ABOVE = '\u0623'
  private const val WAW_HAMZA = '\u0624'
  private const val ALEF_HAMZA_BELOW = '\u0625'
  private const val YA_HAMZA = '\u0626'
  private const val ALEF = '\u0627'
  private const val BA = '\u0628'
  private const val TA_MARBUTA = '\u0629'
  private const val LAM = '\u0644'
  private const val MEEM = '\u0645'
  private const val NOON = '\u0646'
  private const val WAW = '\u0648'
  private const val YA = '\u064A'
  private const val HAMZAT_WASL = '\u0671'
  private const val SUPERSCRIPT_ALEF = '\u0670'
  private const val TATWEEL = '\u0640'

  // Diacritics (harakat and related marks).
  private const val FATHATAN = '\u064B'
  private const val DAMMATAN = '\u064C'
  private const val KASRATAN = '\u064D'
  private const val FATHA = '\u064E'
  private const val DAMMA = '\u064F'
  private const val KASRA = '\u0650'
  private const val SHADDA = '\u0651'
  private const val SUKUN = '\u0652'

  private const val IKHFA_LETTERS = "\u062A\u062B\u062C\u062F\u0630\u0632\u0633\u0634\u0635\u0636\u0637\u0638\u0641\u0642\u0643" // ت ث ج د ذ ز س ش ص ض ط ظ ف ق ك
  private const val IDGHAM_GHUNNAH_LETTERS = "\u064A\u0646\u0645\u0648" // ي ن م و
  private const val IDGHAM_NO_GHUNNAH_LETTERS = "\u0644\u0631" // ل ر
  private const val IZHAR_HALQI_LETTERS = "\u0621\u0647\u0639\u062D\u063A\u062E" // ء ه ع ح غ خ
  private const val QALQALAH_LETTERS = "\u0642\u0637\u0628\u062C\u062F" // ق ط ب ج د
  private const val SUN_LETTERS = "\u062A\u062B\u062F\u0630\u0631\u0632\u0633\u0634\u0635\u0636\u0637\u0638\u0644\u0646" // ت ث د ذ ر ز س ش ص ض ط ظ ل ن
  private const val HAMZA_LETTERS = "\u0621\u0623\u0625\u0624\u0626" // ء أ إ ؤ ئ

  private data class NextLetter(val index: Int, val crossedWordBoundary: Boolean)

  fun analyze(text: String): List<TajweedSpan> {
    val spans = mutableListOf<TajweedSpan>()
    var i = 0
    while (i < text.length) {
      val c = text[i]
      when {
        c == SHADDA -> handleShadda(text, i, spans)
        c == SUKUN -> handleSukun(text, i, spans)
        c == FATHATAN || c == DAMMATAN || c == KASRATAN -> handleTanween(text, i, spans)
        (c == ALEF || c == HAMZAT_WASL) -> {
          handleLamAlif(text, i, spans)
          if (c == ALEF && precedingHarakaIs(text, i, FATHA)) {
            classifyMadd(text, i, spans)
          }
        }
        c == WAW && precedingHarakaIs(text, i, DAMMA) && !isFollowedByShadda(text, i) -> {
          classifyMadd(text, i, spans)
        }
        c == YA && precedingHarakaIs(text, i, KASRA) && !isFollowedByShadda(text, i) -> {
          classifyMadd(text, i, spans)
        }
        c == SUPERSCRIPT_ALEF -> classifyMadd(text, i, spans)
      }
      i++
    }
    return spans.sortedBy { it.startIndex }
  }

  // Noon or meem + shaddah => ghunnah.
  private fun handleShadda(text: String, shaddaIndex: Int, spans: MutableList<TajweedSpan>) {
    val letterIndex = precedingBaseLetterIndex(text, shaddaIndex)
    if (letterIndex != -1 && (text[letterIndex] == NOON || text[letterIndex] == MEEM)) {
      spans += TajweedSpan(letterIndex, shaddaIndex + 1, TajweedRule.GHUNNAH, TajweedConfidence.HIGH)
    }
  }

  // A base letter + sukun: noon sakinah, meem sakinah, or a qalqalah letter.
  private fun handleSukun(text: String, sukunIndex: Int, spans: MutableList<TajweedSpan>) {
    val letterIndex = precedingBaseLetterIndex(text, sukunIndex)
    if (letterIndex == -1) return
    when (val letter = text[letterIndex]) {
      NOON -> handleNoonTrigger(text, letterIndex, sukunIndex + 1, spans)
      MEEM -> handleMeemSakinah(text, letterIndex, sukunIndex + 1, spans)
      else -> if (letter in QALQALAH_LETTERS) {
        spans += TajweedSpan(letterIndex, sukunIndex + 1, TajweedRule.QALQALAH, TajweedConfidence.HIGH)
      }
    }
  }

  // Tanween (fathatan/dammatan/kasratan) behaves like noon sakinah, triggered from the mark
  // itself since it isn't attached to the letter noon.
  private fun handleTanween(text: String, tanweenIndex: Int, spans: MutableList<TajweedSpan>) {
    val letterIndex = precedingBaseLetterIndex(text, tanweenIndex)
    if (letterIndex == -1) return
    handleNoonTrigger(text, tanweenIndex, tanweenIndex + 1, spans)
  }

  // Shared logic for noon sakinah and tanween: look at what follows and classify.
  // [spanStart] is the noon letter's index for noon sakinah, or the tanween mark's index for
  // tanween (since tanween has no separate "letter" of its own).
  private fun handleNoonTrigger(text: String, spanStart: Int, afterMarkIndex: Int, spans: MutableList<TajweedSpan>) {
    val next = findNextLetter(text, afterMarkIndex) ?: return
    val nextLetter = text[next.index]
    when {
      nextLetter == BA -> spans += TajweedSpan(spanStart, afterMarkIndex, TajweedRule.IQLAB, TajweedConfidence.HIGH)
      // Idgham never occurs within a single word in real Quranic vocabulary - it requires
      // crossing into the next word. (This also correctly excludes the small set of words like
      // "الدنيا" / "بنيان" / "قنوان" / "صنوان" that superficially match but are not idgham.)
      nextLetter in IDGHAM_GHUNNAH_LETTERS && next.crossedWordBoundary ->
        spans += TajweedSpan(spanStart, afterMarkIndex, TajweedRule.IDGHAM_WITH_GHUNNAH, TajweedConfidence.HIGH)
      nextLetter in IDGHAM_NO_GHUNNAH_LETTERS && next.crossedWordBoundary ->
        spans += TajweedSpan(spanStart, afterMarkIndex, TajweedRule.IDGHAM_WITHOUT_GHUNNAH, TajweedConfidence.HIGH)
      nextLetter in IZHAR_HALQI_LETTERS -> Unit // izhar: clear pronunciation, intentionally not highlighted
      nextLetter in IKHFA_LETTERS ->
        spans += TajweedSpan(spanStart, afterMarkIndex, TajweedRule.IKHFA, TajweedConfidence.HIGH)
      else -> Unit // e.g. one of ينمولر but within the same word: no rule fires (see comment above)
    }
  }

  private fun handleMeemSakinah(text: String, meemIndex: Int, afterMarkIndex: Int, spans: MutableList<TajweedSpan>) {
    val next = findNextLetter(text, afterMarkIndex) ?: return
    when (text[next.index]) {
      MEEM -> spans += TajweedSpan(meemIndex, afterMarkIndex, TajweedRule.IDGHAM_SHAFAWI, TajweedConfidence.HIGH)
      BA -> spans += TajweedSpan(meemIndex, afterMarkIndex, TajweedRule.IKHFA_SHAFAWI, TajweedConfidence.HIGH)
      else -> Unit // izhar shafawi: clear pronunciation, intentionally not highlighted
    }
  }

  // "ال" where the lam is assimilated into a following sun letter (confirmed by the shaddah
  // tajweed scribes place on that following letter).
  private fun handleLamAlif(text: String, alefIndex: Int, spans: MutableList<TajweedSpan>) {
    val lamIndex = alefIndex + 1
    if (lamIndex >= text.length || text[lamIndex] != LAM) return
    val afterLam = skipMarks(text, lamIndex + 1)
    if (afterLam >= text.length || !isBaseLetter(text[afterLam])) return
    val sunLetter = text[afterLam]
    if (sunLetter !in SUN_LETTERS) return
    val markAfterSunLetter = afterLam + 1
    if (markAfterSunLetter < text.length && text[markAfterSunLetter] == SHADDA) {
      spans += TajweedSpan(alefIndex, lamIndex + 1, TajweedRule.LAM_SHAMSIYYAH, TajweedConfidence.HIGH)
    }
  }

  // Classifies a madd (elongation) letter at [letterIndex] as natural / muttasil / munfasil /
  // lazim based on what immediately follows it.
  private fun classifyMadd(text: String, letterIndex: Int, spans: MutableList<TajweedSpan>) {
    val start = letterIndex
    val end = letterIndex + 1
    val j = letterIndex + 1

    if (j < text.length && !text[j].isWhitespace()) {
      // Same word, something follows immediately.
      if (text[j] in HAMZA_LETTERS) {
        spans += TajweedSpan(start, end, TajweedRule.MADD_MUTTASIL, TajweedConfidence.HEURISTIC)
        return
      }
      val nextBase = skipMarks(text, j)
      if (nextBase < text.length && isBaseLetter(text[nextBase])) {
        val markAfterNextBase = nextBase + 1
        val followedBySukunOrShadda = markAfterNextBase < text.length &&
          (text[markAfterNextBase] == SUKUN || text[markAfterNextBase] == SHADDA)
        if (followedBySukunOrShadda) {
          spans += TajweedSpan(start, end, TajweedRule.MADD_LAZIM, TajweedConfidence.HEURISTIC)
          return
        }
      }
    } else {
      // End of word: munfasil if the next word starts with a hamza letter.
      val next = findNextLetter(text, letterIndex + 1)
      if (next != null && text[next.index] in HAMZA_LETTERS) {
        spans += TajweedSpan(start, end, TajweedRule.MADD_MUNFASIL, TajweedConfidence.HEURISTIC)
        return
      }
    }
    spans += TajweedSpan(start, end, TajweedRule.MADD_NATURAL, TajweedConfidence.HIGH)
  }

  // ---- shared scanning helpers ----

  private fun isBaseLetter(c: Char): Boolean {
    val code = c.code
    return when {
      code in 0x0621..0x063A -> true
      code in 0x0641..0x064A -> true
      code == 0x0671 -> true // hamzat wasl
      else -> false
    }
  }

  private fun isDiacriticMark(c: Char): Boolean {
    if (c == TATWEEL) return false
    return Character.getType(c) == Character.NON_SPACING_MARK.toInt()
  }

  private fun skipMarks(text: String, start: Int): Int {
    var i = start
    while (i < text.length && isDiacriticMark(text[i])) i++
    return i
  }

  private fun precedingBaseLetterIndex(text: String, from: Int): Int {
    var i = from - 1
    while (i >= 0 && isDiacriticMark(text[i])) i--
    return if (i >= 0 && isBaseLetter(text[i])) i else -1
  }

  private fun precedingHarakaIs(text: String, index: Int, haraka: Char): Boolean {
    return index - 1 >= 0 && text[index - 1] == haraka
  }

  private fun isFollowedByShadda(text: String, index: Int): Boolean {
    return index + 1 < text.length && text[index + 1] == SHADDA
  }

  private fun findNextLetter(text: String, from: Int): NextLetter? {
    var i = skipMarks(text, from)
    var crossed = false
    while (i < text.length && text[i].isWhitespace()) {
      crossed = true
      i++
      i = skipMarks(text, i)
    }
    return if (i < text.length && isBaseLetter(text[i])) NextLetter(i, crossed) else null
  }
}
