package com.simplu.transcribetab.tablist

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.simplu.transcribetab.database.Tablature
import com.simplu.transcribetab.databinding.TabItemViewBinding


class TablatureAdapter(val clickListener: TablatureListener) :
    ListAdapter<Tablature, TablatureAdapter.ViewHolder>(TablatureDiffUtilCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item, clickListener)
    }

    class ViewHolder private constructor(val binding: TabItemViewBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Tablature, clickListener: TablatureListener) {
            binding.tablature = item
            binding.clickListener = clickListener
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

class TablatureListener(val clickListener: (tab: Tablature) -> Unit) {
    fun onClick(tab: Tablature) = clickListener(tab)
}