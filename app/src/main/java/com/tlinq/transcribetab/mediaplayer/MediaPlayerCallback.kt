package com.tlinq.transcribetab.mediaplayer

interface MediaPlayerCallback {
    fun trigger()
    fun triggerAt(time: Int)
}