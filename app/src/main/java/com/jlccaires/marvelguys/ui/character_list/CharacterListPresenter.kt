package com.jlccaires.marvelguys.ui.character_list

import android.util.Log
import androidx.lifecycle.Observer
import androidx.work.ExistingWorkPolicy
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.jlccaires.marvelguys.addTo
import com.jlccaires.marvelguys.data.EventBus
import com.jlccaires.marvelguys.data.api.MarvelAPI
import com.jlccaires.marvelguys.data.db.dao.CharacterDao
import com.jlccaires.marvelguys.data.db.entity.CharacterEntity
import com.jlccaires.marvelguys.data.worker.FavoriteSyncWorker
import com.jlccaires.marvelguys.data.worker.FavoriteSyncWorker.Companion.FAVORITE_WORKER_TAG
import com.jlccaires.marvelguys.data.worker.FavoriteSyncWorker.Companion.PARAM_ID
import com.jlccaires.marvelguys.ui.favorites.FavRemovedEvent
import com.jlccaires.marvelguys.ui.vo.CharacterVo
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

class CharacterListPresenter(
    private val view: CharacterContract.View,
    private val api: MarvelAPI,
    private val workManager: WorkManager,
    private val charactersDao: CharacterDao
) : CharacterContract.Presenter {

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

    override fun listCharacters(offset: Int, name: String?) {
        view.showLoading()
        if (offset == 0) view.clearDataset()
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
                    view.showItems(it)
                    view.hideLoading()
                },
                {

                }
            )
            .addTo(disposables)
    }

    override fun handleFavorite(character: CharacterVo, checked: Boolean) {
        val cb: (error: Boolean) -> Unit = {
            Log.i("Favorite", "Save success: ${!it}")
        }
        if (checked) saveFavorite(character, cb)
        else deleteFavorite(character.id, cb)
    }

    override fun dispose() {
        disposables.clear()
    }

    private fun saveFavorite(character: CharacterVo, cb: (Boolean) -> Unit) {

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
            .subscribe({

                workManager.enqueueUniqueWork(
                    character.id.toString(),
                    ExistingWorkPolicy.KEEP,
                    FavoriteSyncWorker.create(character.id)
                )

                view.characterSyncStateChange(character.id, true)

                cb(false)
            }, { cb(true) })
            .addTo(disposables)
    }

    private fun deleteFavorite(id: Int, cb: (Boolean) -> Unit) {
        workManager.cancelUniqueWork(id.toString())
        charactersDao.delete(id)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ cb(false) }, { cb(true) })
            .addTo(disposables)
    }

}