package com.jlccaires.marvelguys.ui.characterDetail

import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.jlccaires.marvelguys.R
import com.jlccaires.marvelguys.ui.vo.DetailItemVo
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.layout_character_list_item.view.*


class DetailItemAdapter :
    BaseQuickAdapter<DetailItemVo, BaseViewHolder>(R.layout.layout_detail_item) {

    override fun convert(helper: BaseViewHolder, item: DetailItemVo) {
        helper.itemView.run {
            txvName.text = item.name
            Picasso.get()
                .load(item.thumbUrl)
                .into(imgCharacter)
        }
    }

}