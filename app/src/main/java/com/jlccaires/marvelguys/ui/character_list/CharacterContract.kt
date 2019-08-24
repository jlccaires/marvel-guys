package com.jlccaires.marvelguys.ui.character_list

import androidx.lifecycle.LifecycleOwner
import com.jlccaires.marvelguys.ui.vo.CharacterVo

interface CharacterContract {
    interface View : LifecycleOwner {
        fun showItems(items: List<CharacterVo>)
        fun showLoading()
        fun hideLoading()
        fun clearDataset()
        fun characterSyncStateChange(characterId: Int, syncing: Boolean)
        fun uncheckFavIconFor(characterId: Int)
    }

    interface Presenter {
        fun listCharacters(offset: Int = 0, name: String? = null)
        fun handleFavorite(character: CharacterVo, checked: Boolean)
        fun dispose()
    }
}