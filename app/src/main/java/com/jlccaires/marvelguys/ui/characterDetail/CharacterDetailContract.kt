package com.jlccaires.marvelguys.ui.characterDetail

import com.jlccaires.marvelguys.ui.vo.DetailItemVo

interface CharacterDetailContract {
    interface View {
        fun showImage(url: String)
        fun setTitle(title: String)
        fun setDescription(description: String)
        fun loadSuccess()
        fun loadError()
        fun showComics(items: List<DetailItemVo>)
        fun showSeries(items: List<DetailItemVo>)
        fun noMoreComics()
        fun noMoreSeries()
    }

    interface Presenter {
        fun loadCharacterData(id: Int)
        fun loadComics(characterId: Int, offset: Int = 0)
        fun loadSeries(characterId: Int, offset: Int = 0)
        fun dispose()
    }
}