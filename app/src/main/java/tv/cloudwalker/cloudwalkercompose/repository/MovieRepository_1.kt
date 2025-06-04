package tv.cloudwalker.cloudwalkercompose.repository

import android.content.Context
import com.google.gson.Gson
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import tv.cloudwalker.cloudwalkercompose.model.MovieResponse
import tv.cloudwalker.cloudwalkercompose.network.ApiService
import tv.cloudwalker.cloudwalkercompose.util.Resource
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MovieRepository_1 @Inject constructor(
    private val apiService: ApiService,
    @ApplicationContext private val context: Context,
    private val gson: Gson
) {

    fun getHomeScreenData(): Flow<Resource<MovieResponse>> = flow {
        try {
            emit(Resource.Loading())

            val response = apiService.getHomeScreenData()

            /*if (response.isSuccessful && response.body() != null) {
                val responseBody = response.body()!!
                val jsonString = responseBody.string()
                val movieResponse = gson.fromJson(jsonString, MovieResponse::class.java)

                if (movieResponse != null && movieResponse.rows.isNotEmpty()) {
                    emit(Resource.Success(movieResponse))
                } else {
                    // Try to load from local assets as fallback
                    val localData = getDataFromAssets()
                    if (localData != null) {
                        emit(Resource.Success(localData))
                    } else {
                        emit(Resource.Error("No data available"))
                    }
                }
            }*/

           // else {
                // Try to load from local assets as fallback
                val localData = getDataFromAssets()
                if (localData != null) {
                    emit(Resource.Success(localData))
                } else {
                    emit(Resource.Error("Failed to load data: ${response.code()}"))
                }
           //}

        } catch (e: Exception) {
            // Try to load from local assets as fallback
            val localData = getDataFromAssets()
            if (localData != null) {
                emit(Resource.Success(localData))
            } else {
                emit(Resource.Error("Network error: ${e.message}"))
            }
        }
    }

    private fun getDataFromAssets(): MovieResponse? {
        return try {
            val inputStream = context.assets.open("defaultrows/default.json")
            val size = inputStream.available()
            val buffer = ByteArray(size)
            inputStream.read(buffer)
            inputStream.close()
            val json = String(buffer, Charsets.UTF_8)
            gson.fromJson(json, MovieResponse::class.java)
        } catch (e: Exception) {
            null
        }
    }
}

