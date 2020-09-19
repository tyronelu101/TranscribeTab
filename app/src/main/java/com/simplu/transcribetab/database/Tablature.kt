package com.simplu.transcribetab.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tablature_table")
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
    var columns: HashMap<Int, ArrayList<Array<String>>>,

    @ColumnInfo(name = "song_uri")
    var songUri: String = ""

)

