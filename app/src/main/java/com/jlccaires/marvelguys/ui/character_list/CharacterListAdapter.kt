package com.jlccaires.marvelguys.ui.character_list

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.jlccaires.marvelguys.R
import com.jlccaires.marvelguys.ui.vo.CharacterVo
import com.squareup.picasso.Picasso
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.layout_character_list_item.view.*


class CharacterListAdapter : RecyclerView.Adapter<CharacterListAdapter.ViewHolder>() {

    val dataset = arrayListOf<CharacterVo>()

    var onClick: ((CharacterVo) -> Unit)? = null
    var onFavStateChange: ((CharacterVo, Boolean) -> Unit)? = null

    fun addItems(items: List<CharacterVo>) {
        val size = dataset.size
        dataset.addAll(items)
        notifyItemRangeInserted(size, items.size)
    }

    fun setItems(items: List<CharacterVo>) {
        val diff = DiffUtil.calculateDiff(object : DiffUtil.Callback() {
            override fun getOldListSize() = dataset.size

            override fun getNewListSize() = items.size

            override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int) =
                dataset[oldItemPosition].id == items[newItemPosition].id

            override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int) =
                dataset[oldItemPosition] == items[newItemPosition]
        })
        dataset.clear()
        dataset.addAll(items)
        diff.dispatchUpdatesTo(this)
    }

    fun clear() {
        dataset.clear()
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.layout_character_list_item, parent, false)
        )

    override fun getItemCount() = dataset.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = dataset[position]
        holder.itemView.apply {
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

    class ViewHolder(override val containerView: View) : RecyclerView.ViewHolder(containerView),
        LayoutContainer
}