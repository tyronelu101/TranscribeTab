package com.simplu.transcribetab.mediaplayer

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import android.text.format.DateUtils
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.simplu.transcribetab.tab.SectionUpdater
import kotlinx.coroutines.*

class MediaPlayerViewModel(
    context: Context,
    uri: Uri,
    private val sectionUpdater: SectionUpdater?
) : ViewModel() {

    private val mediaPlayer: MediaPlayer = MediaPlayer().apply {
        setDataSource(context, uri)
        prepare()
    }

    private val _songUri = MutableLiveData<String>()
    val songUri: LiveData<String> = _songUri

    private val _isPlaying = MutableLiveData<Boolean>()
    val isPlaying: LiveData<Boolean> = _isPlaying

    private val _currentTime = MutableLiveData<Int>()
    val currentTime: LiveData<Int> = _currentTime
    val currentTimeString: LiveData<String> = Transformations.map(currentTime) { time ->

        //Remove range so format of string is 0:00 instead of 00:00
        DateUtils.formatElapsedTime(time.toLong()).removeRange(0, 1)
    }

    private val _duration = MutableLiveData<Int>()
    val duration: LiveData<Int> = _duration
    val durationString: LiveData<String> = Transformations.map(duration) { time ->

        //Remove range so format of string is 0:00 instead of 00:00
        DateUtils.formatElapsedTime(time.toLong()).removeRange(0, 1)
    }

    private val mediaPlayerViewModelJob = Job()
    private val mediaPlayerViewModelScope =
        CoroutineScope(mediaPlayerViewModelJob + Dispatchers.Main)

    private var skipToVal: Int = 0
    private var triggerTime: Int = 0

    init {
        _currentTime.value = 0
        _duration.value = mediaPlayer.durationSecs()
    }

    fun play() {
        _isPlaying.value = true
        startMedia()
    }

    fun pause() {
        if(mediaPlayer.isPlaying) {
            _isPlaying.value = false
            mediaPlayer.pause()
        }
    }

    fun onPlayPause() {
        if (mediaPlayer.isPlaying) {
            _isPlaying.value = false
            mediaPlayer.pause()
        } else {
            _isPlaying.value = true
            startMedia()
        }
    }

    fun skipTo(newTime: Int) {
        mediaPlayer.seekTo(newTime * 1000)
        sectionUpdater?.updateSectionTo(newTime)
        _currentTime.value = newTime
    }

    fun setSkipTo() {
        skipToVal = mediaPlayer.currentPositionSecs() ?: 0
    }

    fun onGoTo() {
        _currentTime.value = skipToVal
        mediaPlayer.seekTo(skipToVal * 1000)
    }

    fun isPlaying() = mediaPlayer.isPlaying

    private fun MediaPlayer.durationSecs() = mediaPlayer.duration/1000

    private fun MediaPlayer.currentPositionSecs() = mediaPlayer.currentPosition/1000

    private fun startMedia() {
        mediaPlayer.start()
        mediaPlayerViewModelScope.launch {
            while (mediaPlayer.isPlaying) {
                _currentTime.value = (mediaPlayer.currentPositionSecs())
                if(mediaPlayer.currentPositionSecs() == triggerTime) {
                    sectionUpdater?.updateSection()
                }
                delay(500)
            }
        }
    }

    fun setTriggerTime(time: Int) {
        this.triggerTime = time
    }

}