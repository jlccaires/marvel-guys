package com.jlccaires.marvelguys.ui.character_list

import com.jlccaires.marvelguys.ui.vo.CharacterVo

interface CharacterContract {
    interface View {
        fun showItems(items: List<CharacterVo>)
        fun showLoading()
        fun hideLoading()
        fun clearDataset()
    }

    interface Presenter {
        fun listCharacters(offset: Int = 0, name: String? = null)
        fun handleFavorite(character: CharacterVo, checked: Boolean)
        fun dispose()
    }
}