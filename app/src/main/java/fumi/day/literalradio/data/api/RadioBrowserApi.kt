package fumi.day.literalradio.data.api

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import fumi.day.literalradio.data.model.Station
import okhttp3.OkHttpClient
import okhttp3.Request
import javax.inject.Inject
import javax.inject.Singleton

val GENRES = listOf(
    "Adult Contemporary", "Alternative Rock", "Blues", "Broadway", "Chill",
    "Classical", "Country", "Dance", "Holiday Music", "Jazz", "Latin",
    "Oldies", "Pop Hits", "Reggae", "Rock", "Soundtracks", "Talk",
    "World Music", "80s", "90s", "00s", "10s",
)

val GENRE_TAGS = mapOf(
    "Adult Contemporary" to "adult contemporary",
    "Alternative Rock"   to "alternative",
    "Blues"              to "blues",
    "Broadway"           to "broadway",
    "Chill"              to "chill",
    "Classical"          to "classical",
    "Country"            to "country",
    "Dance"              to "dance",
    "Holiday Music"      to "christmas",
    "Jazz"               to "jazz",
    "Latin"              to "latin",
    "Oldies"             to "oldies",
    "Pop Hits"           to "pop",
    "Reggae"             to "reggae",
    "Rock"               to "rock",
    "Soundtracks"        to "soundtrack",
    "Talk"               to "talk",
    "World Music"        to "world",
    "80s"                to "80s",
    "90s"                to "90s",
    "00s"                to "2000s",
    "10s"                to "2010s",
)

private fun cleanStationName(raw: String): String {
    var name = raw.trim()
    // 長い名前で `:` の前がタイトルっぽければそこで切る ("Name: desc tag1, tag2")
    val colon = name.indexOf(':')
    if (colon in 3..50 && name.length > colon + 4) {
        name = name.substring(0, colon).trim()
    }
    // ` - ` 以降がタグ列（カンマ含む）なら削除 ("Name - tag1, tag2")
    val dash = name.indexOf(" - ")
    if (dash > 3 && name.indexOf(',', dash) != -1) {
        name = name.substring(0, dash).trim()
    }
    return name
}

private const val BASE_URL = "https://de1.api.radio-browser.info/json"
private const val STATION_PARAMS = "limit=50&order=clickcount&reverse=true&hidebroken=true"

@Singleton
class RadioBrowserApi @Inject constructor(
    private val client: OkHttpClient,
    private val gson: Gson,
) {
    fun fetchByGenre(tag: String): List<Station> =
        fetchStations("$BASE_URL/stations/bytag/$tag?$STATION_PARAMS")

    fun fetchByCountry(country: String): List<Station> =
        fetchStations("$BASE_URL/stations/bycountry/$country?$STATION_PARAMS")

    fun fetchByLanguage(language: String): List<Station> =
        fetchStations("$BASE_URL/stations/bylanguage/$language?$STATION_PARAMS")

    fun fetchByName(query: String): List<Station> =
        fetchStations("$BASE_URL/stations/search?name=$query&$STATION_PARAMS")

    fun fetchCountries(): List<String> {
        val url = "$BASE_URL/countries?order=stationcount&reverse=true&hidebroken=true"
        val json = get(url) ?: return emptyList()
        val type = object : TypeToken<List<Map<String, Any>>>() {}.type
        val list: List<Map<String, Any>> = gson.fromJson(json, type)
        return list.mapNotNull { it["name"] as? String }.filter { it.isNotBlank() }
    }

    fun fetchLanguages(): List<String> {
        val url = "$BASE_URL/languages?order=stationcount&reverse=true&hidebroken=true"
        val json = get(url) ?: return emptyList()
        val type = object : TypeToken<List<Map<String, Any>>>() {}.type
        val list: List<Map<String, Any>> = gson.fromJson(json, type)
        return list.mapNotNull { it["name"] as? String }.filter { it.isNotBlank() }
    }

    private fun fetchStations(url: String): List<Station> {
        val json = get(url) ?: return emptyList()
        val type = object : TypeToken<List<Map<String, Any>>>() {}.type
        val raw: List<Map<String, Any>> = gson.fromJson(json, type)
        return raw.mapNotNull { m ->
            val resolvedUrl = m["url_resolved"] as? String ?: return@mapNotNull null
            if (resolvedUrl.isBlank()) return@mapNotNull null
            val name = cleanStationName(m["name"] as? String ?: "")
            val country = m["country"] as? String ?: ""
            val countryCode = (m["countrycode"] as? String)?.uppercase()
                ?: country.take(2).uppercase()
            val bitrate = (m["bitrate"] as? Double)?.toInt() ?: 0
            val codec = m["codec"] as? String ?: ""
            val language = (m["language"] as? String ?: "")
                .split(",").firstOrNull { it.isNotBlank() }?.trim()?.replaceFirstChar { it.uppercase() } ?: ""
            val tags = (m["tags"] as? String ?: "")
                .split(",").map { it.trim() }.filter { it.isNotBlank() }.take(4)
            Station(
                name = name, country = country, countryCode = countryCode,
                bitrate = bitrate, codec = codec, language = language, tags = tags, url = resolvedUrl,
            )
        }
    }

    private fun get(url: String): String? = try {
        val req = Request.Builder().url(url).build()
        client.newCall(req).execute().use { resp ->
            if (resp.isSuccessful) resp.body?.string() else null
        }
    } catch (e: Exception) {
        null
    }
}
