package com.simplu.transcribetab.tab

import android.text.format.DateUtils
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.simplu.transcribetab.database.Tablature
import com.simplu.transcribetab.database.TablatureDatabaseDao
import kotlinx.coroutines.*


class TabViewModel(val database: TablatureDatabaseDao, val tabId: Long) : ViewModel() {

    private var _tablature: MutableLiveData<Tablature> = MutableLiveData()
    val tablature: LiveData<Tablature> = _tablature

    private val _isPlaying = MutableLiveData<Boolean>()
    val isPlaying: LiveData<Boolean> = _isPlaying

    val _isPaused = MutableLiveData<Boolean>()
    val isPaused: LiveData<Boolean> = _isPaused

    private val _currentTime = MutableLiveData<Long>()
    val currentTime: LiveData<Long> = _currentTime
    val currentTimeString: LiveData<String> = Transformations.map(currentTime) { time ->

        //Remove range so format of string is 0:00 instead of 00:00
        DateUtils.formatElapsedTime(time).removeRange(0, 1)
    }

    private val _duration = MutableLiveData<Long>()
    val duration: LiveData<Long> = _duration
    val durationString: LiveData<String> = Transformations.map(duration) { time ->
        //Remove range so format of string is 0:00 instead of 00:00
        DateUtils.formatElapsedTime(time).removeRange(0, 1)
    }

    private var skipToVal: Long

    private val _skipTo = MutableLiveData<Long>()
    val skipTo: LiveData<Long> = _skipTo

    private var viewModelJob = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }


    init {
        _currentTime.value = 0
        _duration.value = 0
        skipToVal = 0
        initializeTablature()
    }

    private fun initializeTablature() {
        uiScope.launch {
            _tablature.value = getTablature()
        }
    }

    private suspend fun getTablature(): Tablature {
        return withContext(Dispatchers.IO) {
            database.get(tabId)
        }
    }

    public fun onPlay() {
        _isPlaying.value = true
        _isPaused.value = false
    }

    public fun onPause() {
        _isPlaying.value = false
        _isPaused.value = true
    }

    public fun updateTime(newTime: Long) {
        _currentTime.value = newTime
    }

    public fun setDuration(duration: Long) {
        _duration.value = duration
    }

    public fun setSkipTo() {
        skipToVal = currentTime?.value ?: 0L
    }

    public fun onGoTo() {
        _currentTime.value = skipToVal
        _skipTo.value = skipToVal
    }


}