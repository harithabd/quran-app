package com.quran.labs.androidquran.extra.feature.memorization

import android.content.SharedPreferences
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.quran.mobile.memorization.MemorizationEntryPoint
import com.quran.mobile.memorization.MemorizationMode
import com.quran.mobile.memorization.MemorizationQuestion
import com.quran.mobile.memorization.MemorizationResult
import com.quran.mobile.memorization.MemorizationScorer
import com.quran.mobile.memorization.MemorizationTestGenerator
import com.quran.mobile.memorization.ReviewState
import com.quran.mobile.memorization.SpacedRepetitionScheduler
import com.quran.mobile.memorization.VerseTextSource
import kotlinx.coroutines.launch
import kotlin.random.Random

class MemorizationActivity : ComponentActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    val source = MemorizationEntryPoint.verseTextSource
    val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)

    setContent {
      MaterialTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
          if (source == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
              Text("Memorization feature is unavailable right now.")
            }
          } else {
            MemorizationScreen(source, prefs)
          }
        }
      }
    }
  }

  companion object {
    private const val PREFS_NAME = "memorization_review"
  }
}

private sealed class UiState {
  data object Loading : UiState()
  data class Question(
    val question: MemorizationQuestion,
    val contextText: String?,
    val reviewState: ReviewState?
  ) : UiState()
  data class Result(
    val question: MemorizationQuestion,
    val result: MemorizationResult,
    val reviewState: ReviewState
  ) : UiState()
}

@Composable
private fun MemorizationScreen(source: VerseTextSource, prefs: SharedPreferences) {
  var uiState by remember { mutableStateOf<UiState>(UiState.Loading) }
  var answerText by remember { mutableStateOf("") }
  val scope = rememberCoroutineScope()

  fun loadNext() {
    uiState = UiState.Loading
    answerText = ""
    scope.launch {
      val data = source.randomQuestionVerses()
      val mode = pickMode(hasNext = data.next != null)
      val question = when (mode) {
        MemorizationMode.HIDE_WORDS ->
          MemorizationTestGenerator.generateHideWordsQuestion(data.current)
        MemorizationMode.FILL_IN_BLANK ->
          MemorizationTestGenerator.generateFillInBlankQuestion(data.current, anchorWordCount = 2)
        MemorizationMode.NEXT_AYAH_RECALL ->
          MemorizationTestGenerator.generateFillInBlankQuestion(data.next!!, anchorWordCount = 0)
      }
      val contextText = if (mode == MemorizationMode.NEXT_AYAH_RECALL) data.current.text else null
      val review = loadReviewState(prefs, reviewKey(question.sura, question.ayah))
      uiState = UiState.Question(question, contextText, review)
    }
  }

  LaunchedEffect(Unit) { loadNext() }

  when (val state = uiState) {
    is UiState.Loading -> LoadingContent()
    is UiState.Question -> QuestionContent(
      state = state,
      answerText = answerText,
      onAnswerChange = { answerText = it },
      onSubmit = {
        val userWords = answerText.trim().split(Regex("\\s+")).filter { it.isNotBlank() }
        val result = MemorizationScorer.scoreHiddenWords(state.question, userWords)
        val today = epochDayNow()
        val previous = state.reviewState ?: ReviewState.initial(today)
        val updated = SpacedRepetitionScheduler.nextState(previous, result.isPerfect, today)
        saveReviewState(prefs, reviewKey(state.question.sura, state.question.ayah), updated)
        uiState = UiState.Result(state.question, result, updated)
      }
    )
    is UiState.Result -> ResultContent(state = state, onNext = { loadNext() })
  }
}

@Composable
private fun LoadingContent() {
  Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
    Text(stringResource(R.string.memorization_loading))
  }
}

