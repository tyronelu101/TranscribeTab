package com.simplu.transcribetab.tab

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.simplu.transcribetab.TabSection
import com.simplu.transcribetab.database.Tablature


class TabViewModel(val tablature: Tablature) : ViewModel() {

    private val _topSection: MutableLiveData<TabSection> = MutableLiveData()
    val topSection: LiveData<TabSection> = _topSection

    private val _bottomSection: MutableLiveData<TabSection> = MutableLiveData()
    val bottomSection: LiveData<TabSection> = _bottomSection

    private var _sectionUpdateTime = MutableLiveData<Int>()
    var sectionUpdateTime: LiveData<Int> = _sectionUpdateTime

    //Represents section that is currently playing
    private var currentPlayingSectionNum = 1

    private val timeToSectionMap = LinkedHashMap<Int, Int>()

    init {
        initializeTablature()
    }

    private fun initializeTablature() {
        //Initialize the first 2 sections
        if (tablature != null && tablature.sections != null) {
            val nextSectionNum = currentPlayingSectionNum + 1

            _topSection.value = tablature.sections.get(currentPlayingSectionNum)
            _bottomSection.value = tablature.sections.get(nextSectionNum)

            setSectionUpdateTime()
            initTimeToSectionMap()
        }
    }

    private fun initTimeToSectionMap() {
        val sectionsMap = tablature.sections
        for(section in sectionsMap.values) {
            timeToSectionMap[section.sectionTime] = section.sectionNum
        }
    }

    private fun setSectionUpdateTime() {
        val nextTabSection = tablature.sections.get(currentPlayingSectionNum + 2)
        if (nextTabSection != null) {
            _sectionUpdateTime.value = nextTabSection.sectionTime
        }
    }

    //used when audio plays normally
    fun updateSection() {

        if (tablature != null) {
            ++currentPlayingSectionNum
            val nextSectionNum = currentPlayingSectionNum + 1

            val nextSection = tablature.sections.get(nextSectionNum)

            if (nextSection != null) {
                //if even we update bottom section
                if (nextSectionNum % 2 == 0) {
                    _bottomSection.value = tablature.sections.get(nextSectionNum)

                }
                //If odd update top section
                else {
                    _topSection.value = tablature.sections.get(nextSectionNum)
                }
                updateSection()
            }

        }

    }

    //use when user touches seekbar
    fun updateSectionTo(times: Int) {

        val currentSection = getNearestSectionBelow(times)

        //EVEN: Update bottom and top+1
        if (currentSection % 2 == 0) {

            if (tablature.sections[currentSection + 1] != null) {
                _topSection.value = tablature.sections[currentSection + 1]
            }


            _bottomSection.value = tablature.sections[currentSection]
        }
        //ODD: Update top and bottom+1
        else {
            _topSection.value = tablature.sections[currentSection]

            if (tablature.sections[currentSection + 1] != null) {
                _bottomSection.value = tablature.sections[currentSection + 1]
            }
        }
    }

    private fun getNearestSectionBelow(timeFromMedia: Int): Int {

        var section: Int? = null
        var time = timeFromMedia
        while (section == null) {

            //Time should never reach -1
            if (time == -1) {
                return -1
            }

            section = timeToSectionMap.get(time)
            --time

        }

        return section
    }

}