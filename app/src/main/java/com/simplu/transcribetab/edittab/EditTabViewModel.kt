package com.simplu.transcribetab.edittab

import android.text.format.DateUtils
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.simplu.transcribetab.TabSection
import com.simplu.transcribetab.database.Tablature
import com.simplu.transcribetab.database.TablatureDatabaseDao
import kotlinx.coroutines.*

class EditTabViewModel(val database: TablatureDatabaseDao, var tablature: Tablature? = null) :
    ViewModel() {

    private val _currentSection = MutableLiveData<TabSection>()
    val currentSection: LiveData<TabSection> = _currentSection

    private val _totalSectionNum = MutableLiveData<Int>()
    val totalSectionsNum: LiveData<Int> = _totalSectionNum

    val currentSectionNum: LiveData<String> = Transformations.map(currentSection) {
        it.sectionNum.toString()
    }

    val currentSectionTime: LiveData<String> = Transformations.map(currentSection) {
        DateUtils.formatElapsedTime(currentSection.value!!.sectionTime.toLong()).removeRange(0, 1)
    }

    private val _skipToVal = MutableLiveData<Int>()
    val skipToVal: LiveData<Int> = _skipToVal

    var sectionMap = HashMap<Int, TabSection>()

    private var viewModelJob = Job()
    private val uiScope = CoroutineScope(Dispatchers.IO + viewModelJob)

    init {
        initializeTablature()
    }

    private fun initializeTablature() {
        //Editting existing tablature
        if (tablature != null) {
            sectionMap = tablature!!.sections
            _totalSectionNum.value = tablature!!.sections.size
            _currentSection.value = sectionMap[1]
        }
        //Creating new tablature
        else {
            val section = TabSection(sectionNum = 1, sectionTime = 0)
            _totalSectionNum.value = 1
            _currentSection.value = section
            sectionMap.put(section.sectionNum, section)
        }
    }

    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }

    fun addSection(time: Int) {

        storeCurrentSection()

        _totalSectionNum.value = totalSectionsNum.value?.plus(1)
        val newSection = TabSection(totalSectionsNum.value!!, time)
        sectionMap.put(newSection.sectionNum, newSection)

        _currentSection.value = newSection

    }

    fun insertAt(column: Int, string: Int, value: String) {

        val sectionColumns = currentSection.value?.sectionCol

        if (sectionColumns != null) {

            if (value == "") {
                sectionColumns.get(column)[string] = value
            } else {
                var sb = StringBuilder(sectionColumns.get(column)[string])
                if (sb.toString().length == 2) {
                    sb = StringBuilder("")
                }
                sectionColumns.get(column)[string] = sb.append(value).toString()
            }

            _currentSection.value = _currentSection.value
        }
    }

    fun nextSection() {

        if (currentSection.value!!.sectionNum < totalSectionsNum.value!!) {
            storeCurrentSection()

            val nextSection = sectionMap.get(currentSection!!.value!!.sectionNum.plus(1))
            if (nextSection != null) {
                _currentSection.value = nextSection
            }
        }
    }

    public fun previousSection() {

        if (currentSection.value!!.sectionNum > 1) {
            storeCurrentSection()
            val prevSection = sectionMap[currentSection.value!!.sectionNum.minus(1)]

            if (prevSection != null) {
                _currentSection.value = prevSection
            }
        }
    }

    private fun storeCurrentSection() {
        val currentSection = currentSection.value
        if (currentSection != null) {
            sectionMap.put(currentSection.sectionNum, currentSection)
        }
    }

    fun onSetTime(time: Int) {

        val currentSection = this.currentSection.value

        if (currentSection != null) {
            currentSection.sectionTime = time
            this._currentSection.value = currentSection
        }
    }

    fun onSkipTo() {
        _skipToVal.value = currentSection.value?.sectionTime
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

    fun onUpdate(tab: Tablature) {
        uiScope.launch {
            update(tab)
        }
    }

    private suspend fun update(tab: Tablature) {
        withContext(Dispatchers.IO) {
            database.update(tab)
        }
    }
}