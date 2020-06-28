package com.simplu.transcribetab.edittab.customviews

import android.graphics.Rect

//Simple data class a single note. Holds a string for fingering and rect for bound
data class Note(var fingeringVal: String = "", var bound: Rect)

open class Column(
    var columnBound: Rect,
    var notes: Array<Note>,
    var isBar: Boolean = false,
    var barNumber: Int = -1,
    var time: Int = -1
) {
    //Sets the value of a string with a fingering val
    fun setNoteFingering(string: Int, fingering: String) {
        notes[string].fingeringVal = fingering
    }

    fun inColumnBound(x: Float, y: Float): Boolean =
        (x > columnBound.left && x < columnBound.right && y > columnBound.top && y < columnBound.bottom)

    fun getColumnLeftBound(): Int {
        return columnBound.left
    }

    fun getColumnRightBound(): Int {
        return columnBound.right
    }

    fun getNoteLeftBound(): Int {
        return notes[0].bound.left
    }

    fun getNoteRightBound(): Int {
        return notes[0].bound.right
    }

    fun clearColumn() {
        isBar = false
        barNumber = -1
        time = -1
        for (string in notes) {
            string.fingeringVal = ""
        }
    }

    fun getNoteValues(): Array<String> = Array<String>(6) { i ->
        notes[i].fingeringVal

    }

    fun setNoteValues(newNotes: Array<String>) {
        for (string in 0..5) {
            this.notes[string].fingeringVal = newNotes[string]
        }
    }
}
