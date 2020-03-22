package com.simplu.transcribetab.customviews

import android.graphics.Rect

class ColumnNotes(val colId: Int = getId(), val columnBound: Rect? = null, val notes: Array<Note>) {

    companion object {
        var currentId = 0
        fun getId(): Int {
            ++currentId
            return currentId
        }
    }

    //Sets the value of a string with a fingering
    fun setNoteFingering(string: Int, fingering: String) {
        notes[string].fingeringVal = fingering
    }

    fun getNoteRightBound(): Int {
        return notes[0].bound.right
    }

    fun getNoteLeftBound(): Int {
        return notes[0].bound.left
    }
}