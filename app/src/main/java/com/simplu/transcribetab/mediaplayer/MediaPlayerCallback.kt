package com.simplu.transcribetab.mediaplayer

interface MediaPlayerCallback {
    fun trigger()
    fun triggerAt(time: Int)
}