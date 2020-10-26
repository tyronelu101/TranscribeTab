package com.simplu.transcribetab.database

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize

@Entity(tableName = "tablature_table")
@Parcelize
data class Tablature(

    @PrimaryKey(autoGenerate = true)
    var tabId: Long = 0L,

    @ColumnInfo(name = "title")
    val title: String = "",

    @ColumnInfo(name = "artist")
    var artist: String = "",

    @ColumnInfo(name = "arranger")
    var arranger: String = "",

    @ColumnInfo(name = "tuning")
    var tuning: String = "",

    @ColumnInfo(name = "section_columns")
    var sections: HashMap<Int, ArrayList<Array<String>>>,

    @ColumnInfo(name = "section_time_range")
    var sectionToTimeMap: LinkedHashMap<Int, Pair<Int, Int>>,

    @ColumnInfo(name = "song_uri")
    var songUri: String = ""

) : Parcelable

