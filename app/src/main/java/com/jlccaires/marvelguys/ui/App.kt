package com.jlccaires.marvelguys.ui

import android.app.Application
import androidx.work.Configuration
import com.jlccaires.marvelguys.di.AppModule
import com.squareup.picasso.OkHttp3Downloader
import com.squareup.picasso.Picasso
import okhttp3.Cache
import okhttp3.OkHttpClient
import org.koin.android.ext.android.get
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import java.io.File

class App : Application(), Configuration.Provider {

    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@App)
            modules(AppModule.instance)
        }
        val picasso = Picasso.Builder(this)
            .downloader(
                OkHttp3Downloader(
                    get<OkHttpClient>().newBuilder()
                        .cache(Cache(File(cacheDir, "images"), 500 * 1024 * 1024))
                        .build()
                )
            )
            .build()
        Picasso.setSingletonInstance(picasso)
    }

    override fun getWorkManagerConfiguration() =
        Configuration.Builder()
            .setMinimumLoggingLevel(android.util.Log.INFO)
            .build()
}