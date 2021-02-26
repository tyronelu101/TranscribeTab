package com.simplu.transcribetab

import java.io.Serializable


data class TabSection(
    val sectionNum: Int,
    var sectionTime: Int,
    val sectionCol: ArrayList<Array<String>> = ArrayList()) : Serializable {
    init {
        //todo get max number of columns to create from customview attribtute.
        for (i in 0..14) {
            sectionCol.add(Array(6) { "" })
        }
    }

    fun clearColumn(column: Int) {
        sectionCol[column] = Array(6) { "" }
    }
}
