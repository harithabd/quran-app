package com.quran.labs.androidquran.extra.memorization

import com.quran.data.core.QuranConstants
import com.quran.data.core.QuranInfo
import com.quran.data.model.SuraAyah
import com.quran.labs.androidquran.model.translation.ArabicDatabaseUtils
import com.quran.mobile.memorization.VerseQuestionData
import com.quran.mobile.memorization.VerseText
import com.quran.mobile.memorization.VerseTextSource
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.random.Random

/**
 * Picks a random ayah and fetches its text (and the following ayah's text, if any) from the
 * real Arabic text database via [ArabicDatabaseUtils] - the same source the app already uses
 * to show Arabic text elsewhere (e.g. the translation view, sharing).
 */
class ArabicVerseTextSource @Inject constructor(
  private val arabicDatabaseUtils: ArabicDatabaseUtils,
  private val quranInfo: QuranInfo
) : VerseTextSource {

  override suspend fun randomQuestionVerses(): VerseQuestionData = withContext(Dispatchers.IO) {
    val sura = Random.nextInt(QuranConstants.FIRST_SURA, QuranConstants.LAST_SURA + 1)
    val ayah = Random.nextInt(1, quranInfo.getNumberOfAyahs(sura) + 1)
    val current = SuraAyah(sura, ayah)
    val next = current.next(quranInfo)
    val end = next ?: current

    val verses = arabicDatabaseUtils.getVerses(current, end).blockingGet()
    val currentText = verses.firstOrNull { it.sura == current.sura && it.ayah == current.ayah }
    val nextText = if (next != null) {
      verses.firstOrNull { it.sura == next.sura && it.ayah == next.ayah }
    } else {
      null
    }

    VerseQuestionData(
      current = VerseText(current.sura, current.ayah, currentText?.text.orEmpty()),
      next = nextText?.let { VerseText(next!!.sura, next.ayah, it.text) }
    )
  }
}
