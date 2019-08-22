package com.jlccaires.marvelguys.ui.main

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import com.jlccaires.marvelguys.ui.character_list.CharacterListFragment
import com.jlccaires.marvelguys.ui.favorites.FavoritesFragment

class MainPagerAdapter(fm: FragmentManager) :
    FragmentStatePagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

    override fun getItem(position: Int): Fragment {
        return when (position) {
            0 -> CharacterListFragment()
            1 -> FavoritesFragment()
            else -> Fragment()
        }
    }

    override fun getPageTitle(position: Int): CharSequence {
        return when (position) {
            0 -> "Characters"
            1 -> "Favorites"
            else -> "???"
        }
    }

    override fun getCount() = 2
}