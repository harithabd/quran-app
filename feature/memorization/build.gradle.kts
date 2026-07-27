plugins {
  id("quran.android.library.compose")
}

android.namespace = "com.quran.labs.androidquran.extra.feature.memorization"

dependencies {
  implementation(project(":common:memorization"))

  implementation(libs.kotlinx.coroutines.android)

  implementation(libs.androidx.activity.compose)
  implementation(libs.compose.foundation)
  implementation(libs.compose.material3)
  implementation(libs.compose.ui)
  implementation(libs.compose.ui.tooling.preview)
  debugImplementation(libs.compose.ui.tooling)
}
