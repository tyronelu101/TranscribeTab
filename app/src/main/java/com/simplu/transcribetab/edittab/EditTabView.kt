package com.simplu.transcribetab.edittab

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import androidx.core.content.ContextCompat
import com.simplu.transcribetab.DrawableColumn
import com.simplu.transcribetab.R

class EditTabView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    companion object {

        val NUMBER_OF_COLUMNS = 10

    }

    fun dpToPixel(dp: Int): Int {
        val px = (resources.displayMetrics.density * dp).toInt()
        return px
    }

    private val NUMBER_OF_STRINGS = 6

    //Starting coordinates to draw the first column
    private val STARTING_LEFT = dpToPixel(0)
    private val STARTING_TOP = dpToPixel(0)

    //Vertical spacing of each note boundary
    private val VERTICAL_SPACE = dpToPixel(8)

    //Horizontal spacing between each column
    private val HORIZONTAL_SPACE = dpToPixel(12)

    //Size of each note boundary
    private val NOTE_BORDER_SIZE = dpToPixel(16)

    private val PADDING = dpToPixel(16)

    //width of column border
    private val COLUMN_BORDER_WIDTH = NOTE_BORDER_SIZE + HORIZONTAL_SPACE
    private val COLUMN_BORDER_HEIGHT =
        (NUMBER_OF_STRINGS * NOTE_BORDER_SIZE) + ((NUMBER_OF_STRINGS - 1) * VERTICAL_SPACE) + (PADDING)
    private val BAR_NUMBER_PADDING = dpToPixel(8)

    //Set up the objects for drawing
    private val noteBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ContextCompat.getColor(context, R.color.tab_lines_color)
        style = Paint.Style.FILL
    }

    private val columnBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.BLACK
        alpha = 50
    }

    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.BLACK
        textSize = 12f * resources.displayMetrics.scaledDensity
        textAlign = Paint.Align.CENTER
    }

    private val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.BLACK
        strokeWidth = 1.5f

    }

    private lateinit var currentSelectedColumn: DrawableColumn
    private val columnNotesList = ArrayList<DrawableColumn>()


    init {
        Log.v("EditTab", "Initializing edit tab")
        initializeColumns()

    }

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
        super.onDraw(canvas)
        drawLines(canvas)
        drawNotes(canvas)
        drawSelectedColumn(canvas)
    }

    private fun drawSelectedColumn(canvas: Canvas) {
        canvas.drawRect(currentSelectedColumn?.columnBound, columnBorderPaint)
    }

    private fun drawNotes(canvas: Canvas) {

        for (column in columnNotesList) {
            for (i in 0..5) {
//            canvas.drawRect(note.bound, noteBorderPaint)
                val noteVal = column.notes[i]
                val noteBound = column.noteBound[i]
                drawCenterTextRect(canvas, noteVal, noteBound)
            }
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

    private fun drawBar(canvas: Canvas, column: DrawableColumn) {

        val bounds = column.noteBound
        val startX = bounds[0].exactCenterX()
        val endX = startX
        val startY = bounds[0].exactCenterY()
        val endY = bounds[5].exactCenterY();

        canvas.drawLine(startX, startY, endX, endY, linePaint)

        val textX = column.columnBound.exactCenterX()
        val textY = BAR_NUMBER_PADDING + 30f
    }

    private fun initializeColumns() {

        val left = STARTING_LEFT + PADDING + HORIZONTAL_SPACE
        val top = STARTING_TOP + PADDING
        //Create a column of notes at starting coordinates
        val column = createColumnNotes(
            left, top
        )
        currentSelectedColumn = column
        columnNotesList.add(column)
        for (i in 0 until NUMBER_OF_COLUMNS) {
            addColumnToEnd()
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
            val startingY = columnNotesList[firstColumnIndex].noteBound[i].exactCenterY()
            val endingY = startingY
            canvas.drawLine(startingX.toFloat(), startingY, endingX.toFloat(), endingY, linePaint)

        }

    }

    //Draw the left vertical line of tablature based on positioning of first column
    private fun drawLeftVerticalLine(canvas: Canvas) {

        val lastStringIndex = NUMBER_OF_STRINGS - 1

        val firstColumnIndex = 0
        val firstNoteBound = columnNotesList.get(firstColumnIndex).noteBound[0]
        val lastNoteBound = columnNotesList.get(firstColumnIndex).noteBound[lastStringIndex]
        val x = firstNoteBound.left - HORIZONTAL_SPACE.toFloat()
        val startingY = firstNoteBound.exactCenterY()
        val endingY = lastNoteBound.exactCenterY()

        canvas.drawLine(x, startingY, x, endingY, linePaint)
    }

    //Draw the last vertical line of tablature based on positioning of last column
    private fun drawRightVerticalLine(canvas: Canvas) {

        val lastStringIndex = NUMBER_OF_STRINGS - 1

        val lastColumnIndex = columnNotesList.size - 1
        val firstNoteBound = columnNotesList.get(lastColumnIndex).noteBound[0]
        val lastNoteBound = columnNotesList.get(lastColumnIndex).noteBound[lastStringIndex]

        val x = firstNoteBound.right + HORIZONTAL_SPACE.toFloat()
        val startingY = firstNoteBound.exactCenterY()
        val endingY = lastNoteBound.exactCenterY()
        canvas.drawLine(x, startingY, x, endingY, linePaint)
    }

    //Adds a column to the end of tab
    private fun addColumnToEnd() {
        //Left and top of column
        val top = STARTING_TOP + PADDING //Top will be same
        val left = columnNotesList.last().getColumnRightBound() + HORIZONTAL_SPACE
        val newColumn = createColumnNotes(left, top)
        columnNotesList.add(newColumn)
    }

    //Creates a column of notes with left and top as starting coordinates and returns it
    private fun createColumnNotes(left: Int, top: Int): DrawableColumn {

        var currentTop = top
        val noteBounds = arrayOfNulls<Rect>(NUMBER_OF_STRINGS)

        //The note bounds
        for (i in 0 until NUMBER_OF_STRINGS) {

            var noteBorder = Rect(0, 0, 0, 0)

            noteBorder.left = left
            noteBorder.top = currentTop
            noteBorder.bottom = noteBorder.top + NOTE_BORDER_SIZE
            noteBorder.right = noteBorder.left + NOTE_BORDER_SIZE
            currentTop = noteBorder.bottom + VERTICAL_SPACE

            noteBounds[i] = noteBorder
        }

        //The column bounds
        val columnBoundLeft = left - HORIZONTAL_SPACE / 2
        val columnBoundTop = top - VERTICAL_SPACE / 2
        val columnBoundRight = columnBoundLeft + COLUMN_BORDER_WIDTH
        val columnBoundBottom = columnBoundTop + COLUMN_BORDER_HEIGHT

        val columnBounds =
            Rect(columnBoundLeft, columnBoundTop, columnBoundRight, columnBoundBottom)

        return DrawableColumn(
            columnBound = columnBounds,
            noteBound = noteBounds.requireNoNulls()
        )

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
        currentSelectedColumn.clearColumn()
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
        val currentColIndex = columnNotesList.indexOf(currentSelectedColumn)
        if (currentColIndex < NUMBER_OF_COLUMNS) {
            val nextColumn = columnNotesList[currentColIndex + 1]
            currentSelectedColumn = nextColumn
        }
    }

    fun prevColumn() {
        val currentColIndex = columnNotesList.indexOf(currentSelectedColumn)
        if (currentColIndex > 0) {
            val prevColumn = columnNotesList[currentColIndex - 1]
            currentSelectedColumn = prevColumn
        }
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

    public fun getSelectedColumnNumber(): Int {
        return columnNotesList.indexOf(currentSelectedColumn);
    }

    public fun updateTablature(columnValues: ArrayList<Array<String>>) {

        for (i in 0..NUMBER_OF_COLUMNS) {
            columnNotesList[i].setNoteValues(columnValues[i])
        }

        invalidate()
    }

}
