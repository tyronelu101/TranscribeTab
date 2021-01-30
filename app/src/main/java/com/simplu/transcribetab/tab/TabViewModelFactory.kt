package com.simplu.transcribetab.tab

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.simplu.transcribetab.database.Tablature
import com.simplu.transcribetab.database.TablatureDatabase

class TabViewModelFactory(
    private val tablature: Tablature
    , private val database: TablatureDatabase
) : ViewModelProvider.Factory {
    @Suppress("unchecked_cast")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TabViewModel::class.java)) {
            return TabViewModel(tablature, database) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}