package com.quran.mobile.exegesis

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Client for the Quran Foundation content API (https://api-docs.quran.foundation/), the official
 * successor to the original Quran.com API. Requires an OAuth2 access token obtained via client
 * credentials registered with Quran Foundation - there is no anonymous tier for this endpoint as
 * documented. See the module README for how to request access.
 *
 * IMPORTANT: I was not able to make a live request to confirm these exact paths from the sandbox
 * this was written in (no network access) - the shapes below are modeled from the API's published
 * documentation and response examples. Re-verify against https://api-docs.quran.foundation/ before
 * relying on this in production; hosted API surfaces do change over time.
 */
interface TafsirApi {

  /** Lists all available tafsir resources (authors/works), e.g. "Tafsir Ibn Kathir". */
  @GET("resources/tafsirs")
  suspend fun getTafsirResources(
    @Header("Authorization") bearerToken: String,
    @Query("language") languageCode: String? = null
  ): TafsirResourceListResponse

  /**
   * Gets one tafsir resource's commentary on a single ayah.
   * @param verseKey e.g. "1:1" for surah 1, ayah 1 - matches the API's own `verse_key` addressing.
   */
  @GET("tafsirs/{tafsir_id}/by_ayah/{verse_key}")
  suspend fun getTafsirForAyah(
    @Header("Authorization") bearerToken: String,
    @Path("tafsir_id") tafsirResourceId: Int,
    @Path("verse_key") verseKey: String
  ): TafsirAyahResponse
}

@JsonClass(generateAdapter = true)
data class TafsirResourceListResponse(
  @Json(name = "tafsirs") val resources: List<TafsirResourceDto>
)

@JsonClass(generateAdapter = true)
data class TafsirResourceDto(
  @Json(name = "id") val resourceId: Int,
  @Json(name = "name") val name: String,
  @Json(name = "author_name") val authorName: String?,
  @Json(name = "language_name") val languageName: String,
  @Json(name = "slug") val slug: String
) {
  fun toDomain(): TafsirResource = TafsirResource(
    resourceId = resourceId,
    name = name,
    authorName = authorName,
    languageCode = languageName,
    slug = slug
  )
}

@JsonClass(generateAdapter = true)
data class TafsirAyahResponse(
  @Json(name = "tafsirs") val tafsirs: List<TafsirAyahDto>
)

@JsonClass(generateAdapter = true)
data class TafsirAyahDto(
  @Json(name = "resource_id") val resourceId: Int,
  @Json(name = "verse_key") val verseKey: String,
  @Json(name = "text") val text: String
) {
  /** [verseKey] is expected in "sura:ayah" form; returns null if it doesn't parse. */
  fun toDomain(): TafsirEntry? {
    val parts = verseKey.split(":")
    val sura = parts.getOrNull(0)?.toIntOrNull()
    val ayah = parts.getOrNull(1)?.toIntOrNull()
    return if (sura != null && ayah != null) {
      TafsirEntry(resourceId = resourceId, sura = sura, ayah = ayah, html = text)
    } else {
      null
    }
  }
}
