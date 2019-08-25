package com.jlccaires.marvelguys.ui.characterDetail

import com.jlccaires.marvelguys.addTo
import com.jlccaires.marvelguys.data.api.MarvelAPI
import com.jlccaires.marvelguys.data.db.dao.CharacterDao
import com.jlccaires.marvelguys.ui.vo.DetailItemVo
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

class CharacterDetailPresenter(
    private val view: CharacterDetailContract.View,
    private val api: MarvelAPI,
    private val dao: CharacterDao
) : CharacterDetailContract.Presenter {

    private val disposables = CompositeDisposable()

    private fun loadFromApi(id: Int) {
        api.getCharacter(id)
            .subscribeOn(Schedulers.io())
            .retry(3)
            .map { it.data.results.first() }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { character ->
                    view.loadSuccess()
                    view.showImage(
                        character.thumbnail.run { "$path/landscape_incredible.$extension" }
                    )
                    view.setTitle(character.name)
                    if (character.description.isNotEmpty()) {
                        view.setDescription(character.description)
                    }
                },
                {
                    view.loadError()
                }
            )
            .addTo(disposables)
    }

    private fun loadFromDb(id: Int) {
        dao.embededById(id)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { entity ->
                view.loadSuccess()
                view.showImage(
                    entity.characterEntity
                        .thumbUrl
                        .replace("standard_xlarge", "landscape_incredible")
                )
                view.setTitle(entity.characterEntity.name)
                entity.characterEntity.description.run {
                    if (isNotEmpty()) {
                        view.setDescription(this)
                    }
                }
                view.showComics(entity.comics.map {
                    DetailItemVo(it.thumbUrl, it.name)
                })
                view.noMoreComics()
                view.showSeries(entity.series.map {
                    DetailItemVo(it.thumbUrl, it.name)
                })
                view.noMoreSeries()
            }
            .addTo(disposables)
    }

    override fun loadCharacterData(id: Int) {
        dao.byId(id)
            .subscribeOn(Schedulers.io())
            .map { !it.syncing }
            .switchIfEmpty(Single.just(false))
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { fromDatabase ->
                if (fromDatabase) {
                    loadFromDb(id)
                } else {
                    loadFromApi(id)
                }
            }
            .addTo(disposables)
    }

    override fun dispose() {
        disposables.clear()
    }

    override fun loadComics(characterId: Int, offset: Int) {
        api.getComics(characterId, offset, 20)
            .subscribeOn(Schedulers.io())
            .map { it.data.results }
            .map { list ->
                list.map {
                    DetailItemVo(
                        it.thumbnail.run { "$path/portrait_xlarge.$extension" },
                        it.title
                    )
                }
            }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { list ->
                if (list.isEmpty()) {
                    view.noMoreComics()
                    return@subscribe
                }
                view.showComics(list)
            }
            .addTo(disposables)
    }

    override fun loadSeries(characterId: Int, offset: Int) {
        api.getSeries(characterId, offset, 20)
            .subscribeOn(Schedulers.io())
            .retry(3)
            .map { it.data.results }
            .map { list ->
                list.map {
                    DetailItemVo(
                        it.thumbnail.run { "$path/portrait_xlarge.$extension" },
                        it.title
                    )
                }
            }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { list ->
                if (list.isEmpty()) {
                    view.noMoreSeries()
                    return@subscribe
                }
                view.showSeries(list)
            }
            .addTo(disposables)
    }
}