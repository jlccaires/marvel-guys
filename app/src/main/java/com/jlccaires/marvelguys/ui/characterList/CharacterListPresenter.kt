package com.jlccaires.marvelguys.ui.characterList

import android.net.ConnectivityManager
import android.net.NetworkInfo
import androidx.lifecycle.Observer
import androidx.work.ExistingWorkPolicy
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.jlccaires.marvelguys.addTo
import com.jlccaires.marvelguys.data.api.MarvelAPI
import com.jlccaires.marvelguys.data.db.dao.CharacterDao
import com.jlccaires.marvelguys.data.db.entity.CharacterEntity
import com.jlccaires.marvelguys.data.worker.FavoriteSyncWorker
import com.jlccaires.marvelguys.data.worker.FavoriteSyncWorker.Companion.FAVORITE_WORKER_TAG
import com.jlccaires.marvelguys.data.worker.FavoriteSyncWorker.Companion.PARAM_ID
import com.jlccaires.marvelguys.ui.event.EventBus
import com.jlccaires.marvelguys.ui.event.FavRemovedEvent
import com.jlccaires.marvelguys.ui.vo.CharacterVo
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

/**
 * Sim, eu deveria ter criado um model para isso :(
 * Mas foi o que deu pra fazer nesse tempo!
 */
class CharacterListPresenter(
    private val view: CharacterListContract.View,
    private val api: MarvelAPI,
    private val cm: ConnectivityManager,
    private val workManager: WorkManager,
    private val charactersDao: CharacterDao
) : CharacterListContract.Presenter {

    private val disposables = CompositeDisposable()

    init {
        workManager.getWorkInfosByTagLiveData(FAVORITE_WORKER_TAG)
            .observe(view, Observer { works ->
                works.forEach { workInfo ->
                    if (workInfo.state == WorkInfo.State.SUCCEEDED) {
                        val characterId = workInfo.outputData.getInt(PARAM_ID, 0)
                        view.characterSyncStateChange(characterId, false)
                    }
                }
            })

        EventBus.subscribe<FavRemovedEvent>()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                view.uncheckFavIconFor(it.characterId)
            }
            .addTo(disposables)
    }

    private fun hasConnection(): Boolean {
        val activeNetwork: NetworkInfo? = cm.activeNetworkInfo
        return activeNetwork?.isConnected == true
    }

    override fun listCharacters(offset: Int, name: String?) {

        if (!hasConnection()) {
            view.clearDataset()
            view.showConnectionError()
            return
        }

        if (offset == 0) view.clearDataset()
        view.showLoading()

        api.listCharacters(offset, name)
            .subscribeOn(Schedulers.io())
            .toObservable()
            .flatMapIterable { it.data.results }
            .map { dto ->
                CharacterVo(
                    dto.id,
                    dto.name,
                    dto.thumbnail.run { "$path/standard_xlarge.$extension" }
                )
            }
            .flatMapMaybe { vo ->
                charactersDao.byId(vo.id)
                    .subscribeOn(Schedulers.io())
                    .map { entity ->
                        vo.apply {
                            isFavorite = true
                            syncing = entity.syncing
                        }
                    }
                    .defaultIfEmpty(vo)
            }
            .toSortedList { o1, o2 -> o1.name.compareTo(o2.name) }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                {
                    view.hideLoading()
                    view.showItems(it)
                },
                {
                    view.hideLoading()
                    view.clearDataset()
                    view.showServerError()
                }
            )
            .addTo(disposables)
    }

    override fun handleFavorite(character: CharacterVo, checked: Boolean) {
        if (checked) saveFavorite(character)
        else deleteFavorite(character.id)
    }

    override fun dispose() {
        disposables.clear()
    }

    private fun saveFavorite(character: CharacterVo) {
        api.getCharacter(character.id)
            .subscribeOn(Schedulers.io())
            .map { it.data.results.first() }
            .map {
                CharacterEntity(
                    character.id,
                    character.name,
                    it.description,
                    character.thumbUrl,
                    true
                )
            }
            .flatMapCompletable {
                charactersDao.insert(it)
                    .subscribeOn(Schedulers.io())
            }
            .subscribeOn(Schedulers.computation())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                workManager.enqueueUniqueWork(
                    character.id.toString(),
                    ExistingWorkPolicy.KEEP,
                    FavoriteSyncWorker.create(character.id)
                )
            }
            .addTo(disposables)
    }

    private fun deleteFavorite(id: Int) {
        charactersDao.delete(id)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe()
            .addTo(disposables)
    }

}