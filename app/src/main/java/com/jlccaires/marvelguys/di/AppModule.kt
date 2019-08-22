package com.jlccaires.marvelguys.di

import com.jlccaires.marvelguys.BuildConfig
import com.jlccaires.marvelguys.data.api.MarvelAPI
import com.jlccaires.marvelguys.data.api.MarvelRepository
import com.jlccaires.marvelguys.data.db.AppDatabase
import com.jlccaires.marvelguys.md5
import com.jlccaires.marvelguys.ui.character_list.CharacterContract
import com.jlccaires.marvelguys.ui.character_list.CharacterListAdapter
import com.jlccaires.marvelguys.ui.character_list.CharacterListPresenter
import com.jlccaires.marvelguys.ui.favorites.FavoritesContract
import com.jlccaires.marvelguys.ui.favorites.FavoritesPresenter
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory

object AppModule {

    val instance = module {

        single {
            val logInterceptor = HttpLoggingInterceptor()
            logInterceptor.level = HttpLoggingInterceptor.Level.BODY

            OkHttpClient.Builder()
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
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .client(get())
                .build()
                .create(MarvelAPI::class.java)
        }

        single {
            MarvelRepository(get())
        }

        single {
            AppDatabase.getDatabase(get())
        }

        single {
            get<AppDatabase>().characterDao()
        }

        factory {
            CharacterListAdapter()
        }

        factory<CharacterContract.Presenter> { (view: CharacterContract.View) ->
            CharacterListPresenter(view, get(), get())
        }

        factory<FavoritesContract.Presenter> { (view: FavoritesContract.View) ->
            FavoritesPresenter(view, get())
        }
    }
}