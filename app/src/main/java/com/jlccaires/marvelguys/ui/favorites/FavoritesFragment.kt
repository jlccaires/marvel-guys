package com.jlccaires.marvelguys.ui.favorites

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.jlccaires.marvelguys.R
import com.jlccaires.marvelguys.gone
import com.jlccaires.marvelguys.ui.BaseFragment
import com.jlccaires.marvelguys.ui.character_list.CharacterListAdapter
import com.jlccaires.marvelguys.ui.main.MainFragmentDirections
import com.jlccaires.marvelguys.ui.vo.CharacterVo
import kotlinx.android.synthetic.main.fragment_character_list.*
import org.koin.android.ext.android.inject
import org.koin.core.parameter.parametersOf

class FavoritesFragment : BaseFragment(R.layout.fragment_favorites), FavoritesContract.View {

    private val presenter: FavoritesContract.Presenter by inject { parametersOf(this) }
    private val mAdapter: CharacterListAdapter by inject()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        rcvCharacters.apply {
            layoutManager = GridLayoutManager(activity, 2)
            adapter = mAdapter
        }
        mAdapter.onClick = {
            findNavController().navigate(
                MainFragmentDirections.actionMainFragmentToCharacterDetailFragment(
                    it.id
                )
            )
        }
        mAdapter.onFavStateChange = { character: CharacterVo, checked: Boolean ->
        }
        presenter.getFavorites()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        presenter.dispose()
    }

    override fun showFavorites(favorites: List<CharacterVo>) {
        mAdapter.setItems(favorites)
    }

    override fun hideLoading() {
        bottomProgress.gone()
    }
}