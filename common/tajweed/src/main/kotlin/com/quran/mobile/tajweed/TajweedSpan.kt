package com.quran.mobile.tajweed

/**
 * How confident the analyzer is that [TajweedSpan.rule] applies.
 *
 * [HIGH] rules are determined unambiguously from the text itself (e.g. a shaddah is either
 * present or it isn't). [HEURISTIC] rules (mainly madd sub-types and qalqalah kubra) genuinely
 * depend on context this analyzer does not have — such as where a reciter chooses to pause
 * (waqf) — so they are marked lower-confidence rather than asserted as certain.
 */
enum class TajweedConfidence {
  HIGH,
  HEURISTIC
}

/**
 * A single detected tajweed rule occurrence within a string of Uthmani text.
 *
 * @property startIndex inclusive start offset (UTF-16 code unit index) into the analyzed string.
 * @property endIndex exclusive end offset (UTF-16 code unit index) into the analyzed string.
 */
data class TajweedSpan(
  val startIndex: Int,
  val endIndex: Int,
  val rule: TajweedRule,
  val confidence: TajweedConfidence
) {
  init {
    require(startIndex >= 0) { "startIndex must be >= 0, was $startIndex" }
    require(endIndex > startIndex) { "endIndex ($endIndex) must be > startIndex ($startIndex)" }
  }

  fun substringOf(text: String): String = text.substring(startIndex, endIndex)
}
