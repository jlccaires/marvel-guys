package com.jlccaires.marvelguys.ui.character_list

import android.util.Log
import com.jlccaires.marvelguys.addTo
import com.jlccaires.marvelguys.data.api.MarvelRepository
import com.jlccaires.marvelguys.data.db.dao.CharacterDao
import com.jlccaires.marvelguys.data.db.entity.CharacterEntity
import com.jlccaires.marvelguys.ui.vo.CharacterVo
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

class CharacterListPresenter(
    private val view: CharacterContract.View,
    private val repository: MarvelRepository,
    private val charactersDao: CharacterDao
) : CharacterContract.Presenter {

    private val disposables = CompositeDisposable()

    override fun listCharacters(offset: Int, name: String?) {
        view.showLoading()
        if (offset == 0) view.clearDataset()
        repository.listCharacters(offset, name)
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
        val entity = CharacterEntity(character.id, character.name, character.thumbUrl)

        (if (checked) charactersDao.insert(entity)
        else charactersDao.delete(entity.id))
            .subscribeOn(Schedulers.io())
            .subscribe(
                {
                    Log.i("Database", "database: $checked")
                },
                {}
            ).addTo(disposables)
    }

    override fun dispose() {
        disposables.clear()
    }
}