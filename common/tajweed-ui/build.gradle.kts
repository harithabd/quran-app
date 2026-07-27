plugins {
  id("quran.android.library.compose")
}

android.namespace = "com.quran.mobile.tajweed.ui"

dependencies {
  implementation(project(":common:tajweed"))

  implementation(libs.compose.foundation)
  implementation(libs.compose.material3)
  implementation(libs.compose.ui)
  implementation(libs.compose.ui.tooling.preview)
  debugImplementation(libs.compose.ui.tooling)
}
