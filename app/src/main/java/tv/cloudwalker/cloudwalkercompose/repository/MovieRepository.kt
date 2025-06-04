package tv.cloudwalker.cloudwalkercompose.repository

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import tv.cloudwalker.cloudwalkercompose.model.MovieResponse
import tv.cloudwalker.cloudwalkercompose.model.MovieRow
import tv.cloudwalker.cloudwalkercompose.model.MovieTile
import tv.cloudwalker.cloudwalkercompose.network.ApiService
import tv.cloudwalker.cloudwalkercompose.util.Resource
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MovieRepository @Inject constructor(
    private val apiService: ApiService,
    @ApplicationContext private val context: Context,
    private val gson: Gson
) {

    private val TAG = "MovieRepository"

    fun getHomeScreenData(): Flow<Resource<MovieResponse>> = flow {
        val response = apiService.getHomeScreenData()
        emit(Resource.Loading())
        Log.d(TAG, "Starting to load data...")

        if (response.isSuccessful && response.body() != null) {
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
        } else {
            // Try to load from local assets
            val localData = getDataFromAssets()
            if (localData != null && localData.rows.isNotEmpty()) {
                Log.d(TAG, "Successfully loaded data from assets: ${localData.rows.size} rows")
                emit(Resource.Success(localData))
            } else {
                Log.e(TAG, "No data found in assets")
                emit(Resource.Error("No data available"))
            }
        }
    }.catch { exception ->
        Log.e(TAG, "Flow exception caught", exception)
        emit(Resource.Error("Error loading data: ${exception.message}"))
    }

    private fun getDataFromAssets(): MovieResponse? {
        return try {
            Log.d(TAG, "Attempting to read from assets...")
            // Try different possible file paths
            val possiblePaths = listOf(
                "defaultrows/default.json",
                "defaultrows/offline_noservice.json"
            )

            for (path in possiblePaths) {
                try {
                    Log.d(TAG, "Trying to read: $path")
                    val inputStream = context.assets.open(path)
                    val size = inputStream.available()
                    val buffer = ByteArray(size)
                    val bytesRead = inputStream.read(buffer)
                    inputStream.close()

                    if (bytesRead > 0) {
                        val json = String(buffer, Charsets.UTF_8)
                        Log.d(TAG, "Successfully read $path, size: ${json.length}")

                        if (json.isNotBlank()) {
                            try {
                                val movieResponse = gson.fromJson(json, MovieResponse::class.java)

                                if (movieResponse != null && movieResponse.rows.isNotEmpty()) {
                                    Log.d(TAG, "Successfully parsed JSON from $path")

                                    // Validate the data
                                    val validatedResponse = validateMovieResponse(movieResponse)

                                    Log.d(TAG, "Validated response: ${validatedResponse.rows.size} rows")
                                    validatedResponse.rows.forEachIndexed { index, row ->
                                        Log.d(TAG, "Row $index: '${row.rowHeader}' - ${row.rowItems.size} items, layout: ${row.rowLayout}")
                                    }

                                    return validatedResponse
                                }
                            } catch (e: Exception) {
                                Log.e(TAG, "Error parsing JSON from $path", e)
                            }
                        }
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "Failed to read $path: ${e.message}")
                }
            }

            Log.e(TAG, "No valid JSON file found in assets")

            // Create sample data as fallback
            return createSampleData()

        } catch (e: Exception) {
            Log.e(TAG, "Error reading from assets", e)
            return createSampleData()
        }
    }

    private fun validateMovieResponse(response: MovieResponse): MovieResponse {
        val validatedRows = response.rows.mapNotNull { row ->
            try {
                // Filter out tiles with invalid data
                val validTiles = row.rowItems.mapNotNull { tile ->
                    if (tile.tid.isBlank() || tile.title.isBlank()) {
                        Log.w(TAG, "Skipping invalid tile: tid='${tile.tid}', title='${tile.title}'")
                        null
                    } else {
                        tile
                    }
                }

                if (validTiles.isNotEmpty()) {
                    row.copy(rowItems = validTiles)
                } else {
                    Log.w(TAG, "Skipping empty row: ${row.rowHeader}")
                    null
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error validating row: ${row.rowHeader}", e)
                null
            }
        }

        return response.copy(rows = validatedRows)
    }

    private fun createSampleData(): MovieResponse {
        Log.d(TAG, "Creating sample data as fallback")

        return MovieResponse(
            rowCount = 2,
            rows = listOf(
                // Hero row
                MovieRow(
                    rowHeader = "Featured",
                    rowIndex = 0,
                    rowLayout = "landscape",
                    rowItems = listOf(
                        MovieTile(
                            tid = "sample_1",
                            title = "Sample Movie 1",
                            synopsis = "This is a sample movie for testing",
                            tileWidth = "1200",
                            tileHeight = "313",
                            source = "Sample"
                        ),
                        MovieTile(
                            tid = "sample_2",
                            title = "Sample Movie 2",
                            synopsis = "Another sample movie for testing",
                            tileWidth = "1200",
                            tileHeight = "313",
                            source = "Sample"
                        )
                    )
                ),
                // Content row
                MovieRow(
                    rowHeader = "Popular Movies",
                    rowIndex = 1,
                    rowLayout = "square",
                    rowItems = listOf(
                        MovieTile(
                            tid = "sample_3",
                            title = "Sample Square 1",
                            tileWidth = "220",
                            tileHeight = "220",
                            source = "Sample"
                        ),
                        MovieTile(
                            tid = "sample_4",
                            title = "Sample Square 2",
                            tileWidth = "220",
                            tileHeight = "220",
                            source = "Sample"
                        )
                    )
                )
            )
        )
    }
}