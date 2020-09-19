package com.simplu.transcribetab.edittab

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.simplu.transcribetab.database.Tablature
import com.simplu.transcribetab.database.TablatureDatabaseDao
import kotlinx.coroutines.*


class EditTabViewModel(val database: TablatureDatabaseDao) : ViewModel() {

    private val _currentSectionNum = MutableLiveData<Int>()
    val currentSectionNum: LiveData<Int> = _currentSectionNum

    private val _currentSection = MutableLiveData<ArrayList<Array<String>>>()
    val currentSection: LiveData<ArrayList<Array<String>>> = _currentSection

    private val _totalSections = MutableLiveData<Int>()
    val totalSections: LiveData<Int> = _totalSections

    val sectionValuesMap = HashMap<Int, ArrayList<Array<String>>>()

    private var viewModelJob = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    init {
        _currentSectionNum.value = 1
        _totalSections.value = 1

        val newArrayList = ArrayList<Array<String>>()
        for (i in 0..9) {
            val newColumn = Array<String>(6) { "" }
            newArrayList.add(newColumn)
        }
        _currentSection.value = newArrayList
    }

    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }

    private fun createNewSection() {

        val newArrayList = ArrayList<Array<String>>()
        for (i in 0..9) {
            val newColumn = Array<String>(6) { "" }
            newArrayList.add(newColumn)
        }
        _currentSection.value = newArrayList
    }


    fun onSave(tab: Tablature) {
        storeCurrentSectionColumns()
        uiScope.launch {
            insert(tab)
        }
    }

    private suspend fun insert(tab: Tablature) {
        withContext(Dispatchers.IO) {
            database.insert(tab)
        }
    }

    public fun insertAt(column: Int, string: Int, value: String) {
        _currentSection.value!!.get(column)[string] = value
        _currentSection.value = _currentSection.value
    }

    public fun nextSection() {
        var sectionVal = currentSectionNum.value as Int
        if (sectionVal < totalSections.value!!) {
            storeCurrentSectionColumns()
            _currentSectionNum.value = ++sectionVal
            val nextSection = sectionValuesMap.get(sectionVal)!!
            _currentSection.value = nextSection

        }
    }

    public fun previousSection() {
        var sectionVal = currentSectionNum.value as Int
        if (sectionVal > 1) {
            storeCurrentSectionColumns()
            _currentSectionNum.value = --sectionVal
            val prevSection = sectionValuesMap.get(sectionVal)!!
            _currentSection.value = prevSection
        }
    }

    public fun addSection() {

        _currentSectionNum.value = totalSections.value!! + 1
        _totalSections.value = _totalSections.value!! + 1
        createNewSection()

    }

    private fun storeCurrentSectionColumns() {
        val currentSection = _currentSectionNum.value!!
        val tabSection = _currentSection.value!!
        sectionValuesMap.put(currentSection, tabSection)
    }
}