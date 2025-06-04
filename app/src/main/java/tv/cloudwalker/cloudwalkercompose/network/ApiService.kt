package tv.cloudwalker.cloudwalkercompose.network

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Headers

interface ApiService {
    @Headers("Accept-Version: 2.0.0")
    @GET("cats")
    suspend fun getHomeScreenData(): Response<ResponseBody>
}



