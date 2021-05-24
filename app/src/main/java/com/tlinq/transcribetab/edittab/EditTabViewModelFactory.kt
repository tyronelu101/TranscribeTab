package com.tlinq.transcribetab.edittab

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.tlinq.transcribetab.database.Tablature
import com.tlinq.transcribetab.database.TablatureRepository

class EditTabViewModelFactory(
    private val repository: TablatureRepository,
    private val tablature: Tablature?,
    private val numberOfColumns: Int
) : ViewModelProvider.Factory {
    @Suppress("unchecked_cast")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(EditTabViewModel::class.java)) {
            return EditTabViewModel(repository, tablature, numberOfColumns) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}