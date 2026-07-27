package com.quran.mobile.memorization

/**
 * Tracks review scheduling for one memorized unit (e.g. one ayah or one page) using a simple
 * Leitner-box scheme: a correct review promotes to the next box (a longer interval); an
 * incorrect review drops back to box 1.
 *
 * This is a standard, well-known spaced-repetition approach (not a claim about optimal
 * memorization science for Quran memorization specifically) - it is offered as a reasonable
 * default and the interval table below is easy to tune.
 */
data class ReviewState(
  val boxLevel: Int,
  val lastReviewEpochDay: Long,
  val nextReviewEpochDay: Long
) {
  companion object {
    /** Interval, in days, before the next review at each box level (1-indexed). */
    val BOX_INTERVALS_DAYS = listOf(1L, 2L, 4L, 7L, 14L, 30L, 60L)

    fun initial(todayEpochDay: Long): ReviewState {
      return ReviewState(
        boxLevel = 1,
        lastReviewEpochDay = todayEpochDay,
        nextReviewEpochDay = todayEpochDay + BOX_INTERVALS_DAYS[0]
      )
    }
  }
}

object SpacedRepetitionScheduler {

  fun nextState(current: ReviewState, wasCorrect: Boolean, todayEpochDay: Long): ReviewState {
    val intervals = ReviewState.BOX_INTERVALS_DAYS
    val newLevel = if (wasCorrect) {
      (current.boxLevel + 1).coerceAtMost(intervals.size)
    } else {
      1
    }
    val interval = intervals[newLevel - 1]
    return ReviewState(
      boxLevel = newLevel,
      lastReviewEpochDay = todayEpochDay,
      nextReviewEpochDay = todayEpochDay + interval
    )
  }

  fun isDue(state: ReviewState, todayEpochDay: Long): Boolean = todayEpochDay >= state.nextReviewEpochDay
}
