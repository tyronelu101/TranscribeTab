package com.simplu.transcribetab.tab

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.util.Log
import android.view.View
import com.simplu.transcribetab.edittab.Column
import com.simplu.transcribetab.edittab.DrawableColumn

class TablatureView @JvmOverloads constructor(
    private val columns: ArrayList<Column>? = null,
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

    val columnNotesList = ArrayList<DrawableColumn>()

    private var barNumberCount: Int = 0

    init {
        initializeColumns()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        Log.v("TablatureView", "onMeasure")
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

        Log.v("TablatureView", "onDraw")

        drawLines(canvas)
        drawTablature(canvas)
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

    private fun drawNotes(canvas: Canvas, column: DrawableColumn) {
        for (i in 0..5) {
            val noteVal = column.notes[i]
            val noteBound = column.noteBound[i]
            if (noteVal != "") {
                canvas.drawRect(noteBound, noteBorderPaint)
            }
            drawCenterTextRect(canvas, noteVal, noteBound)
            Log.v("TablatureView", "Drawing note")
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
        canvas.drawText(barNumberCount.toString(), textX, textY, textPaint)
        ++barNumberCount

    }

    //ToDo Load the columns from db. Creates a single column if column list is empty
    private fun initializeColumns() {
        Log.v("TablatureView", "Initialize column")

        if (columns != null) {
            //Initialize the first column
            val left = STARTING_LEFT + PADDING + HORIZONTAL_SPACE
            val top = STARTING_TOP + PADDING

            val column = createColumnNotes(
                left, top, columns[0]
            )
            columnNotesList.add(column)

            for (i in 1 until columns.size) {
                addColumnToEnd(columns[i]);
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

        val textX = x
        val textY = BAR_NUMBER_PADDING + 30f
        canvas.drawText(barNumberCount.toString(), textX, textY, textPaint)
        barNumberCount++
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

    //Creates a column of notes with left and top as starting coordinates and returns it
    private fun createColumnNotes(left: Int, top: Int, column: Column): DrawableColumn {

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
            noteBound = noteBounds.requireNoNulls(),
            isBar = column.isBar,
            notes = column.notes
        )

    }

    //Adds a column to the end of tab
    fun addColumnToEnd(column: Column) {
        //Left and top of column
        val top = STARTING_TOP + PADDING //Top will be same
        val left = columnNotesList.last().getColumnRightBound() + HORIZONTAL_SPACE
        val newColumn = createColumnNotes(left, top, column)
        columnNotesList.add(newColumn)

    }
}