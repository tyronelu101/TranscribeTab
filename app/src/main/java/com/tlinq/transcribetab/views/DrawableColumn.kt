package com.tlinq.transcribetab.views

import android.graphics.Rect

class DrawableColumn(
    var notes: Array<String> = Array(6) { "" },
    var columnBound: Rect,
    var noteBound: Array<Rect> = Array(6) { Rect(0, 0, 0, 0) }
) {

    fun inColumnBound(x: Float, y: Float): Boolean {
        return (x > columnBound.left && x < columnBound.right && y > columnBound.top && y < columnBound.bottom)
    }

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
