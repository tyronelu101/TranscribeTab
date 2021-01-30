package com.simplu.transcribetab.edittab

import android.text.format.DateUtils
import androidx.lifecycle.*
import com.simplu.transcribetab.TabSection
import com.simplu.transcribetab.database.Tablature
import com.simplu.transcribetab.database.TablatureRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class EditTabViewModel(
    private val repository: TablatureRepository,
    var tablature: Tablature? = null
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

    fun clearColumn(column: Int) {
        currentSection.value?.clearColumn(column)
        _currentSection.value = currentSection.value
    }

    fun onSkipTo() {
        _skipToVal.value = currentSection.value?.sectionTime
    }

    fun loadTab(): LiveData<Tablature>? {
        if(tablature!= null) {
            return repository.get(tablature!!)
        }
        return null
    }

    fun onSave(tab: Tablature) {
        storeCurrentSection()
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                repository.insert(tab)
            }
        }
    }

    fun onUpdate(tab: Tablature) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                repository.update(tab)
            }
        }
    }
}