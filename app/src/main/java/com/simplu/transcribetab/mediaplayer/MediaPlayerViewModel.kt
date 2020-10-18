package com.simplu.transcribetab

import android.text.format.DateUtils
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job

class MediaPlayerViewModel() : ViewModel() {

    private val _songUri = MutableLiveData<String>()
    val songUri: LiveData<String> = _songUri

    private val _isPlaying = MutableLiveData<Boolean>()
    val isPlaying: LiveData<Boolean> = _isPlaying

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

    init {
        skipToVal = 0
        _currentTime.value = 0
        _duration.value = 0
        _isPlaying.value = false
    }

    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }

    fun onTogglePlayPause() {
        _isPlaying.value = isPlaying.value != true
    }

    fun updateTime(newTime: Long) {
        _currentTime.value = newTime
    }

    fun setDuration(duration: Long) {
        _duration.value = duration
    }

    fun setSkipTo() {
        skipToVal = currentTime?.value ?: 0L
    }

    fun onGoTo() {
        _currentTime.value = skipToVal
        _skipTo.value = skipToVal
    }

    fun setUri(uri: String) {
        _songUri.value = uri
    }

}