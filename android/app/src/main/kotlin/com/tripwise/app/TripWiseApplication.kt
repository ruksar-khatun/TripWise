package com.tripwise.app

import android.app.Application
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.SingletonImageLoader
import coil3.network.okhttp.OkHttpNetworkFetcherFactory
import coil3.request.crossfade
import com.tripwise.core.network.di.Public
import dagger.hilt.android.HiltAndroidApp
import okhttp3.OkHttpClient
import javax.inject.Inject

@HiltAndroidApp
class TripWiseApplication : Application(), SingletonImageLoader.Factory {

    /** Shared client: identifies the app (User-Agent), as image hosts such as Wikimedia require. */
    @Inject @Public lateinit var httpClient: OkHttpClient

    override fun newImageLoader(context: PlatformContext): ImageLoader =
        ImageLoader.Builder(context)
            .components { add(OkHttpNetworkFetcherFactory(callFactory = { httpClient })) }
            .crossfade(true)
            .build()
}
