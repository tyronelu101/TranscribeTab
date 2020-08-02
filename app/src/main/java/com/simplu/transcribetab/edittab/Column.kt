package com.simplu.transcribetab.edittab

import android.graphics.Rect

open class Column(
    var notes: Array<String> = Array(6) { "" }, var isBar: Boolean = false
)

class DrawableColumn(
    var columnBound: Rect,
    var noteBound: Array<Rect> = Array(6) { Rect(0, 0, 0, 0) },
    var barNumber: Int = -1,
    var time: Int = -1,
    notes: Array<String> = Array(6) { "" },
    isBar: Boolean = false
) : Column(notes, isBar) {
    //Sets the value of a string with a fingering val
    fun setNoteFingering(string: Int, fingering: String) {
        notes[string] = fingering
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
        return noteBound[0].left
    }

    fun getNoteRightBound(): Int {
        return noteBound[0].right
    }

    fun clearColumn() {
        isBar = false
        barNumber = -1
        time = -1
        for (string in 0..5) {
            notes[string] = ""
        }
    }

    fun setNoteValues(newNotes: Array<String>) {
        for (string in 0..5) {
            notes[string] = newNotes[string]
        }
    }
}