@Composable
private fun QuestionContent(
  state: UiState.Question,
  answerText: String,
  onAnswerChange: (String) -> Unit,
  onSubmit: () -> Unit
) {
  val isNextAyahMode = state.question.mode == MemorizationMode.NEXT_AYAH_RECALL
  Column(
    modifier = Modifier
      .fillMaxSize()
      .padding(24.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    Text(
      text = when (state.question.mode) {
        MemorizationMode.HIDE_WORDS -> stringResource(R.string.memorization_mode_hide_words)
        MemorizationMode.FILL_IN_BLANK -> stringResource(R.string.memorization_mode_fill_in_blank)
        MemorizationMode.NEXT_AYAH_RECALL -> stringResource(R.string.memorization_mode_next_ayah)
      },
      style = MaterialTheme.typography.labelLarge
    )

    if (isNextAyahMode && state.contextText != null) {
      Text(text = state.contextText, style = MaterialTheme.typography.bodyLarge)
      Text(stringResource(R.string.memorization_prompt_next_ayah), style = MaterialTheme.typography.bodyMedium)
    } else {
      Text(stringResource(R.string.memorization_prompt_hide_words), style = MaterialTheme.typography.bodyMedium)
      Text(
        text = state.question.promptWords().joinToString(" "),
        style = MaterialTheme.typography.bodyLarge
      )
    }

    OutlinedTextField(
      value = answerText,
      onValueChange = onAnswerChange,
      label = { Text(stringResource(R.string.memorization_answer_hint)) },
      modifier = Modifier.fillMaxWidth()
    )

    Button(onClick = onSubmit, modifier = Modifier.fillMaxWidth()) {
      Text(stringResource(R.string.memorization_submit))
    }
  }
}

@Composable
private fun ResultContent(state: UiState.Result, onNext: () -> Unit) {
  Column(
    modifier = Modifier
      .fillMaxSize()
      .padding(24.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    Text(
      text = stringResource(
        R.string.memorization_score_format,
        state.result.correctBlanks,
        state.result.totalBlanks
      ),
      style = MaterialTheme.typography.headlineSmall
    )
    Text(
      text = if (state.result.isPerfect) {
        stringResource(R.string.memorization_perfect)
      } else {
        stringResource(R.string.memorization_not_perfect)
      }
    )
    Text(
      text = stringResource(
        R.string.memorization_streak_format,
        state.reviewState.boxLevel,
        (state.reviewState.nextReviewEpochDay - epochDayNow()).coerceAtLeast(0)
      ),
      style = MaterialTheme.typography.bodyMedium
    )
    Button(onClick = onNext, modifier = Modifier.fillMaxWidth()) {
      Text(stringResource(R.string.memorization_next_question))
    }
  }
}

private fun pickMode(hasNext: Boolean): MemorizationMode {
  val options = if (hasNext) {
    listOf(MemorizationMode.HIDE_WORDS, MemorizationMode.FILL_IN_BLANK, MemorizationMode.NEXT_AYAH_RECALL)
  } else {
    listOf(MemorizationMode.HIDE_WORDS, MemorizationMode.FILL_IN_BLANK)
  }
  return options[Random.nextInt(options.size)]
}

private fun epochDayNow(): Long = System.currentTimeMillis() / 86_400_000L

private fun reviewKey(sura: Int, ayah: Int): String = "review_${sura}_$ayah"

private fun loadReviewState(prefs: SharedPreferences, key: String): ReviewState? {
  val raw = prefs.getString(key, null) ?: return null
  val parts = raw.split(":")
  if (parts.size != 3) return null
  return try {
    ReviewState(
      boxLevel = parts[0].toInt(),
      lastReviewEpochDay = parts[1].toLong(),
      nextReviewEpochDay = parts[2].toLong()
    )
  } catch (e: NumberFormatException) {
    null
  }
}

private fun saveReviewState(prefs: SharedPreferences, key: String, state: ReviewState) {
  val raw = "${state.boxLevel}:${state.lastReviewEpochDay}:${state.nextReviewEpochDay}"
  prefs.edit().putString(key, raw).apply()
}
