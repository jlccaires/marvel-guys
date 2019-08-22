package com.jlccaires.marvelguys.ui.main


import android.os.Bundle
import android.view.View
import com.jlccaires.marvelguys.R
import com.jlccaires.marvelguys.ui.BaseFragment
import kotlinx.android.synthetic.main.fragment_main.*

class MainFragment : BaseFragment(R.layout.fragment_main) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        tabLayout.setupWithViewPager(pager)
        pager.adapter = MainPagerAdapter(childFragmentManager)
    }
}
