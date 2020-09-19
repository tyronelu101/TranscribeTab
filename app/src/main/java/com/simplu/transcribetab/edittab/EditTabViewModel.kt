package com.simplu.transcribetab.edittab

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.simplu.transcribetab.database.Tablature
import com.simplu.transcribetab.database.TablatureDatabaseDao
import kotlinx.coroutines.*


class EditTabViewModel(val database: TablatureDatabaseDao) : ViewModel() {

    private val _currentSectionObs = MutableLiveData<ArrayList<Array<String>>>()
    val currentSectionObs: LiveData<ArrayList<Array<String>>> = _currentSectionObs

    private val _currentSectionNumObs = MutableLiveData<Int>()
    val currentSectionNumObs: LiveData<Int> = _currentSectionNumObs

    private val _totalSectionsObs = MutableLiveData<Int>()
    val totalSectionsObs: LiveData<Int> = _totalSectionsObs

    val sectionValuesMap = HashMap<Int, ArrayList<Array<String>>>()

    private var currentSection = ArrayList<Array<String>>()
    private var currentSectionNum = 1
    private var totalSections = 1

    private var viewModelJob = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    init {
        createNewSection()
        _currentSectionNumObs.value = currentSectionNum
        _totalSectionsObs.value = totalSections
    }

    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }

    public fun addSection() {
        createNewSection()

        ++totalSections
        currentSectionNum = totalSections
        _currentSectionNumObs.value = totalSections
        _totalSectionsObs.value = totalSections
    }

    private fun createNewSection() {

        val newArrayList = ArrayList<Array<String>>()
        for (i in 0..EditTabView.NUMBER_OF_COLUMNS) {
            val newColumn = Array<String>(6) { "" }
            newArrayList.add(newColumn)
        }
        currentSection = newArrayList
        _currentSectionObs.value = newArrayList
        storeCurrentSection()

        Log.v("ViewModel", "Size of map is ${sectionValuesMap.size}")
    }

    public fun insertAt(column: Int, string: Int, value: String) {
        currentSection.get(column)[string] = value
        _currentSectionObs.value = currentSection
    }

    public fun nextSection() {
        if (currentSectionNum < totalSections) {
            Log.v("ViewMode", "Next Section")
            storeCurrentSection()
            _currentSectionNumObs.value = ++currentSectionNum
            val nextSection = sectionValuesMap.get(currentSectionNum)
            _currentSectionObs.value = nextSection
        }
    }

    public fun previousSection() {
        if (currentSectionNum > 1) {
            storeCurrentSection()
            _currentSectionNumObs.value = --currentSectionNum
            val prevSection = sectionValuesMap.get(currentSectionNum)
            _currentSectionObs.value = prevSection
        }
    }

    private fun storeCurrentSection() {
        sectionValuesMap.put(currentSectionNum, currentSection)
    }

    fun onSave(tab: Tablature) {
        storeCurrentSection()
        uiScope.launch {
            insert(tab)
        }
    }

    private suspend fun insert(tab: Tablature) {
        withContext(Dispatchers.IO) {
            database.insert(tab)
        }
    }
}