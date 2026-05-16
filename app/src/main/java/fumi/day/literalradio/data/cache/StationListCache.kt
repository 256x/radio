package fumi.day.literalradio.data.cache

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File

private const val TTL_MS = 24 * 60 * 60 * 1000L

class StationListCache(context: Context, private val gson: Gson) {

    private val cacheDir = File(context.cacheDir, "rlp").also { it.mkdirs() }
    private val countriesFile = File(cacheDir, "countries.json")
    private val languagesFile = File(cacheDir, "languages.json")

    fun getCountries(): List<String>? = readCached(countriesFile)

    fun setCountries(list: List<String>) = writeCached(countriesFile, list)

    fun getLanguages(): List<String>? = readCached(languagesFile)

    fun setLanguages(list: List<String>) = writeCached(languagesFile, list)

    fun clearCache() {
        countriesFile.delete()
        languagesFile.delete()
    }

    private fun readCached(file: File): List<String>? {
        if (!file.exists()) return null
        if (System.currentTimeMillis() - file.lastModified() > TTL_MS) return null
        return try {
            val type = object : TypeToken<List<String>>() {}.type
            gson.fromJson(file.readText(), type)
        } catch (e: Exception) {
            null
        }
    }

    private fun writeCached(file: File, list: List<String>) {
        try { file.writeText(gson.toJson(list)) } catch (_: Exception) {}
    }
}
