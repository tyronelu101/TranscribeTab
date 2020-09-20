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

    private val _currentSectionObs = MutableLiveData<ArrayList<Array<String>>>()
    val currentSectionObs: LiveData<ArrayList<Array<String>>> = _currentSectionObs

    private val _currentSectionNumObs = MutableLiveData<Int>()
    val currentSectionNumObs: LiveData<Int> = _currentSectionNumObs

    private val _totalSectionsObs = MutableLiveData<Int>()
    val totalSectionsObs: LiveData<Int> = _totalSectionsObs

    private val _currentSectionTimeRange = MutableLiveData<Pair<Int, Int?>>()
    val currentSectionTimeRange: LiveData<Pair<Int, Int?>> = _currentSectionTimeRange

    val sectionTimeRangeString: LiveData<String> = Transformations.map(currentSectionTimeRange) {

        val beginningTime = it.first
        val endTime = it.second

        val timeStringSb =
            StringBuilder(
                "[" + DateUtils.formatElapsedTime(beginningTime.toLong()).removeRange(0, 1) + "-"
            )

        if (endTime != null) {
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
    val sectionTimeMap = HashMap<Int, Pair<Int, Int?>>()

    private var currentSection = ArrayList<Array<String>>()
    private var currentSectionNum = 1
    private var totalSections = 1

    private var viewModelJob = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    init {
        createNewSection()
        _currentSectionNumObs.value = currentSectionNum
        _totalSectionsObs.value = totalSections

        val initTimeRange = Pair(0, null)
        sectionTimeMap.put(currentSectionNum, initTimeRange)
        _currentSectionTimeRange.value = initTimeRange

    }

    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }

    public fun addSection() {
        //Only add new section if current section has a ending time range set
        if (sectionTimeMap[totalSections]?.second != null) {
            storeCurrentSection()
            createNewSection()

            currentSectionNum = ++totalSections
            createNewSectionRange()

            _currentSectionNumObs.value = totalSections
            _totalSectionsObs.value = totalSections
        }
        else {
            Log.v("EditTabViewModel", "Set an end time for this section.")
        }

    }

    private fun createNewSectionRange() {

        if (currentSectionNum > 1) {

            val prevRange = sectionTimeMap.get(currentSectionNum - 1)
            if (prevRange?.second != null) {
                val newRange = Pair(prevRange.second!!, null)
                _currentSectionTimeRange.value = newRange
                sectionTimeMap.put(currentSectionNum, newRange)
            }
        }
    }

    private fun createNewSection() {

        val newArrayList = ArrayList<Array<String>>()
        for (i in 0..EditTabView.NUMBER_OF_COLUMNS) {
            val newColumn = Array<String>(6) { "" }
            newArrayList.add(newColumn)
        }
        currentSection = newArrayList
        _currentSectionObs.value = newArrayList

        Log.v("ViewModel", "Size of map is ${sectionValuesMap.size}")
    }

    public fun insertAt(column: Int, string: Int, value: String) {
        currentSection.get(column)[string] = value
        _currentSectionObs.value = currentSection
    }

    public fun nextSection() {
        if (currentSectionNum < totalSections) {
            storeCurrentSection()
            _currentSectionNumObs.value = ++currentSectionNum
            Log.v("Test", "next section: ${currentSectionNum}")

            val nextSection = sectionValuesMap.get(currentSectionNum)
            if (nextSection != null) {
                currentSection = nextSection
                _currentSectionObs.value = nextSection
            }
            updateSectionRange()
        }
    }

    public fun previousSection() {
        if (currentSectionNum > 1) {
            storeCurrentSection()
            _currentSectionNumObs.value = --currentSectionNum
            Log.v("Test", "prev section: ${currentSectionNum}")
            val prevSection = sectionValuesMap[currentSectionNum]
            if (prevSection != null) {
                currentSection = prevSection
                _currentSectionObs.value = prevSection
            }
            updateSectionRange()

        }
    }

    private fun updateSectionRange() {
        val currentTimeRange = sectionTimeMap.get(currentSectionNum)
        if (currentTimeRange != null) {
            _currentSectionTimeRange.value = currentTimeRange
        }
    }

    private fun storeCurrentSection() {
        Log.v("Test", "Storing ${currentSectionNum} with ${currentSection}")
        sectionValuesMap.put(currentSectionNum, currentSection)
    }

    fun onSetTime(time: Int) {
        Log.v("Test", "Time is ${time}")
        val currentRange = sectionTimeMap.get(currentSectionNum)
        if (currentRange != null) {
            Log.v("Test", "ViewModel onSetTime")

            val start = currentRange.first
            val end = time
            val newRange = Pair(start, end)
            sectionTimeMap.put(currentSectionNum, newRange)
            _currentSectionTimeRange.value = newRange
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