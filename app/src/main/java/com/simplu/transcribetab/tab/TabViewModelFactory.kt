package com.simplu.transcribetab.tab

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.simplu.transcribetab.database.TablatureDatabaseDao

class TabViewModelFactory(
    private val dataSource: TablatureDatabaseDao, private val tabId: Long
) : ViewModelProvider.Factory {
    @Suppress("unchecked_cast")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TabViewModel::class.java)) {
            return TabViewModel(dataSource, tabId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}