package com.tlinq.transcribetab.tablist

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.tlinq.transcribetab.database.Tablature
import com.tlinq.transcribetab.databinding.TabItemViewBinding


class TablatureAdapter(val clickListener: TabClickListener, val longClickListener: TabItemContextMenuListener) :
    ListAdapter<Tablature, TablatureAdapter.ViewHolder>(TablatureDiffUtilCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item, clickListener, longClickListener)
    }

    class ViewHolder private constructor(val binding: TabItemViewBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Tablature, clickListener: TabClickListener, longClickListener: TabItemContextMenuListener) {
            binding.tablature = item
            binding.clickListener = clickListener
            binding.longClickListener = longClickListener
        }

        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = TabItemViewBinding.inflate(layoutInflater, parent, false)
                return ViewHolder(binding)
            }
        }
    }
}

class TablatureDiffUtilCallback : DiffUtil.ItemCallback<Tablature>() {
    override fun areContentsTheSame(oldItem: Tablature, newItem: Tablature): Boolean {
        return oldItem.tabId == newItem.tabId
    }

    override fun areItemsTheSame(oldItem: Tablature, newItem: Tablature): Boolean {
        return oldItem == newItem
    }
}

class TabClickListener(val clickListener: (tab: Tablature) -> Unit) {
    fun onClick(tab: Tablature) = clickListener(tab)
}

class TabItemContextMenuListener(val longClickListener: (tab: Tablature) -> Boolean){
    fun onLongClick(tab: Tablature): Boolean = longClickListener(tab)
}