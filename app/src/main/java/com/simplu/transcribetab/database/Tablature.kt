package com.simplu.transcribetab.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.simplu.transcribetab.Column

@Entity(tableName = "tablature_table")
data class Tablature(

    @PrimaryKey(autoGenerate = true)
    var tabId: Long = 0L,

    @ColumnInfo(name = "title")
    val title: String,

    @ColumnInfo(name = "artist")
    val artist: String,

    @ColumnInfo(name = "arranger")
    val arranger: String,

    @ColumnInfo(name = "tuning")
    val tuning: String,

    @ColumnInfo(name = "columns")
    val columns: ArrayList<Column>,

    @ColumnInfo(name = "songUri")
    val songUri: String
)