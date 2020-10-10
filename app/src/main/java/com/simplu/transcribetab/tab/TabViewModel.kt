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

    var sectionValuesMap = HashMap<Int, ArrayList<Array<String>>>()
    var sectionTimeMap = HashMap<Int, Pair<Int, Int>>()

    private var currentTab = 1
    private var _timeToWatch = MutableLiveData<Int?>()
    var timeToWatch: LiveData<Int?> = _timeToWatch

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
            val tablature = getTablature()
            _tablature.value = tablature

            sectionValuesMap = tablature.sections
            sectionTimeMap = tablature.sectionToTimeMap

            //Initialize the first 2 sections
            if (sectionValuesMap != null) {
                _topTabValues.value = sectionValuesMap.get(1)
                _bottomTabValues.value = sectionValuesMap.get(2)
            }
            if (sectionTimeMap != null) {
                _timeToWatch.value = sectionTimeMap.get(currentTab)?.second;
            }
        }
    }

    private suspend fun getTablature(): Tablature {
        return withContext(Dispatchers.IO) {
            database.get(tabId)
        }
    }

    fun update() {

    }

}