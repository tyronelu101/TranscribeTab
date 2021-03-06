package com.simplu.transcribetab.tab

import android.text.format.DateUtils
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.simplu.transcribetab.database.Tablature
import com.simplu.transcribetab.database.TablatureDatabase
import com.simplu.transcribetab.database.TablatureRepository
import com.simplu.transcribetab.views.TabSection
import kotlin.collections.set


class TabViewModel(tablature: Tablature, database: TablatureDatabase) : ViewModel() {

    val repository: TablatureRepository = TablatureRepository(database)

    var tab: LiveData<Tablature> = repository.get(tablature)

    private val _topSection: MutableLiveData<TabSection> = MutableLiveData()
    val topSection: LiveData<TabSection> = _topSection

    val topSectionTime = Transformations.map(topSection) {
        DateUtils.formatElapsedTime(it.sectionTime.toLong()).removeRange(0, 1)
    }

    private val _bottomSection: MutableLiveData<TabSection> = MutableLiveData()
    val bottomSection: LiveData<TabSection> = _bottomSection

    val bottomSectionTime = Transformations.map(bottomSection) {
        DateUtils.formatElapsedTime(it.sectionTime.toLong()).removeRange(0, 1)
    }

    private var _sectionUpdateTime = MutableLiveData<Int>()
    var sectionUpdateTime: LiveData<Int> = _sectionUpdateTime

    //Represents section that is currently playing
    private var currentPlayingSectionNum = 1

    private val timeToSectionMap = LinkedHashMap<Int, Int>()

    fun initializeTablature() {
        val tablature = tab.value
        if (tablature != null) {
            _topSection.value = tablature.sections.get(currentPlayingSectionNum)
            _bottomSection.value = tablature.sections.get(currentPlayingSectionNum + 1)
        }

        initTimeToSectionMap()
        setSectionUpdateTime()

    }

    private fun initTimeToSectionMap() {
        val tablature = tab.value

        if (tablature != null) {
            val sectionsMap = tablature.sections
            for (section in sectionsMap.values) {
                timeToSectionMap[section.sectionTime] = section.sectionNum
            }
        }

    }

    private fun setSectionUpdateTime() {
        val tablature = tab.value

        if (tablature != null) {
            val sectionsTimeToWatch = tablature.sections.get(currentPlayingSectionNum + 1)
            if (sectionsTimeToWatch != null) {
                _sectionUpdateTime.value = sectionsTimeToWatch.sectionTime
                Log.v("TabViewModel", "Section update time is ${sectionUpdateTime.value}")
            }
        }

    }

    //used when audio plays normally
    fun updateSection() {

        val tablature = tab.value
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
                setSectionUpdateTime()
            }

        }

    }

    //use when user touches seekbar
    fun updateSectionTo(times: Int) {

        val tablature = tab.value


        if (tablature != null) {
            val currentSection = getNearestSectionBelow(times)
            currentPlayingSectionNum = getNearestSectionBelow(times)
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

            setSectionUpdateTime()
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