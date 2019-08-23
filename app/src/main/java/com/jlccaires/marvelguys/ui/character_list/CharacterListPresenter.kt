package com.jlccaires.marvelguys.ui.character_list

import android.util.Log
import androidx.work.ExistingWorkPolicy
import androidx.work.WorkManager
import com.jlccaires.marvelguys.addTo
import com.jlccaires.marvelguys.data.api.MarvelAPI
import com.jlccaires.marvelguys.data.db.dao.CharacterDao
import com.jlccaires.marvelguys.data.db.entity.CharacterEntity
import com.jlccaires.marvelguys.data.workmanager.FavoriteSyncWorker
import com.jlccaires.marvelguys.ui.vo.CharacterVo
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

class CharacterListPresenter(
    private val view: CharacterContract.View,
    private val api: MarvelAPI,
    private val charactersDao: CharacterDao
) : CharacterContract.Presenter {

    private val disposables = CompositeDisposable()

    override fun listCharacters(offset: Int, name: String?) {
        view.showLoading()
        if (offset == 0) view.clearDataset()
        api.listCharacters(offset, name)
            .subscribeOn(Schedulers.io())
            .toObservable()
            .flatMapIterable { it.data.results }
            .flatMapSingle { dto ->
                charactersDao.exists(dto.id)
                    .subscribeOn(Schedulers.io())
                    .map {
                        CharacterVo(
                            dto.id,
                            dto.name,
                            dto.thumbnail.run { "$path/standard_xlarge.$extension" },
                            it > 0
                        )
                    }
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
                    character.thumbUrl
                )
            }
            .flatMapCompletable {
                charactersDao.insert(it)
                    .subscribeOn(Schedulers.io())
            }
            .subscribeOn(Schedulers.computation())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({

                WorkManager.getInstance().enqueueUniqueWork(
                    character.id.toString(),
                    ExistingWorkPolicy.KEEP,
                    FavoriteSyncWorker.create(character.id)
                )
                cb(false)
            }, { cb(true) })
            .addTo(disposables)
    }

    private fun deleteFavorite(id: Int, cb: (Boolean) -> Unit) {
        WorkManager.getInstance().cancelUniqueWork(id.toString())
        charactersDao.delete(id)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ cb(false) }, { cb(true) })
            .addTo(disposables)
    }
}