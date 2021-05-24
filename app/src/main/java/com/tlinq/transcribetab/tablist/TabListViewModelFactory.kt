package com.tlinq.transcribetab.tablist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.tlinq.transcribetab.database.TablatureRepository


class TabListViewModelFactory(
    private val repository: TablatureRepository
) : ViewModelProvider.Factory {
    @Suppress("unchecked_cast")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TabListViewModel::class.java)) {
            return TabListViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}