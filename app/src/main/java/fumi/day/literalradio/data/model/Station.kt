package fumi.day.literalradio.data.model

data class Station(
    val name: String,
    val country: String,
    val countryCode: String,
    val bitrate: Int,
    val codec: String,
    val language: String,
    val tags: List<String>,
    val url: String,
)
