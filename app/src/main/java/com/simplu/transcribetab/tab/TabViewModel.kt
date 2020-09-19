package com.simplu.transcribetab.tab

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.simplu.transcribetab.database.Tablature
import com.simplu.transcribetab.database.TablatureDatabaseDao
import kotlinx.coroutines.*


class TabViewModel(val database: TablatureDatabaseDao, val tabId: Long) : ViewModel() {

    private val _topTabValues: MutableLiveData<ArrayList<Array<String>>> = MutableLiveData()
    val topTabValues: LiveData<ArrayList<Array<String>>> = _topTabValues

    private val _bottomTabValues: MutableLiveData<ArrayList<Array<String>>> = MutableLiveData()
    val bottomTabValues: LiveData<ArrayList<Array<String>>> = _bottomTabValues

    private var _tablature: MutableLiveData<Tablature> = MutableLiveData()
    val tablature: LiveData<Tablature> = _tablature

    var sectionMap: HashMap<Int, ArrayList<Array<String>>>? = null

    private var nextTabSectionIndex = 3
    var timeToWatch = 30

    private var viewModelJob = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }

    init {
        initializeTablature()
    }

    private fun initializeTablature() {
        uiScope.launch {
            _tablature.value = getTablature()
            sectionMap = _tablature?.value?.columns

            //Initialize the first 2 sections
            if (sectionMap != null) {
                _topTabValues.value = sectionMap?.get(1)
                _bottomTabValues.value = sectionMap?.get(2)
            }
        }
    }

    private suspend fun getTablature(): Tablature {
        return withContext(Dispatchers.IO) {
            database.get(tabId)
        }
    }

}