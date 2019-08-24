package com.jlccaires.marvelguys.ui.util

import android.view.View
import com.jlccaires.marvelguys.R
import com.jlccaires.marvelguys.gone
import com.jlccaires.marvelguys.visible
import kotlinx.android.synthetic.main.layout_ui_state.view.*

class UiStateView private constructor(
    private val view: View,
    private val tryAgainCallback: () -> Unit
) {

    init {
        view.btnTryAgain.setOnClickListener { tryAgainCallback() }
    }

    fun noConnection() {
        view.run {
            layoutStateParent.visible()
            pgrLoading.gone()
            imgStatusIcon.visible()
            txvStatus.visible()
            btnTryAgain.visible()
            imgStatusIcon.setImageResource(R.drawable.ic_no_connection)
            txvStatus.setText(R.string.no_connection)
        }
    }

    fun error() {
        view.run {
            layoutStateParent.visible()
            pgrLoading.gone()
            imgStatusIcon.visible()
            txvStatus.visible()
            btnTryAgain.visible()
            imgStatusIcon.setImageResource(R.drawable.ic_error)
            txvStatus.setText(R.string.server_error)
        }
    }

    fun empty() {
        view.run {
            layoutStateParent.visible()
            pgrLoading.gone()
            btnTryAgain.gone()
            imgStatusIcon.visible()
            txvStatus.visible()
            imgStatusIcon.setImageResource(R.drawable.ic_nothing)
            txvStatus.setText(R.string.empty_label)
        }
    }

    fun progress() {
        view.run {
            layoutStateParent.visible()
            pgrLoading.visible()
            imgStatusIcon.gone()
            txvStatus.gone()
            btnTryAgain.gone()
        }
    }

    fun hide() = view.run {
        layoutStateParent.gone()
        pgrLoading.gone()
        imgStatusIcon.gone()
        txvStatus.gone()
        btnTryAgain.gone()
    }

    companion object {
        fun from(view: View, tryAgainCallback: () -> Unit = {}) =
            UiStateView(view, tryAgainCallback)
    }
}