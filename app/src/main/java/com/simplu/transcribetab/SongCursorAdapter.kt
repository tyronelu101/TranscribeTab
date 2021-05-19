package com.simplu.transcribetab

import android.content.Context
import android.database.Cursor
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cursoradapter.widget.CursorAdapter
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*


class SongCursorAdapter(context: Context?, c: Cursor?, flags: Int) :
    CursorAdapter(context, c, flags) {

    override fun newView(context: Context, cursor: Cursor, parent: ViewGroup): View {
        return LayoutInflater.from(context).inflate(R.layout.item_song, parent, false)
    }

    override fun bindView(view: View, context: Context, cursor: Cursor) {
        val songTitleTextView: TextView = view.findViewById(R.id.item_song_title)
        val songDurationTextView: TextView = view.findViewById(R.id.item_song_duration)

        val songTitle = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE))
        val songDuration = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION))

        val formatter: DateFormat = SimpleDateFormat("m:ss", Locale.US)
        formatter.setTimeZone(TimeZone.getTimeZone("UTC"))
        val songDurationFormatted: String = formatter.format(Date(songDuration.toLong()))

        songTitleTextView.text = songTitle
        songDurationTextView.text = songDurationFormatted

    }

}