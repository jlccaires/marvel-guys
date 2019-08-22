package com.jlccaires.marvelguys.ui.character_detail


import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.navArgs
import com.jlccaires.marvelguys.R
import com.jlccaires.marvelguys.ui.BaseFragment

class CharacterDetailFragment : BaseFragment(R.layout.fragment_character_detail) {

    val args: CharacterDetailFragmentArgs by navArgs()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    }
}
