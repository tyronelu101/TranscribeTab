package com.simplu.transcribetab.mediaplayer

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class MediaPlayerViewModelFactory(
    private val context: Context, private val uri: Uri, private val sectionUpdater: MediaPlayerCallback?
) : ViewModelProvider.Factory {
    @Suppress("unchecked_cast")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MediaPlayerViewModel::class.java)) {
            return MediaPlayerViewModel(context, uri, sectionUpdater) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}