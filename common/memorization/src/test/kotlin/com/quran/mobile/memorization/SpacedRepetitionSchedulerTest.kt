package com.quran.mobile.memorization

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class SpacedRepetitionSchedulerTest {

  @Test
  fun `initial state starts at box 1 due tomorrow`() {
    val state = ReviewState.initial(todayEpochDay = 100)
    assertThat(state.boxLevel).isEqualTo(1)
    assertThat(state.nextReviewEpochDay).isEqualTo(101)
  }

  @Test
  fun `correct review promotes to the next box with a longer interval`() {
    val state = ReviewState.initial(todayEpochDay = 100)
    val next = SpacedRepetitionScheduler.nextState(state, wasCorrect = true, todayEpochDay = 101)
    assertThat(next.boxLevel).isEqualTo(2)
    assertThat(next.nextReviewEpochDay).isEqualTo(101 + ReviewState.BOX_INTERVALS_DAYS[1])
  }

  @Test
  fun `incorrect review resets to box 1`() {
    var state = ReviewState.initial(todayEpochDay = 0)
    state = SpacedRepetitionScheduler.nextState(state, wasCorrect = true, todayEpochDay = 1)
    state = SpacedRepetitionScheduler.nextState(state, wasCorrect = true, todayEpochDay = 3)
    assertThat(state.boxLevel).isEqualTo(3)

    val afterMiss = SpacedRepetitionScheduler.nextState(state, wasCorrect = false, todayEpochDay = 10)
    assertThat(afterMiss.boxLevel).isEqualTo(1)
  }

  @Test
  fun `box level never exceeds the interval table size`() {
    var state = ReviewState.initial(todayEpochDay = 0)
    var day = 0L
    repeat(20) {
      day += 1
      state = SpacedRepetitionScheduler.nextState(state, wasCorrect = true, todayEpochDay = day)
    }
    assertThat(state.boxLevel).isEqualTo(ReviewState.BOX_INTERVALS_DAYS.size)
  }

  @Test
  fun `isDue is false before the scheduled day and true on or after it`() {
    val state = ReviewState.initial(todayEpochDay = 100) // next review = 101
    assertThat(SpacedRepetitionScheduler.isDue(state, todayEpochDay = 100)).isFalse()
    assertThat(SpacedRepetitionScheduler.isDue(state, todayEpochDay = 101)).isTrue()
    assertThat(SpacedRepetitionScheduler.isDue(state, todayEpochDay = 105)).isTrue()
  }
}
