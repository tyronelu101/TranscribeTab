package com.simplu.transcribetab.customviews

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.util.Log
import android.view.View


class EditTabView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    val SCALE_DENSITY = resources.displayMetrics.scaledDensity.toInt()

    //The starting x-coordinate to draw
    val NUMBER_OF_STRINGS = 6

    //Starting coordinates to draw the first column
    val STARTING_LEFT = 0 * SCALE_DENSITY
    val STARTING_TOP = 64 * SCALE_DENSITY

    //Vertical spacing of each note boundary
    val VERTICAL_SPACE = 16 * SCALE_DENSITY

    //Horizontal spacing between each column
    val HORIZONTAL_SPACE = 16 * SCALE_DENSITY

    //Size of each note boundary
    val NOTE_BORDER_SIZE = 16 * SCALE_DENSITY

    //width of column border
    val COLUMN_BORDER_WIDTH = NOTE_BORDER_SIZE + HORIZONTAL_SPACE
    val COLUMN_BORDER_HEIGHT =
        (NUMBER_OF_STRINGS * NOTE_BORDER_SIZE) + ((NUMBER_OF_STRINGS - 1) * VERTICAL_SPACE) + (VERTICAL_SPACE)
    val PADDING = 16 * SCALE_DENSITY

    //Set up the objects for drawing
    val noteBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.RED
        style = Paint.Style.STROKE

    }

    val columnBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.GREEN
    }

    val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.BLACK
    }

    val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.BLACK
    }

    var selectedColumn = 1
    val columnNotesList = ArrayList<ColumnNotes>()

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        Log.v("EditTab", "on measure called")
        setMeasuredDimension(1000, 500)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        initializeColumns(canvas)
        drawLines(canvas)

    }

    //Draws the columns onto canvas. Creates a single column before drawing if column list is empty
    private fun initializeColumns(canvas: Canvas) {

        if (columnNotesList.size == 0) {
            //Create a column of notes at starting coordinates
            createColumnNotes(STARTING_LEFT + PADDING + HORIZONTAL_SPACE, STARTING_TOP)
        }

        /**Delete later. For testing purposes only**/
        for (column in columnNotesList) {

            //Draw the column bound
            canvas.drawRect(column.columnBound, columnBorderPaint)

            val columnNotes = column.notes
            //Draw the column notes
            for (note in columnNotes) {
                canvas.drawRect((note.bound), noteBorderPaint)
            }
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
    fun addColumnToEnd() {

        val lastColumnIndex = columnNotesList.size - 1

        //Left and top of column
        val top = STARTING_TOP //Top will be same
        val left = columnNotesList.get(lastColumnIndex).getNoteRightBound() + HORIZONTAL_SPACE
        createColumnNotes(left, top)
        invalidate()
    }

    //Creates a column of notes with left and top as starting coordinates and adds it to columnlist
    private fun createColumnNotes(left: Int, top: Int) {

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
        val columnBoundLeft = left - HORIZONTAL_SPACE/2
        val columnBoundTop = top - VERTICAL_SPACE/2
        val columnBoundRight = columnBoundLeft + COLUMN_BORDER_WIDTH
        val columnBoundBottom = columnBoundTop + COLUMN_BORDER_HEIGHT

        val columnBounds =
            Rect(columnBoundLeft, columnBoundTop, columnBoundRight, columnBoundBottom)
        columnNotesList.add(
            ColumnNotes(
                columnBound = columnBounds,
                notes = columnNotes.requireNoNulls()
            )
        )
    }


}