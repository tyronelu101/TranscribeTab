package com.simplu.transcribetab

import java.io.Serializable


data class TabSection(
    val numberOfColumns: Int,
    val sectionNum: Int,
    var sectionTime: Int,
    val sectionCol: ArrayList<Array<String>> = ArrayList()) : Serializable {
    init {
        for (i in 0..numberOfColumns) {
            sectionCol.add(Array(6) { "" })
        }
    }

    fun clearColumn(column: Int) {
        sectionCol[column] = Array(6) { "" }
    }
}
