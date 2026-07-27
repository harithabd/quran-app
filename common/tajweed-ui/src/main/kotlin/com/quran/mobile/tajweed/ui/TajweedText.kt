package com.quran.mobile.tajweed.ui

import androidx.compose.foundation.text.selection.DisableSelection
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.TextUnit
import com.quran.mobile.tajweed.TajweedAnalyzer
import com.quran.mobile.tajweed.TajweedRule

/**
 * Builds an [AnnotatedString] from raw Uthmani [text], applying a [SpanStyle] color for every
 * detected [com.quran.mobile.tajweed.TajweedSpan].
 *
 * @param colorOverrides optional per-rule color overrides. Rules not present here fall back to
 * [TajweedRule.colorHex], which is a suggested default palette, not an authoritative standard -
 * callers building a themed UI will likely want to supply their own map here.
 */
fun buildTajweedAnnotatedString(
  text: String,
  colorOverrides: Map<TajweedRule, Color> = emptyMap()
): AnnotatedString {
  val spans = TajweedAnalyzer.analyze(text)
  return AnnotatedString.Builder(text).apply {
    for (span in spans) {
      val color = colorOverrides[span.rule] ?: parseHexColor(span.rule.colorHex)
      addStyle(SpanStyle(color = color), span.startIndex, span.endIndex)
    }
  }.toAnnotatedString()
}

private fun parseHexColor(hex: String): Color {
  val cleaned = hex.removePrefix("#")
  val colorLong = cleaned.toLong(16)
  return if (cleaned.length == 6) Color(0xFF000000 or colorLong) else Color(colorLong)
}

/**
 * Renders Uthmani [text] with tajweed rule coloring applied. This is a small, self-contained
 * composable intended to be dropped into an existing Arabic text row (e.g. next to a
 * translation) - it does not attempt to match the app's page-image mushaf rendering, which
 * draws pre-built glyph images rather than live text.
 */
@Composable
fun TajweedText(
  text: String,
  modifier: Modifier = Modifier,
  fontSize: TextUnit = LocalTextStyle.current.fontSize,
  colorOverrides: Map<TajweedRule, Color> = emptyMap()
) {
  val annotated = buildTajweedAnnotatedString(text, colorOverrides)
  DisableSelection {
    Text(
      text = annotated,
      modifier = modifier,
      style = TextStyle(textDirection = TextDirection.Rtl),
      fontSize = fontSize
    )
  }
}
