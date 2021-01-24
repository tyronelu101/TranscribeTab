package com.simplu.transcribetab.edittab

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.simplu.transcribetab.database.Tablature
import com.simplu.transcribetab.database.TablatureRepository

class EditTabViewModelFactory(
    private val repository: TablatureRepository,
    private val tablature: Tablature?
) : ViewModelProvider.Factory {
    @Suppress("unchecked_cast")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(EditTabViewModel::class.java)) {
            return EditTabViewModel(repository, tablature) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}