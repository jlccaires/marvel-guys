package com.jlccaires.marvelguys.di

import android.content.Context
import android.net.ConnectivityManager
import androidx.work.WorkManager
import com.jlccaires.marvelguys.BuildConfig
import com.jlccaires.marvelguys.data.api.MarvelAPI
import com.jlccaires.marvelguys.data.db.AppDatabase
import com.jlccaires.marvelguys.md5
import com.jlccaires.marvelguys.ui.characterDetail.CharacterDetailContract
import com.jlccaires.marvelguys.ui.characterDetail.CharacterDetailPresenter
import com.jlccaires.marvelguys.ui.characterList.CharacterListAdapter
import com.jlccaires.marvelguys.ui.characterList.CharacterListContract
import com.jlccaires.marvelguys.ui.characterList.CharacterListPresenter
import com.jlccaires.marvelguys.ui.favorites.FavoritesContract
import com.jlccaires.marvelguys.ui.favorites.FavoritesPresenter
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

object AppModule {

    val instance = module {

        single {
            val logInterceptor = HttpLoggingInterceptor()
            logInterceptor.level = HttpLoggingInterceptor.Level.BASIC

            OkHttpClient.Builder()
                .callTimeout(20, TimeUnit.SECONDS)
                .addInterceptor(logInterceptor)
                .addInterceptor { chain ->
                    val request = chain.request()
                    if (!request.url().toString().contains(BuildConfig.API_URL)) {
                        return@addInterceptor chain.proceed(request)
                    }
                    val ts = System.nanoTime().toString()
                    chain.proceed(
                        request.newBuilder().url(
                            request.url()
                                .newBuilder()
                                .addQueryParameter("apikey", BuildConfig.API_PUBLIC_KEY)
                                .addQueryParameter("ts", ts)
                                .addQueryParameter(
                                    "hash",
                                    (ts + BuildConfig.API_PRIVATE_KEY + BuildConfig.API_PUBLIC_KEY).md5()
                                )
                                .build()
                        ).build()
                    )
                }
                .build()
        }

        single {
            Retrofit.Builder()
                .baseUrl(BuildConfig.API_URL)
                .addConverterFactory(MoshiConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .client(get())
                .build()
                .create(MarvelAPI::class.java)
        }

        single {
            get<Context>().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        }

        single {
            WorkManager.getInstance(get())
        }

        single {
            AppDatabase.getDatabase(get())
        }

        single {
            get<AppDatabase>().characterDao()
        }

        single {
            get<AppDatabase>().comicsDao()
        }

        single {
            get<AppDatabase>().seriesDao()
        }

        factory {
            CharacterListAdapter()
        }

        factory<CharacterListContract.Presenter> { (view: CharacterListContract.View) ->
            CharacterListPresenter(view, get(), get(), get(), get())
        }

        factory<FavoritesContract.Presenter> { (view: FavoritesContract.View) ->
            FavoritesPresenter(view, get())
        }

        factory<CharacterDetailContract.Presenter> { (view: CharacterDetailContract.View) ->
            CharacterDetailPresenter(view, get(), get())
        }
    }
}