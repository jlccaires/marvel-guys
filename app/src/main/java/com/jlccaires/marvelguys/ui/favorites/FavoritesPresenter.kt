package com.jlccaires.marvelguys.ui.favorites

import android.util.Log
import com.jlccaires.marvelguys.addTo
import com.jlccaires.marvelguys.data.db.dao.CharacterDao
import com.jlccaires.marvelguys.ui.vo.CharacterVo
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

class FavoritesPresenter(
    private val view: FavoritesContract.View,
    private val characterDao: CharacterDao
) : FavoritesContract.Presenter {

    private val disposables = CompositeDisposable()

    override fun getFavorites() {
        characterDao.list()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { list ->
                    view.showFavorites(list.map {
                        CharacterVo(it.id, it.name, it.thumbUrl, true, it.syncing)
                    })
                    view.hideLoading()
                },
                {
                    Log.e("Favs", "", it)
                }
            )
            .addTo(disposables)
    }

    override fun handleFavorite(vo: CharacterVo) {
        characterDao.delete(vo.id)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe()
            .addTo(disposables)
    }

    override fun dispose() {
        disposables.clear()
    }
}