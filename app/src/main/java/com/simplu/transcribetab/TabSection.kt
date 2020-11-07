package com.simplu.transcribetab

import com.simplu.transcribetab.edittab.EditTabView


data class TabSection(
    val sectionNum: Int,
    var sectionTime: Int,
    val sectionCol: ArrayList<Array<String>> = ArrayList()
) {
    init {
        for (i in 0..EditTabView.NUMBER_OF_COLUMNS) {
            sectionCol.add(Array(6) { "" })
        }
    }
}
