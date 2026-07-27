package com.quran.labs.androidquran.extra.memorization

import android.content.Context
import android.content.Intent
import com.quran.labs.androidquran.R
import com.quran.labs.androidquran.extra.feature.memorization.MemorizationActivity
import com.quran.labs.androidquran.extra.feature.memorization.R as MemorizationR
import com.quran.mobile.di.ExtraScreenProvider
import com.quran.mobile.memorization.MemorizationEntryPoint
import com.quran.mobile.memorization.VerseTextSource

class MemorizationScreenProvider(
  private val verseTextSource: VerseTextSource
) : ExtraScreenProvider {

  override val order: Int = 100
  override val id: Int = R.id.memorization_test
  override val titleResId: Int = MemorizationR.string.memorization_test_title

  override fun onClick(context: Context): Boolean {
    MemorizationEntryPoint.verseTextSource = verseTextSource
    context.startActivity(Intent(context, MemorizationActivity::class.java))
    return true
  }
}
