package com.simplu.transcribetab.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.simplu.transcribetab.edittab.Column

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

    @ColumnInfo(name = "tab_columns")
    var columns: ArrayList<Column>,

    @ColumnInfo(name = "song_uri")
    var songUri: String = ""

)

