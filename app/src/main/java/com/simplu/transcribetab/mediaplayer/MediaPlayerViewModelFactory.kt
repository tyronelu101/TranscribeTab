package com.simplu.transcribetab.mediaplayer

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.simplu.transcribetab.tab.SectionUpdater

class MediaPlayerViewModelFactory(
    private val context: Context, private val uri: Uri, private val sectionUpdater: SectionUpdater?
) : ViewModelProvider.Factory {
    @Suppress("unchecked_cast")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MediaPlayerViewModel::class.java)) {
            return MediaPlayerViewModel(context, uri, sectionUpdater) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}