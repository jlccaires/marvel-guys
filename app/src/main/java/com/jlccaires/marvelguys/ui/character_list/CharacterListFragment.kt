package com.jlccaires.marvelguys.ui.character_list


import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.appcompat.widget.SearchView
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.jlccaires.marvelguys.R
import com.jlccaires.marvelguys.ui.BaseFragment
import com.jlccaires.marvelguys.ui.main.MainFragmentDirections
import com.jlccaires.marvelguys.ui.util.UiStateView
import com.jlccaires.marvelguys.ui.vo.CharacterVo
import kotlinx.android.synthetic.main.fragment_character_list.*
import org.koin.android.ext.android.inject
import org.koin.core.parameter.parametersOf

class CharacterListFragment : BaseFragment(R.layout.fragment_character_list),
    CharacterContract.View {

    private val mAdapter: CharacterListAdapter by inject()
    private val presenter: CharacterContract.Presenter by inject { parametersOf(this) }

    private lateinit var uiState: UiStateView
    private var searchString: String? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setHasOptionsMenu(true)
        uiState = UiStateView.from(view) {
            presenter.listCharacters()
        }
        rcvCharacters.apply {
            layoutManager = GridLayoutManager(activity, 2)
            adapter = mAdapter
        }
        swipeRefresh.setOnRefreshListener {
            swipeRefresh.isRefreshing = false
            presenter.listCharacters(name = searchString)
        }
        mAdapter.apply {
            setPreLoadNumber(4)
            loadMoreEnd()
            onClick = {
                findNavController().navigate(
                    MainFragmentDirections.actionMainFragmentToCharacterDetailFragment(
                        it.id
                    )
                )
            }
            onFavStateChange = { character: CharacterVo, checked: Boolean ->
                presenter.handleFavorite(character, checked)
            }
            mAdapter.setOnLoadMoreListener(
                {
                    presenter.listCharacters(mAdapter.data.size, searchString)
                }, rcvCharacters
            )
        }

        presenter.listCharacters()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_main, menu)
        val searchItem = menu.findItem(R.id.action_search)
        val searchView = searchItem.actionView as SearchView

        searchItem.setOnActionExpandListener(object : MenuItem.OnActionExpandListener {
            override fun onMenuItemActionExpand(item: MenuItem) = true
            override fun onMenuItemActionCollapse(item: MenuItem?): Boolean {
                searchString = null
                presenter.listCharacters()
                return true
            }
        })
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextChange(newText: String) = false
            override fun onQueryTextSubmit(query: String): Boolean {
                searchString = searchView.query.toString().ifEmpty { null }
                presenter.listCharacters(name = searchString)
                return true
            }
        })
        return super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        presenter.dispose()
    }

    override fun showItems(items: List<CharacterVo>) {
        if (items.isEmpty()) {
            mAdapter.loadMoreEnd(true)
            if (mAdapter.data.isEmpty()) {
                uiState.empty()
            }
            return
        }
        uiState.hide()
        mAdapter.addData(items)
    }

    override fun showLoading() {
        if (mAdapter.data.isEmpty()) {
            uiState.progress()
        }
    }

    override fun hideLoading() {
        swipeRefresh.isRefreshing = false
        mAdapter.loadMoreComplete()
    }

    override fun clearDataset() {
        mAdapter.clear()
    }

    override fun characterSyncStateChange(characterId: Int, syncing: Boolean) {
        val position = mAdapter.data.indexOfFirst { it.id == characterId }
        if (position < 0) return
        mAdapter.getItem(position)?.syncing = syncing
        mAdapter.notifyItemChanged(position)
    }

    override fun uncheckFavIconFor(characterId: Int) {
        val position = mAdapter.data.indexOfFirst { it.id == characterId }
        if (position < 0) return
        mAdapter.getItem(position)?.isFavorite = false
        mAdapter.notifyItemChanged(position)
    }

    override fun showConnectionError() {
        uiState.noConnection()
    }

    override fun showServerError() {
        uiState.error()
    }
}
