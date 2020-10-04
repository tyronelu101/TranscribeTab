package com.simplu.transcribetab.edittab

import android.text.format.DateUtils
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.simplu.transcribetab.database.Tablature
import com.simplu.transcribetab.database.TablatureDatabaseDao
import kotlinx.coroutines.*


class EditTabViewModel(val database: TablatureDatabaseDao) : ViewModel() {

    private val _currentSectionColumns = MutableLiveData<ArrayList<Array<String>>>()
    val currentSectionColumns: LiveData<ArrayList<Array<String>>> = _currentSectionColumns

    private val _currentSectionNum = MutableLiveData<Int>()
    val currentSectionNum: LiveData<Int> = _currentSectionNum

    private val _totalSectionNum = MutableLiveData<Int>()
    val totalSectionsNum: LiveData<Int> = _totalSectionNum

    private val _currentSectionTimeRange = MutableLiveData<Pair<Int, Int>>()
    val currentSectionTimeRange: LiveData<Pair<Int, Int>> = _currentSectionTimeRange
    val sectionTimeRangeString: LiveData<String> = Transformations.map(currentSectionTimeRange) {

        val beginningTime = it.first
        val endTime = it.second

        val timeStringSb =
            StringBuilder(
                "[" + DateUtils.formatElapsedTime(beginningTime.toLong()).removeRange(0, 1) + "-"
            )

        if (endTime != -1) {
            timeStringSb.append(
                DateUtils.formatElapsedTime(endTime.toLong()).removeRange(0, 1) + "]"
            )
        } else {
            timeStringSb.append("_:__]")
        }

        timeStringSb.toString()
    }

    private val _skipToVal = MutableLiveData<Int>()
    val skipToVal: LiveData<Int> = _skipToVal

    val sectionValuesMap = HashMap<Int, ArrayList<Array<String>>>()
    val sectionTimeMap = HashMap<Int, Pair<Int, Int>>()
    val timeToSectionMap = HashMap<Int, Int>()

    private var viewModelJob = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    init {
        createNewSection()
        _currentSectionNum.value = 1
        _totalSectionNum.value = 1

        val initTimeRange = Pair(0, -1)
        _currentSectionTimeRange.value = initTimeRange
        sectionTimeMap.put(currentSectionNum.value!!, initTimeRange)

        storeCurrentSection()

    }

    private fun createNewSection() {

        val newSectionColumns = ArrayList<Array<String>>()

        for (i in 0..EditTabView.NUMBER_OF_COLUMNS) {
            val column = Array<String>(6) { "" }
            newSectionColumns.add(column)
        }
        _currentSectionColumns.value = newSectionColumns

    }

    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }

    fun addSection() {

        val totalSections = totalSectionsNum.value

        //Only add new section if current section has a ending time range set
        if (sectionTimeMap[totalSections]?.second != null) {
            storeCurrentSection()
            createNewSection()

            _currentSectionNum.value = currentSectionNum.value?.plus(1)
            createNewSectionRange()

            _currentSectionNum.value = totalSections?.plus(1)
            _totalSectionNum.value = totalSections?.plus(1)
        } else {
            Log.v("EditTabViewModel", "Set an end time for this section.")
        }

    }

    private fun createNewSectionRange() {

        val currentSectionNumVal = currentSectionNum.value

        if (currentSectionNumVal != null && currentSectionNumVal > 1) {

            val prevRange = sectionTimeMap.get(currentSectionNumVal - 1)
            if (prevRange != null) {
                val newRange = Pair(prevRange.second, -1)
                _currentSectionTimeRange.value = newRange
                sectionTimeMap.put(currentSectionNumVal, newRange)
            }
        }
    }

    fun insertAt(column: Int, string: Int, value: String) {

        val updatedColumn = _currentSectionColumns.value

        if (updatedColumn != null) {
            updatedColumn.get(column)[string] = value
            _currentSectionColumns.value = updatedColumn
        }
    }

    public fun nextSection() {

        if (currentSectionNum.value!! < totalSectionsNum.value!!) {
            storeCurrentSection()

            _currentSectionNum.value = currentSectionNum.value?.plus(1)

            val nextSection = sectionValuesMap.get(currentSectionNum.value!!)
            if (nextSection != null) {
                _currentSectionColumns.value = nextSection
            }
            updateSectionRange()
        }
    }

    public fun previousSection() {

        if (currentSectionNum.value!! > 1) {
            storeCurrentSection()
            _currentSectionNum.value = currentSectionNum.value?.minus(1)
            val prevSection = sectionValuesMap[currentSectionNum.value!!]

            if (prevSection != null) {
                _currentSectionColumns.value = prevSection
            }
            updateSectionRange()

        }
    }

    private fun updateSectionRange() {
        val currentTimeRange = sectionTimeMap.get(currentSectionNum.value)
        if (currentTimeRange != null) {
            _currentSectionTimeRange.value = currentTimeRange
        }
    }

    private fun storeCurrentSection() {
        val currentSectionNumber = currentSectionNum.value
        val currentSectionColumns = currentSectionColumns.value

        if (currentSectionNumber != null && currentSectionColumns != null) {
            sectionValuesMap.put(currentSectionNumber, currentSectionColumns)
        }
    }

    fun onSetTime(time: Int) {

        val currentRange = sectionTimeMap.get(currentSectionNum.value!!)
        Log.v("EditTabViewModel", "onSetTime() ${currentRange}")

        if (currentRange != null) {
            Log.v("EditTabViewModel", "onSetTimes()")
            val start = currentRange.first
            val end = time
            val newRange = Pair(start, end)
            sectionTimeMap.put(currentSectionNum.value!!, newRange)
            _currentSectionTimeRange.value = newRange
        }
    }

    fun onSkipTo() {
        val skipToTime = sectionTimeMap.get(currentSectionNum.value!!)
        if (skipToTime != null) {
            _skipToVal.value = skipToTime.first
        }
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