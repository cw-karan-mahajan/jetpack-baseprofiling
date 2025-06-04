package tv.cloudwalker.cloudwalkercompose.model

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class MovieResponse(
    @SerializedName("rowCount")
    val rowCount: Int = 0,
    @SerializedName("rows")
    val rows: List<MovieRow> = emptyList()
)

@Keep
data class MovieRow(
    @SerializedName("rowHeader")
    val rowHeader: String = "",
    @SerializedName("rowIndex")
    val rowIndex: Int = 0,
    @SerializedName("rowItems")
    val rowItems: List<MovieTile> = emptyList(),
    @SerializedName("rowLayout")
    val rowLayout: String = "square",
    @SerializedName("isAdsRow")
    val isAdsRow: Boolean? = false,
    @SerializedName("rowAutoRotate")
    val rowAutoRotate: Boolean? = false
)

@Keep
data class MovieTile(
    @SerializedName("tid")
    val tid: String = "",
    @SerializedName("title")
    val title: String = "",
    @SerializedName("poster")
    val poster: String? = null,
    @SerializedName("portrait")
    val portrait: String? = null,
    @SerializedName("background")
    val background: String? = null,
    @SerializedName("synopsis")
    val synopsis: String? = null,
    @SerializedName("genre")
    val genre: List<String> = emptyList(),
    @SerializedName("cast")
    val cast: List<String> = emptyList(),
    @SerializedName("director")
    val director: List<String> = emptyList(),
    @SerializedName("rating")
    val rating: Double = 0.0,
    @SerializedName("runtime")
    val runtime: String? = null,
    @SerializedName("year")
    val year: String? = null,
    @SerializedName("source")
    val source: String = "",
    @SerializedName("package")
    val packageName: String? = null,
    @SerializedName("target")
    val target: List<String> = emptyList(),
    @SerializedName("tileWidth")
    val tileWidth: String? = null,
    @SerializedName("tileHeight")
    val tileHeight: String? = null,
    @SerializedName("tileType")
    val tileType: String? = null,
    @SerializedName("detailPage")
    val detailPage: Boolean = false,
    @SerializedName("useAlternate")
    val useAlternate: Boolean = false,
    @SerializedName("adServer")
    val adServer: String? = null,
    @SerializedName("adVideoUrl")
    val adVideoUrl: String? = null
) {
    val isAdTile: Boolean
        get() = !adServer.isNullOrEmpty()

    val displayImage: String?
        get() = when {
            !poster.isNullOrEmpty() -> poster
            !portrait.isNullOrEmpty() -> portrait
            !background.isNullOrEmpty() -> background
            else -> null
        }

    val isHeroTile: Boolean
        get() = tileType == "typeAdsVideoBanner" || tileType == "typeAdsStaticBanner"
}