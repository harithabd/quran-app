package com.quran.mobile.tajweed

/**
 * A tajweed (recitation) rule that can be detected in Uthmani Quranic text.
 *
 * The [colorHex] values are a *default, editable* presentation choice, not a claim about an
 * "official" scheme — different printed tajweed mushafs use different palettes. Callers are free
 * to remap [TajweedRule] to whatever colors/styles they want; the important, factual part of this
 * type is the rule identity and [description], not the color.
 */
enum class TajweedRule(val displayName: String, val colorHex: String, val description: String) {
  /**
   * Noon or meem carrying a shaddah (ّ) — obligatory nasalization (ghunnah) held for
   * approximately two counts.
   */
  GHUNNAH(
    displayName = "Ghunnah",
    colorHex = "#2E7D32",
    description = "Noon or meem with shaddah: hold the nasal sound for ~2 counts."
  ),

  /** Noon sakinah / tanween merges into a following ي ن م و with nasalization. */
  IDGHAM_WITH_GHUNNAH(
    displayName = "Idgham (with ghunnah)",
    colorHex = "#43A047",
    description = "Noon sakinah/tanween merges into the following letter (ي ن م و) with ghunnah."
  ),

  /** Noon sakinah / tanween merges into a following ل or ر without nasalization. */
  IDGHAM_WITHOUT_GHUNNAH(
    displayName = "Idgham (without ghunnah)",
    colorHex = "#66BB6A",
    description = "Noon sakinah/tanween merges into the following letter (ل ر) with no ghunnah."
  ),

  /** Meem sakinah followed by another meem: merges with ghunnah. */
  IDGHAM_SHAFAWI(
    displayName = "Idgham Shafawi",
    colorHex = "#558B2F",
    description = "Meem sakinah followed by meem: merge with ghunnah."
  ),

  /** Noon sakinah / tanween followed by one of the 15 ikhfa letters. */
  IKHFA(
    displayName = "Ikhfa",
    colorHex = "#EF6C00",
    description = "Noon sakinah/tanween is hidden/nasalized before one of the 15 ikhfa letters."
  ),

  /** Meem sakinah followed by ba: hidden with ghunnah. */
  IKHFA_SHAFAWI(
    displayName = "Ikhfa Shafawi",
    colorHex = "#F57C00",
    description = "Meem sakinah followed by ba: hidden with ghunnah."
  ),

  /** Noon sakinah / tanween followed by ba: changes to a meem sound with ghunnah. */
  IQLAB(
    displayName = "Iqlab",
    colorHex = "#8E24AA",
    description = "Noon sakinah/tanween followed by ba: converted to a meem sound with ghunnah."
  ),

  /** Qaf, Ta, Ba, Jeem, or Dal with sukun: an echoing/bouncing sound. */
  QALQALAH(
    displayName = "Qalqalah",
    colorHex = "#546E7A",
    description = "One of ق ط ب ج د with sukun: pronounce with a slight echo/bounce."
  ),

  /** A madd (elongation) letter not immediately followed by hamzah or sukun: ~2 counts. */
  MADD_NATURAL(
    displayName = "Madd Tabee'i (natural)",
    colorHex = "#C62828",
    description = "Natural elongation (~2 counts): a madd letter with no following hamzah/sukun."
  ),

  /**
   * A madd letter followed by hamzah in the *same* word: elongated further (commonly 4-5
   * counts). Detected heuristically; word-boundary segmentation in this analyzer is based on
   * whitespace only.
   */
  MADD_MUTTASIL(
    displayName = "Madd Muttasil (connected)",
    colorHex = "#B71C1C",
    description = "Madd letter followed by hamzah in the same word: elongate ~4-5 counts."
  ),

  /**
   * A madd letter at the end of a word, immediately followed by a hamzah that begins the next
   * word: elongated (commonly 4-5 counts, or 2 if not paused/connected in recitation).
   */
  MADD_MUNFASIL(
    displayName = "Madd Munfasil (separate)",
    colorHex = "#D84315",
    description = "Madd letter at word end, next word starts with hamzah: elongate ~4-5 counts."
  ),

  /** A madd letter followed by a letter with sukun in the same word: elongated (~6 counts). */
  MADD_LAZIM(
    displayName = "Madd Lazim (obligatory)",
    colorHex = "#4A148C",
    description = "Madd letter followed by a sukun letter in the same word: elongate ~6 counts."
  ),

  /**
   * The lam of the definite article "ال" is assimilated (silent) because it is followed by a
   * "sun letter" carrying a shaddah.
   */
  LAM_SHAMSIYYAH(
    displayName = "Lam Shamsiyyah",
    colorHex = "#9E9E9E",
    description = "The lam of \"ال\" is silent; assimilated into a following sun letter."
  );
}
