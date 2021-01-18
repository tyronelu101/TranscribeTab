package com.simplu.transcribetab

import com.simplu.transcribetab.edittab.EditTabView
import java.io.Serializable


data class TabSection(
    val sectionNum: Int,
    var sectionTime: Int,
    val sectionCol: ArrayList<Array<String>> = ArrayList()
) : Serializable {
    init {
        for (i in 0..EditTabView.NUMBER_OF_COLUMNS) {
            sectionCol.add(Array(6) { "" })
        }
    }

    fun clearColumn(column: Int) {
        sectionCol[column] = Array(6) { "" }
    }
}
