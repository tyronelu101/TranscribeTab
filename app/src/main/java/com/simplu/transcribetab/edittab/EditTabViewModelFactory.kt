package com.simplu.transcribetab.edittab

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.simplu.transcribetab.database.TablatureDatabaseDao

class EditTabViewModelFactory(
    private val dataSource: TablatureDatabaseDao
) : ViewModelProvider.Factory {
    @Suppress("unchecked_cast")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(EditTabViewModel::class.java)) {
            return EditTabViewModel(dataSource) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}