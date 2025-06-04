package tv.cloudwalker.cloudwalkercompose.network

import android.content.Context
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CloudWalkerInterceptor @Inject constructor(
    private val context: Context
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()

        val md5 = "sample_md5_hash" // Replace with actual MD5 logic

        val request = original.newBuilder()
            .header("mboard", "BD_TP_MS358_PB803")
            .header("model", "CWT43SUX216")
            .header("emac", "18:18:18:18:E5:18")
            .header("lversion", "projectTest")
            .header("brand", "TSERIES")
            .header("accept-version", "3.0.0")
            .header("cotaversion", "20200727_185452")
            .header("fotaversion", "20200727_185452")
            .header("keymd5", md5)
            .header("cats-version", "") // Get from preferences
            .header("appVersion", "projectTest")
            .method(original.method, original.body)
            .build()

        return chain.proceed(request)
    }
}