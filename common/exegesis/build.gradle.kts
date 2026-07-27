plugins {
  id("quran.android.library")
  alias(libs.plugins.ksp)
}

dependencies {
  api(libs.retrofit)
  implementation(libs.converter.moshi)
  implementation(libs.moshi)
  ksp(libs.moshi.codegen)

  implementation(libs.kotlinx.coroutines.core)

  testImplementation(libs.junit)
  testImplementation(libs.truth)
}
