package com.jlccaires.marvelguys.ui.favorites

import com.jlccaires.marvelguys.ui.vo.CharacterVo

interface FavoritesContract {
    interface View {
        fun showFavorites(favorites: List<CharacterVo>)
        fun hideLoading()
    }

    interface Presenter {
        fun getFavorites()
        fun handleFavorite(vo: CharacterVo)
        fun dispose()
    }
}