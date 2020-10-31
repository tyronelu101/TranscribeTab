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

class EditTabViewModel(val database: TablatureDatabaseDao, var tablature: Tablature? = null) :
    ViewModel() {

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

    var sectionMap = HashMap<Int, ArrayList<Array<String>>>()
    var sectionTimeMap = LinkedHashMap<Int, Pair<Int, Int>>()

    private var viewModelJob = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    init {
        initializeTablature()
    }

    private fun initializeTablature() {
        //Editting existing tablature
        if (tablature != null) {
            sectionMap = tablature!!.sections
            sectionTimeMap = tablature!!.sectionToTimeMap

            _currentSectionNum.value = 1
            _totalSectionNum.value = tablature!!.sections.size
            _currentSectionColumns.value = sectionMap[currentSectionNum.value]
            _currentSectionTimeRange.value = sectionTimeMap[currentSectionNum.value]
        }
        //Creating new tablature
        else {
            createNewSection()
            _currentSectionNum.value = 1
            _totalSectionNum.value = 1

            val initTimeRange = Pair(0, -1)
            _currentSectionTimeRange.value = initTimeRange
            sectionTimeMap.put(currentSectionNum.value!!, initTimeRange)

            storeCurrentSection()
        }
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

        //Only add new section if last section has a ending time range set
        if (sectionTimeMap[totalSections]?.second != -1) {
            storeCurrentSection()
            createNewSection()
            createNewSectionRange()

            _currentSectionNum.value = totalSections?.plus(1)
            _totalSectionNum.value = totalSections?.plus(1)
            Log.i(javaClass.simpleName, "CurrentSection ${currentSectionNum.value} Total sections ${totalSectionsNum.value}")
        } else {
            Log.v("EditTabViewModel", "Set an end time for this section.")
        }

    }

    private fun createNewSectionRange() {

        val lastSectionNum = totalSectionsNum.value

        if (lastSectionNum != null && lastSectionNum > 1) {

            val prevRange = sectionTimeMap.get(lastSectionNum - 1)
            if (prevRange != null) {
                Log.v("Range val","Prev range is ${prevRange.second}. Section num is ${lastSectionNum}")

                val newRange = Pair(prevRange.second, -1)
                _currentSectionTimeRange.value = newRange
                sectionTimeMap.put(lastSectionNum+1, newRange)
            }
        }
        else if (lastSectionNum != null  && lastSectionNum == 1) {
            val currentRange = currentSectionTimeRange.value
            if(currentRange != null) {
                val newRange = Pair(currentRange.second, -1)
                _currentSectionTimeRange.value = newRange
                sectionTimeMap.put(lastSectionNum+1, newRange)
            }
        }
    }

    fun insertAt(column: Int, string: Int, value: String) {

        val updatedColumn = _currentSectionColumns.value

        if (updatedColumn != null) {
            var sb = StringBuilder(updatedColumn.get(column)[string])
            if (sb.toString().length == 2) {
                sb = StringBuilder("")
            }
            updatedColumn.get(column)[string] = sb.append(value).toString()
            _currentSectionColumns.value = updatedColumn
        }
    }

    fun nextSection() {

        if (currentSectionNum.value!! < totalSectionsNum.value!!) {
            storeCurrentSection()

            _currentSectionNum.value = currentSectionNum.value?.plus(1)

            val nextSection = sectionMap.get(currentSectionNum.value!!)
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
            val prevSection = sectionMap[currentSectionNum.value!!]

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
            sectionMap.put(currentSectionNumber, currentSectionColumns)
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