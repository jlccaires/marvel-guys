package com.jlccaires.marvelguys.ui.character_list

import android.view.animation.AnimationUtils
import androidx.recyclerview.widget.DiffUtil
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.jlccaires.marvelguys.R
import com.jlccaires.marvelguys.gone
import com.jlccaires.marvelguys.ui.vo.CharacterVo
import com.jlccaires.marvelguys.visible
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
                isEnabled = !item.syncing
                setOnCheckedChangeListener { _, isChecked ->
                    item.isFavorite = isChecked
                    item.syncing = isChecked
                    notifyItemChanged(helper.adapterPosition)
                    onFavStateChange?.invoke(item, isChecked)
                }
            }
            if (item.syncing) {
                imgSync.startAnimation(AnimationUtils.loadAnimation(context, R.anim.anim_rotate))
                imgSync.visible()
            } else {
                imgSync.gone()
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