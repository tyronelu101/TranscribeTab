package com.simplu.transcribetab.tablist

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simplu.transcribetab.database.Tablature
import com.simplu.transcribetab.database.TablatureRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class TabListViewModel(private val tablatureRepository: TablatureRepository) : ViewModel() {

    lateinit var tabList: LiveData<List<Tablature>>

    init {
        loadTabs()
    }

    private fun loadTabs() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                tabList = tablatureRepository.getAllTab()
            }
        }
    }

    fun deleteTab(tab: Tablature) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                tablatureRepository.delete(tab)
            }
        }
    }
}