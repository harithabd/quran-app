package com.quran.mobile.exegesis

/**
 * A tafsir (exegesis) resource/author, e.g. "Tafsir Ibn Kathir". Mirrors the shape of the Quran
 * Foundation API's `/resources/tafsirs` endpoint - see the module README for source details.
 */
data class TafsirResource(
  val resourceId: Int,
  val name: String,
  val authorName: String?,
  val languageCode: String,
  val slug: String
)

/** One tafsir resource's commentary on one ayah. */
data class TafsirEntry(
  val resourceId: Int,
  val sura: Int,
  val ayah: Int,
  /** HTML, as supplied by the source - render with an HTML-aware text view, don't strip blindly. */
  val html: String
)

/**
 * A single asbab al-nuzul (reason/occasion for revelation) narration tied to one ayah (or a
 * range starting at it). Modeled separately from [TafsirEntry] - see the module README for why.
 */
data class AsbabAlNuzulEntry(
  val sura: Int,
  val ayah: Int,
  /** e.g. "al-Wahidi" - always carry the named source; never leave this blank. */
  val sourceAuthor: String,
  /** HTML or plain text narration, as supplied by the source. */
  val text: String
)

/**
 * One traditional Arabic grammar (i'rab) part-of-speech tag from the Quranic Arabic Corpus
 * tagset. This is a small subset of their documented tags - extend as needed, but keep tags
 * matching the corpus's own vocabulary rather than inventing new categories.
 */
enum class PartOfSpeech {
  NOUN, VERB, PARTICLE, PRONOUN, ADJECTIVE, PROPER_NOUN, DEMONSTRATIVE,
  RELATIVE_PRONOUN, PREPOSITION, CONJUNCTION, NEGATIVE_PARTICLE, VOCATIVE_PARTICLE,
  UNKNOWN
}

/**
 * One morphological segment of one word, as annotated by the Quranic Arabic Corpus. A single
 * word may have multiple segments (e.g. a verb stem plus an attached pronoun suffix).
 *
 * @property sura/[ayah]/[wordPosition]/[segmentPosition] mirror the corpus's own
 * `(sura:ayah:word:segment)` addressing scheme.
 * @property transliteration Buckwalter-style transliteration as given by the corpus.
 * @property partOfSpeech the segment's grammatical category.
 * @property lemma dictionary/lemma form, if the corpus provides one for this segment.
 * @property root the triliteral/quadriliteral root, if applicable.
 * @property features raw feature tags as given by the corpus (e.g. "PERF", "3MS", "NOM") -
 * kept as-is rather than force-fit into a rigid schema, since the corpus's feature set is large
 * and varies by part of speech.
 */
data class MorphologicalSegment(
  val sura: Int,
  val ayah: Int,
  val wordPosition: Int,
  val segmentPosition: Int,
  val transliteration: String,
  val partOfSpeech: PartOfSpeech,
  val lemma: String?,
  val root: String?,
  val features: List<String>
)
