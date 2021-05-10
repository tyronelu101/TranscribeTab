package com.simplu.transcribetab.database

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize
import kotlinx.android.parcel.RawValue

@Entity(tableName = "tablature_table")
@Parcelize
data class Tablature(

    @PrimaryKey(autoGenerate = true)
    var tabId: Long = 0L,

    @ColumnInfo(name = "title")
    var title: String = "",

    @ColumnInfo(name = "artist")
    var artist: String = "",

    @ColumnInfo(name = "arranger")
    var arranger: String = "",

    @ColumnInfo(name = "tuning")
    var tuning: String = "",

    @ColumnInfo(name = "section_columns")
    var sections: LinkedHashMap<Int, @RawValue TabSection>,

    @ColumnInfo(name = "song_uri")
    var songUri: String = ""

) : Parcelable

