package com.quran.mobile.memorization

/**
 * A minimal, deliberately simple bridge between the app module (which owns the real
 * [VerseTextSource] implementation, backed by the Quran text database) and the
 * `feature:memorization` module (which only knows the [VerseTextSource] interface).
 *
 * The app's [com.quran.mobile.di.ExtraScreenProvider] implementation sets [verseTextSource]
 * before launching the memorization Activity; the Activity reads it on creation. This avoids
 * needing a full dependency-injection graph extension for a single cross-module reference.
 */
object MemorizationEntryPoint {
  @Volatile
  var verseTextSource: VerseTextSource? = null
}
