package com.jlccaires.marvelguys.ui.characterDetail


import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.jlccaires.marvelguys.R
import com.jlccaires.marvelguys.ui.BaseFragment
import com.jlccaires.marvelguys.ui.vo.DetailItemVo
import com.jlccaires.marvelguys.visible
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.fragment_character_detail.*
import org.koin.android.ext.android.inject
import org.koin.core.parameter.parametersOf

class CharacterDetailFragment : BaseFragment(R.layout.fragment_character_detail),
    CharacterDetailContract.View {

    private val args: CharacterDetailFragmentArgs by navArgs()
    private val presenter: CharacterDetailContract.Presenter by inject { parametersOf(this) }

    private val comicsAdapter = DetailItemAdapter()
    private val seriesAdapter = DetailItemAdapter()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        rcvComics.adapter = comicsAdapter
        rcvSeries.adapter = seriesAdapter
        comicsAdapter.setOnLoadMoreListener({
            presenter.loadComics(args.characterId, comicsAdapter.data.size)
        }, rcvComics)
        seriesAdapter.setOnLoadMoreListener({
            presenter.loadSeries(args.characterId, seriesAdapter.data.size)
        }, rcvSeries)

        presenter.loadCharacterData(args.characterId)
    }

    override fun loadSuccess() {
        presenter.loadComics(args.characterId)
        presenter.loadSeries(args.characterId)
    }

    override fun loadError() {
        findNavController().navigateUp()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        presenter.dispose()
    }

    override fun showImage(url: String) {
        Picasso.get()
            .load(url)
            .into(imgCharacter)
    }

    override fun setTitle(title: String) {
        txvCharacterName.visible()
        txvCharacterName.text = title
    }

    override fun setDescription(description: String) {
        txvCharacterDescription.visible()
        txvCharacterDescription.text = description
    }

    override fun showComics(items: List<DetailItemVo>) {
        txtComics.visible()
        rcvComics.visible()
        comicsAdapter.loadMoreComplete()
        comicsAdapter.addData(items)
    }

    override fun showSeries(items: List<DetailItemVo>) {
        txtSeries.visible()
        rcvSeries.visible()
        seriesAdapter.loadMoreComplete()
        seriesAdapter.addData(items)
    }

    override fun noMoreComics() {
        comicsAdapter.loadMoreComplete()
        comicsAdapter.loadMoreEnd(true)
    }

    override fun noMoreSeries() {
        seriesAdapter.loadMoreComplete()
        seriesAdapter.loadMoreEnd(true)
    }
}
