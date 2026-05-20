package fumi.day.literalradio.data.repository

import fumi.day.literalradio.data.api.GENRE_TAGS
import fumi.day.literalradio.data.api.RadioBrowserApi
import fumi.day.literalradio.data.cache.StationListCache
import fumi.day.literalradio.data.model.Station
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StationRepository @Inject constructor(
    private val api: RadioBrowserApi,
    private val cache: StationListCache,
) {
    suspend fun fetchByGenre(genre: String): List<Station> = withContext(Dispatchers.IO) {
        val tag = GENRE_TAGS[genre] ?: genre.lowercase()
        api.fetchByGenre(tag)
    }

    suspend fun fetchByCountry(country: String): List<Station> = withContext(Dispatchers.IO) {
        api.fetchByCountry(country)
    }

    suspend fun fetchByLanguage(language: String): List<Station> = withContext(Dispatchers.IO) {
        api.fetchByLanguage(language)
    }

    suspend fun fetchByName(query: String): List<Station> = withContext(Dispatchers.IO) {
        api.fetchByName(query)
    }

    suspend fun getCountries(): List<String> = withContext(Dispatchers.IO) {
        cache.getCountries()?.takeIf { it.isNotEmpty() }
            ?: api.fetchCountries().also { if (it.isNotEmpty()) cache.setCountries(it) }
    }

    suspend fun getLanguages(): List<String> = withContext(Dispatchers.IO) {
        cache.getLanguages()?.takeIf { it.isNotEmpty() }
            ?: api.fetchLanguages().also { if (it.isNotEmpty()) cache.setLanguages(it) }
    }

    fun clearListCache() = cache.clearCache()
}
