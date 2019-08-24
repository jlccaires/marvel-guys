package com.jlccaires.marvelguys.data.worker

import android.content.Context
import android.util.Log
import androidx.work.*
import com.jlccaires.marvelguys.data.api.MarvelAPI
import com.jlccaires.marvelguys.data.db.dao.CharacterDao
import com.jlccaires.marvelguys.data.db.dao.ComicsDao
import com.jlccaires.marvelguys.data.db.dao.SeriesDao
import com.jlccaires.marvelguys.data.db.entity.ComicEntity
import com.jlccaires.marvelguys.data.db.entity.SerieEntity
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import org.koin.core.KoinComponent
import org.koin.core.inject
import java.util.concurrent.TimeUnit

class FavoriteSyncWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : RxWorker(appContext, workerParams), KoinComponent {

    private val api: MarvelAPI by inject()
    private val characterDao: CharacterDao by inject()
    private val comicsDao: ComicsDao by inject()
    private val seriesDao: SeriesDao by inject()

    override fun createWork(): Single<Result> {

        val characterId = inputData.getInt(PARAM_ID, 0)

        val comicsSource = Observable.range(0, 100)
            .concatMapSingle { page ->
                api.getComics(characterId, page * 100)
                    .retry(3)
                    .subscribeOn(Schedulers.io())
            }
            .takeUntil { it.data.total == it.data.offset + it.data.count }
            .map { result ->
                result.data.results.map {
                    ComicEntity(
                        it.id,
                        it.title,
                        it.thumbnail.run { "$path/standard_xlarge.$extension" },
                        characterId
                    )
                }
            }
            .concatMapCompletable {
                comicsDao.insert(it)
                    .subscribeOn(Schedulers.io())
            }


        val seriesSource = Observable.range(0, 100)
            .concatMapSingle { page ->
                api.getSeries(characterId, page * 100)
                    .retry(3)
                    .subscribeOn(Schedulers.io())
            }
            .takeUntil { it.data.total == it.data.offset + it.data.count }
            .map { result ->
                result.data.results.map {
                    SerieEntity(
                        it.id,
                        it.title,
                        it.thumbnail.run { "$path/standard_xlarge.$extension" },
                        characterId
                    )
                }
            }
            .concatMapCompletable {
                seriesDao.insert(it)
                    .subscribeOn(Schedulers.io())
            }

        return Completable.mergeArray(comicsSource, seriesSource)
            .subscribeOn(Schedulers.computation())
            .concatWith {
                characterDao.byId(characterId)
                    .subscribeOn(Schedulers.io())
                    .flatMapCompletable {
                        characterDao.update(it.copy(syncing = false))
                            .subscribeOn(Schedulers.io())

                    }
                    .subscribe(it)
            }
            .toSingleDefault(Result.success(workDataOf(PARAM_ID to characterId)))
            .onErrorReturn { Result.retry() }
            .doOnError {
                Log.e(FavoriteSyncWorker::class.java.simpleName, "", it)
            }
    }

    companion object {
        const val FAVORITE_WORKER_TAG = "FAVORITE_WORKER_TAG"
        const val PARAM_ID = "PARAM_ID"

        fun create(characterId: Int): OneTimeWorkRequest {

            val data = workDataOf(PARAM_ID to characterId)

            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            return OneTimeWorkRequestBuilder<FavoriteSyncWorker>()
                .setInputData(data)
                .setConstraints(constraints)
                .setBackoffCriteria(
                    BackoffPolicy.LINEAR,
                    OneTimeWorkRequest.MIN_BACKOFF_MILLIS,
                    TimeUnit.MILLISECONDS
                )
                .addTag(FAVORITE_WORKER_TAG)
                .build()
        }
    }
}