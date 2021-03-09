package com.simplu.transcribetab.edittab

import android.text.format.DateUtils
import androidx.lifecycle.*
import com.simplu.transcribetab.database.Tablature
import com.simplu.transcribetab.database.TablatureRepository
import com.simplu.transcribetab.views.TabSection
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class EditTabViewModel(
    private val repository: TablatureRepository,
    var tablature: Tablature? = null,
    val numberOfColumns: Int
) :
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

    var sectionMap = LinkedHashMap<Int, TabSection>()

    private var saveFlag = true

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
            val section = TabSection(
                numberOfColumns,
                sectionNum = 1,
                sectionTime = 0
            )
            _totalSectionNum.value = 1
            _currentSection.value = section
            sectionMap.put(section.sectionNum, section)
        }
    }

    fun addSection(time: Int) {

        if (validAddTime(time)) {
            storeCurrentSection()

            _totalSectionNum.value = totalSectionsNum.value?.plus(1)
            val newSection = TabSection(
                numberOfColumns,
                totalSectionsNum.value!!,
                time
            )
            sectionMap.put(newSection.sectionNum, newSection)

            _currentSection.value = newSection
            saveFlag = false
        }
    }

    fun insertAt(column: Int, string: Int, value: String) {

        val sectionColumns = currentSection.value?.sectionCol

        if (sectionColumns != null) {

            if (value == "X") {
                sectionColumns.get(column)[string] = value
            } else {
                var sb = StringBuilder(sectionColumns.get(column)[string])
                if (sb.toString() == "X") {
                    sb.setLength(0)
                }
                if (sb.toString().length == 2) {
                    sb.setLength(0)
                }
                sectionColumns.get(column)[string] = sb.append(value).toString()
            }

            _currentSection.value = _currentSection.value
            saveFlag = false
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

    fun previousSection() {

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

        if (currentSection != null && currentSection.sectionNum > 1 && validSetTime(time)) {
            currentSection.sectionTime = time
            this._currentSection.value = currentSection
            saveFlag = false
        }
    }

    private fun validAddTime(time: Int): Boolean {
        val lastSection = sectionMap.get(totalSectionsNum.value)
        lastSection?.sectionTime.let {
            if (it != null && it < time) {
                return true
            }
        }
        return false
    }

    private fun validSetTime(time: Int): Boolean {
        val currentSection = this.currentSection.value
        var isTimeBetweenSection = false
        var isTimeAfterSection = false
        if (currentSection != null && currentSection.sectionNum > 1) {

            //previous section should always be not null because of currentSection.sectionNum > 1
            val previousSection = sectionMap[currentSection.sectionNum - 1]
            val nextSection = sectionMap[currentSection.sectionNum + 1]
            isTimeBetweenSection =
                if (previousSection != null && nextSection != null) {
                    (previousSection.sectionTime < time && time < nextSection.sectionTime)
                } else
                    false

            isTimeAfterSection =
                if (previousSection != null && nextSection == null) {
                    (previousSection.sectionNum < time)
                } else
                    false

        }
        return isTimeBetweenSection || isTimeAfterSection
    }

    fun tabIsUpdated() = this.saveFlag

    fun clearColumn(column: Int) {
        currentSection.value?.clearColumn(column)
        _currentSection.value = currentSection.value
        saveFlag = false
    }

    fun onSkipTo() {
        _skipToVal.value = currentSection.value?.sectionTime
    }

    fun loadTab(): LiveData<Tablature>? {
        if (tablature != null) {
            return repository.get(tablature!!)
        }
        return null
    }

    fun needToSave() {
        saveFlag = true
    }


    fun onSave(tab: Tablature) {
        storeCurrentSection()
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                repository.insert(tab)
                saveFlag = true
            }
        }
    }

    fun onUpdate(tab: Tablature) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                repository.update(tab)
                saveFlag = true
            }
        }
    }
}