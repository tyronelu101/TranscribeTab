package com.simplu.transcribetab.edittab.customviews

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View


class EdittableTabView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    fun dpToPixel(dp: Int): Int {
        val px = (resources.displayMetrics.density * dp).toInt()
        return px
    }

    val NUMBER_OF_STRINGS = 6

    //Starting coordinates to draw the first column
    val STARTING_LEFT = dpToPixel(0)
    val STARTING_TOP = dpToPixel(0)

    //Vertical spacing of each note boundary
    val VERTICAL_SPACE = dpToPixel(8)

    //Horizontal spacing between each column
    val HORIZONTAL_SPACE = dpToPixel(12)

    //Size of each note boundary
    val NOTE_BORDER_SIZE = dpToPixel(20)

    val PADDING = dpToPixel(16)

    //width of column border
    val COLUMN_BORDER_WIDTH = NOTE_BORDER_SIZE + HORIZONTAL_SPACE
    val COLUMN_BORDER_HEIGHT =
        (NUMBER_OF_STRINGS * NOTE_BORDER_SIZE) + ((NUMBER_OF_STRINGS - 1) * VERTICAL_SPACE) + (PADDING)
    val BAR_NUMBER_PADDING = dpToPixel(8)

    //Set up the objects for drawing
    val noteBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        style = Paint.Style.FILL
    }

    val columnBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.BLACK
        alpha = 50
    }

    val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.BLACK
        textSize = 12f * resources.displayMetrics.scaledDensity
        textAlign = Paint.Align.CENTER
    }

    val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.BLACK
        strokeWidth = 1.5f

    }

    lateinit var currentSelectedColumn: Column
    val columnNotesList = ArrayList<Column>()
    private var barNumberCount: Int = 0
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        var desiredWidth = 0
        var desiredHeight = COLUMN_BORDER_HEIGHT + PADDING
        //Width will change dynamically so recalculate the width
        if (columnNotesList.size != 0) {
            val rightMostNote = columnNotesList[columnNotesList.size - 1].getNoteRightBound()
            desiredWidth = rightMostNote + HORIZONTAL_SPACE + PADDING
        } else {
            desiredWidth = NOTE_BORDER_SIZE + (2 * HORIZONTAL_SPACE) + PADDING
        }
        setMeasuredDimension(
            desiredWidth,
            desiredHeight
        )

    }

    override fun onDraw(canvas: Canvas) {
        barNumberCount = 0
        super.onDraw(canvas)
        initializeColumns(canvas)
        drawLines(canvas)
        drawTablature(canvas)
        drawSelectedColumn(canvas)
    }

    private fun drawSelectedColumn(canvas: Canvas) {
        canvas.drawRect(currentSelectedColumn?.columnBound, columnBorderPaint)
    }

    private fun drawTablature(canvas: Canvas) {

        for (column in columnNotesList) {
            if (column.isBar) {
                drawBar(canvas, column)
                continue
            } else
                drawNotes(canvas, column)
        }
    }

    private fun drawNotes(canvas: Canvas, column: Column) {
        for (note in column.notes) {
//            canvas.drawRect(note.bound, noteBorderPaint)
            val noteVal = note.fingeringVal
            val noteBound = note.bound
            drawCenterTextRect(canvas, noteVal, noteBound)
        }
    }

    //Draws text centered inside a rect
    private fun drawCenterTextRect(canvas: Canvas, text: String, rect: Rect) {
        var textBound = Rect()
        textPaint.getTextBounds(text, 0, text.length, textBound)
        val offSet = (rect.height() - textBound.height()) / 2f
        canvas.drawText(
            text,
            rect.exactCenterX(),
            rect.bottom.toFloat() - offSet,
            textPaint
        )
    }

    private fun drawBar(canvas: Canvas, column: Column) {

        val notes = column.notes
        val startX = notes[0].bound.exactCenterX()
        val endX = startX
        val startY = notes[0].bound.exactCenterY()
        val endY = notes[5].bound.exactCenterY();

        canvas.drawLine(startX, startY, endX, endY, linePaint)

        val textX = column.columnBound.exactCenterX()
        val textY = BAR_NUMBER_PADDING + 30f
        canvas.drawText(barNumberCount.toString(), textX, textY, textPaint)
        ++barNumberCount

    }

    //ToDo Load the columns from db. Creates a single column if column list is empty
    private fun initializeColumns(canvas: Canvas) {

        if (columnNotesList.size == 0) {
            val left = STARTING_LEFT + PADDING + HORIZONTAL_SPACE
            val top = STARTING_TOP + PADDING
            //Create a column of notes at starting coordinates
            val column = createColumnNotes(
                left, top
            )
            currentSelectedColumn = column
            columnNotesList.add(column)

        }
    }

    //Draws the left, right, and horizontal lines of tablature
    private fun drawLines(canvas: Canvas) {
        drawLeftVerticalLine(canvas)
        drawRightVerticalLine(canvas)
        drawHorizontalLines(canvas)
    }

    //Draws the horizontal lines of tablature
    private fun drawHorizontalLines(canvas: Canvas) {

        val firstColumnIndex = 0
        val lastColumnIndex = columnNotesList.size - 1

        for (i in 0 until NUMBER_OF_STRINGS) {

            val startingX = columnNotesList[firstColumnIndex].getNoteLeftBound() - HORIZONTAL_SPACE
            val endingX = columnNotesList[lastColumnIndex].getNoteRightBound() + HORIZONTAL_SPACE
            val startingY = columnNotesList[firstColumnIndex].notes[i].bound.exactCenterY()
            val endingY = startingY
            canvas.drawLine(startingX.toFloat(), startingY, endingX.toFloat(), endingY, linePaint)

        }

    }

    //Draw the left vertical line of tablature based on positioning of first column
    private fun drawLeftVerticalLine(canvas: Canvas) {

        val lastStringIndex = NUMBER_OF_STRINGS - 1

        val firstColumnIndex = 0
        val firstNoteBound = columnNotesList.get(firstColumnIndex).notes[0].bound
        val lastNoteBound = columnNotesList.get(firstColumnIndex).notes[lastStringIndex].bound
        val x = firstNoteBound.left - HORIZONTAL_SPACE.toFloat()
        val startingY = firstNoteBound.exactCenterY()
        val endingY = lastNoteBound.exactCenterY()

        canvas.drawLine(x, startingY, x, endingY, linePaint)

        val textX = x
        val textY = BAR_NUMBER_PADDING + 30f
        canvas.drawText(barNumberCount.toString(), textX, textY, textPaint)
        barNumberCount++
    }

    //Draw the last vertical line of tablature based on positioning of last column
    private fun drawRightVerticalLine(canvas: Canvas) {

        val lastStringIndex = NUMBER_OF_STRINGS - 1

        val lastColumnIndex = columnNotesList.size - 1
        val firstNoteBound = columnNotesList.get(lastColumnIndex).notes[0].bound
        val lastNoteBound = columnNotesList.get(lastColumnIndex).notes[lastStringIndex].bound

        val x = firstNoteBound.right + HORIZONTAL_SPACE.toFloat()
        val startingY = firstNoteBound.exactCenterY()
        val endingY = lastNoteBound.exactCenterY()
        canvas.drawLine(x, startingY, x, endingY, linePaint)
    }

    //Adds a column to the end of tab
    fun addColumnToEnd(moveToNewCol: Boolean) {
        //Left and top of column
        val top = STARTING_TOP + PADDING //Top will be same
        val left = columnNotesList.last().getColumnRightBound() + HORIZONTAL_SPACE
        val newColumn = createColumnNotes(left, top)
        columnNotesList.add(newColumn)
        if (moveToNewCol) {
            currentSelectedColumn = newColumn
        }

        invalidate()
        //Adding column will increase width of so call requestLayout to remeasure
        requestLayout()
    }

    //Creates a column of notes with left and top as starting coordinates and returns it
    private fun createColumnNotes(left: Int, top: Int): Column {

        var currentTop = top
        val columnNotes = arrayOfNulls<Note>(NUMBER_OF_STRINGS)

        //The note bounds
        for (i in 0 until NUMBER_OF_STRINGS) {

            var noteBorder = Rect(0, 0, 0, 0)

            noteBorder.left = left
            noteBorder.top = currentTop
            noteBorder.bottom = noteBorder.top + NOTE_BORDER_SIZE
            noteBorder.right = noteBorder.left + NOTE_BORDER_SIZE
            currentTop = noteBorder.bottom + VERTICAL_SPACE

            columnNotes[i] = Note(bound = noteBorder)
        }

        //The column bounds
        val columnBoundLeft = left - HORIZONTAL_SPACE / 2
        val columnBoundTop = top - VERTICAL_SPACE / 2
        val columnBoundRight = columnBoundLeft + COLUMN_BORDER_WIDTH
        val columnBoundBottom = columnBoundTop + COLUMN_BORDER_HEIGHT

        val columnBounds =
            Rect(columnBoundLeft, columnBoundTop, columnBoundRight, columnBoundBottom)
        return Column(
            columnBound = columnBounds,
            notes = columnNotes.requireNoNulls()

        )

    }

    fun insertNote(string: Int, fingeringVal: String) {

        if (currentSelectedColumn.isBar) {
            currentSelectedColumn.isBar = false
        }
        var currentVal = currentSelectedColumn.notes[string].fingeringVal
        var newVal = ""
        if (currentVal.length == 1) {
            newVal = currentVal + fingeringVal
        } else {
            newVal = fingeringVal
        }
        currentSelectedColumn.notes[string].fingeringVal = newVal
        invalidate()
    }

    fun clearColumn() {
        currentSelectedColumn.clearColumn()
        invalidate()
    }

    fun clearString(string: Int) {
        currentSelectedColumn.notes[string].fingeringVal = ""
        invalidate()
    }

    fun insertBar() {
        columnNotesList.indexOf(currentSelectedColumn)
        currentSelectedColumn.clearColumn()
        currentSelectedColumn.isBar = true
        invalidate()
    }

    fun insertColumn() {

        addColumnToEnd(false)

        for (column in columnNotesList.size - 1 downTo columnNotesList.indexOf(currentSelectedColumn) + 1) {
            if (columnNotesList[column - 1].isBar) {
                columnNotesList[column].isBar = true
                columnNotesList[column - 1].clearColumn()
            } else {
                val newNotes = columnNotesList[column - 1].getNoteValues()
                columnNotesList[column].setNoteValues(newNotes)
            }
        }

        currentSelectedColumn.clearColumn()

        invalidate()
        requestLayout()
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

    public fun deleteCol() {

        //Delete only if there is more than 1 column
        if (columnNotesList.size > 1) {

            //Shift note values down towards current selected column then delete last column
            for (column in columnNotesList.indexOf(currentSelectedColumn) until columnNotesList.size - 1) {
                val columnToReplace = columnNotesList[column]
                columnToReplace.clearColumn()
                if (columnNotesList[column + 1].isBar) {
                    columnToReplace.isBar = true
                } else {
                    columnToReplace.isBar = false
                    val newNotes = columnNotesList[column + 1].getNoteValues()
                    columnToReplace.setNoteValues(newNotes)
                }
            }

            //Update the current selected column if column that was deleted was the last column
            if (columnNotesList.indexOf(currentSelectedColumn) == columnNotesList.size - 1) {
                currentSelectedColumn = columnNotesList.get(columnNotesList.size - 2)
            }

            columnNotesList.removeAt(columnNotesList.size - 1)
        }
        invalidate()
    }

    public fun getNotes() = columnNotesList

}