package com.simplu.transcribetab.views

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.MotionEvent

class EditTabView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : TabView(context, attrs, defStyleAttr) {

    private val columnBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        alpha = 50
    }

    private var currentSelectedColumn: DrawableColumn

    init {
        currentSelectedColumn = columnNotesList[0]
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        drawSelectedColumn(canvas)
    }

    private fun drawSelectedColumn(canvas: Canvas) {
        if (currentSelectedColumn?.columnBound != null) {
            canvas.drawRect(currentSelectedColumn?.columnBound, columnBorderPaint)
        }
    }

    fun insertNote(string: Int, fingeringVal: String) {

        var currentVal = currentSelectedColumn.notes[string]
        var newVal = ""
        if (currentVal.length == 1) {
            newVal = currentVal + fingeringVal
        } else {
            newVal = fingeringVal
        }
        currentSelectedColumn.notes[string] = newVal
        invalidate()
    }

    fun clearColumn() {
        val index = columnNotesList.indexOf(currentSelectedColumn)
        currentSelectedColumn.clearColumn()
        columnNotesList[index]
        invalidate()
    }

    fun clearString(string: Int) {
        currentSelectedColumn.notes[string] = ""
        invalidate()
    }

    fun clearAll() {
        for (column in columnNotesList) {
            column.clearColumn()
        }
        invalidate()
    }

    fun nextColumn() {
        val currentColumnIndex = columnNotesList.indexOf(currentSelectedColumn)
        val currentColumn = currentColumnIndex + 1
        if (currentColumn < numberOfColumns) {
            val nextColumn = columnNotesList[currentColumnIndex + 1]
            currentSelectedColumn = nextColumn
        }
        invalidate()
    }

    fun prevColumn() {
        val currentColumnIndex = columnNotesList.indexOf(currentSelectedColumn)
        if (currentColumnIndex > 0) {
            currentSelectedColumn = columnNotesList[currentColumnIndex - 1]
        }
        invalidate()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val x = event.x
        val y = event.y

        for (column in columnNotesList) {

            if (column.inColumnBound(x, y)) {
                currentSelectedColumn = column
                invalidate()
            }
        }

        return super.onTouchEvent(event)
    }

    fun getCurrentSelectedColumn(): Int {
        return columnNotesList.indexOf(currentSelectedColumn)
    }
}
