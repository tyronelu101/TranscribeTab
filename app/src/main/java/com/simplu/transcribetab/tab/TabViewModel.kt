package com.simplu.transcribetab.tab

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.simplu.transcribetab.database.Tablature


class TabViewModel(val tablature: Tablature) : ViewModel() {

//    private val _topSection: MutableLiveData<ArrayList<Array<String>>> = MutableLiveData()
//    val topSection: LiveData<ArrayList<Array<String>>> = _topSection
//
//    private val _bottomSection: MutableLiveData<ArrayList<Array<String>>> = MutableLiveData()
//    val bottomSection: LiveData<ArrayList<Array<String>>> = _bottomSection
//
//    private val _topSectionNum: MutableLiveData<Int> = MutableLiveData();
//    val topSectionNum: LiveData<Int> = _topSectionNum
//
//    private val _bottomSectionNum: MutableLiveData<Int> = MutableLiveData();
//    val bottomSectionNum: LiveData<Int> = _bottomSectionNum
//
//    private var _sectionUpdateTime = MutableLiveData<Int>()
//    var sectionUpdateTime: LiveData<Int> = _sectionUpdateTime
//
//    private val timeToSectionMap = LinkedHashMap<Int, Int>()
//
//    //Represents section that is currently playing
//    private var currentPlayingSectionNum = 1
//
//
//    init {
//        initializeTablature()
//    }
//
//    private fun initializeTablature() {
//        //Initialize the first 2 sections
//        if (tablature != null && tablature.sections != null) {
//            val nextSectionNum = currentPlayingSectionNum + 1
//
//            _topSection.value = tablature.sections.get(currentPlayingSectionNum)
//            _topSectionNum.value = currentPlayingSectionNum
//            _bottomSection.value = tablature.sections.get(nextSectionNum)
//            _bottomSectionNum.value = nextSectionNum
//            _sectionUpdateTime.value = tablature.sectionToTimeMap.get(nextSectionNum)?.second
//
//            createTimeToSectionMap()
//        }
//    }
//
//    private fun createTimeToSectionMap() {
//        if (tablature != null) {
//            for (sectionKey in tablature.sectionToTimeMap.keys) {
//                val timeRange = tablature.sectionToTimeMap[sectionKey]
//                if (timeRange != null) {
//                    timeToSectionMap.put(timeRange.first, sectionKey)
//                }
//            }
//
//        }
//    }
//
//    fun updateSection() {
//
//        if (tablature != null) {
//            ++currentPlayingSectionNum
//            val nextSectionNum = currentPlayingSectionNum + 1
//
//            val nextSection = tablature.sections.get(nextSectionNum)
//
//            if (nextSection != null) {
//                //if even we update bottom section
//                if (nextSectionNum % 2 == 0) {
//                    _bottomSection.value = tablature.sections.get(nextSectionNum)
//                    _bottomSectionNum.value = nextSectionNum
//
//                }
//                //If odd update top section
//                else {
//                    _topSection.value = tablature.sections.get(nextSectionNum)
//                    _topSectionNum.value = nextSectionNum
//                }
//                _sectionUpdateTime.value = tablature.sectionToTimeMap[nextSectionNum]?.second
//            }
//
//        }
//
//    }
//
//    fun updateSectionTo(times: Int) {
//
//        Log.v("TabViewModel", "Time is ${times}")
//
//        for (time in timeToSectionMap.keys) {
//            Log.v("TabViewModel", "Time ${time} -> section ${timeToSectionMap.getValue(time)}")
//        }
//
//        val currentSection = getNearestSectionBelow(times)
//
//        //EVEN: Update bottom and top+1
//        if (currentSection % 2 == 0) {
//
//            if(tablature.sections[currentSection + 1] != null) {
//                _topSection.value = tablature.sections[currentSection + 1]
//                _topSectionNum.value = currentSection + 1
//            }
//
//
//            _bottomSection.value = tablature.sections[currentSection]
//            _bottomSectionNum.value = currentSection
//        }
//        //ODD: Update top and bottom+1
//        else {
//            _topSection.value = tablature.sections[currentSection]
//            _topSectionNum.value = currentSection
//
//            if(tablature.sections[currentSection + 1] != null) {
//                _bottomSection.value = tablature.sections[currentSection + 1]
//                _bottomSectionNum.value = currentSection + 1
//            }
//        }
//    }
//
//    private fun getNearestSectionBelow(timeFromMedia: Int): Int {
//
//        var section: Int? = null
//        var time = timeFromMedia
//        while (section == null) {
//
//            //Time should never reach -1
//            if (time == -1) {
//                return -1
//            }
//
//            section = timeToSectionMap.get(time)
//            --time
//
//        }
//
//        Log.v("TabViewModel", "Nearest section for time ${timeFromMedia} is section ${section}")
//        return section
//    }

}