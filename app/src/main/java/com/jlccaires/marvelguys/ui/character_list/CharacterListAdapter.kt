package com.jlccaires.marvelguys.ui.character_list

import androidx.recyclerview.widget.DiffUtil
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.jlccaires.marvelguys.R
import com.jlccaires.marvelguys.ui.vo.CharacterVo
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.layout_character_list_item.view.*


class CharacterListAdapter :
    BaseQuickAdapter<CharacterVo, BaseViewHolder>(R.layout.layout_character_list_item) {

    var onClick: ((CharacterVo) -> Unit)? = null
    var onFavStateChange: ((CharacterVo, Boolean) -> Unit)? = null

    override fun convert(helper: BaseViewHolder, item: CharacterVo) {
        helper.itemView.apply {
            txvName.text = item.name
            togFav.run {
                setOnCheckedChangeListener(null)
                isChecked = item.isFavorite
                setOnCheckedChangeListener { _, isChecked ->
                    onFavStateChange?.invoke(item, isChecked)
                }
            }
            Picasso.get()
                .load(item.thumbUrl)
                .into(imgCharacter)
            setOnClickListener { onClick?.invoke(item) }
        }
    }

    fun setItems(items: List<CharacterVo>) {
        val diff = DiffUtil.calculateDiff(object : DiffUtil.Callback() {
            override fun getOldListSize() = data.size

            override fun getNewListSize() = items.size

            override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int) =
                data[oldItemPosition].id == items[newItemPosition].id

            override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int) =
                data[oldItemPosition] == items[newItemPosition]
        })
        setNewDiffData(diff, items)
    }

    fun clear() {
        data.clear()
        notifyDataSetChanged()
    }
}