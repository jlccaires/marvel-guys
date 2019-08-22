package com.jlccaires.marvelguys.ui.favorites

import com.jlccaires.marvelguys.addTo
import com.jlccaires.marvelguys.data.db.dao.CharacterDao
import com.jlccaires.marvelguys.ui.vo.CharacterVo
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

class FavoritesPresenter(
    private val view: FavoritesContract.View,
    private val dao: CharacterDao
) : FavoritesContract.Presenter {

    private val disposables = CompositeDisposable()

    override fun getFavorites() {
        dao.list()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { list ->
                    view.showFavorites(list.map { CharacterVo(it.id, it.name, null) })
                    view.hideLoading()
                },
                {

                }
            )
            .addTo(disposables)
    }

    override fun dispose() {
        disposables.clear()
    }
}