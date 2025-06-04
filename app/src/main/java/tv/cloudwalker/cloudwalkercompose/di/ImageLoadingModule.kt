package tv.cloudwalker.cloudwalkercompose.di

import android.content.Context
import coil.ImageLoader
import coil.disk.DiskCache
import coil.memory.MemoryCache
import coil.request.CachePolicy
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ImageLoadingModule {

    @Provides
    @Singleton
    fun provideImageLoader(@ApplicationContext context: Context): ImageLoader {
        return ImageLoader.Builder(context)
            .memoryCache {
                MemoryCache.Builder(context)
                    // Use 25% of the app's available memory for image caching (good for TV)
                    .maxSizePercent(0.25)
                    // Keep strong references to cached images
                    .strongReferencesEnabled(true)
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(context.cacheDir.resolve("image_cache"))
                    // 100MB disk cache for TV apps (more than mobile apps)
                    .maxSizeBytes(100 * 1024 * 1024)
                    .build()
            }
            // Performance optimizations for TV
            .crossfade(true) // Smooth image transitions
            .crossfade(200) // 200ms transition
            .respectCacheHeaders(false) // Better caching control
            .allowHardware(true) // Use hardware bitmaps when possible
            // Cache policies for better performance
            .memoryCachePolicy(CachePolicy.ENABLED)
            .diskCachePolicy(CachePolicy.ENABLED)
            .networkCachePolicy(CachePolicy.ENABLED)
            .build()
    }
}