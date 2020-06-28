package com.simplu.transcribetab.database

import com.simplu.transcribetab.edittab.customviews.Column

data class Tablature(
    val title: String,
    val artist: String,
    val notes: Array<Column>,
    val songUri: String
)