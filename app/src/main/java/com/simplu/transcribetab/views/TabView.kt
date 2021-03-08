package com.simplu.transcribetab.views

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import com.simplu.transcribetab.R

open class TabView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    var numberOfColumns: Int = 8

    private val COLUMN_BORDER_HEIGHT_EXTRA = ScreenUtil.dpToPixel(4, context)
    private val COLUMN_BORDER_WIDTH_EXTRA = ScreenUtil.dpToPixel(4, context)

    protected val NUMBER_OF_STRINGS = 6

    //Vertical spacing of each note boundary
    protected var noteVerticalSpace = ScreenUtil.dpToPixel(8, context)

    //Horizontal spacing between each column
    protected var columnHorizontalSpace = ScreenUtil.dpToPixel(8, context)

    //Size of each note boundary
    protected var noteBorderSize = 12

    protected val PADDING = ScreenUtil.dpToPixel(8, context)

    //width of column border
    private val COLUMN_BORDER_HEIGHT =
        (NUMBER_OF_STRINGS * noteBorderSize) + ((NUMBER_OF_STRINGS - 1) * noteVerticalSpace) + (PADDING)

    //Set up the objects for drawing
    protected val noteBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ContextCompat.getColor(context, R.color.backgroundColor)
        style = Paint.Style.FILL
        alpha = 50
    }

    protected val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textAlign = Paint.Align.CENTER
    }

    protected val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        strokeWidth = 1.5f

    }

    protected val columnNotesList = ArrayList<DrawableColumn>()

    init {
        context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.TablatureView,
            0, 0
        ).apply {
            try {
                textPaint.textSize = getFloat(
                    R.styleable.TablatureView_textSize,
                    12f
                ) * resources.displayMetrics.scaledDensity
                noteBorderSize =
                    ScreenUtil.dpToPixel(
                        getFloat(R.styleable.TablatureView_textSize, 12f).toInt(),
                        context
                    )
                noteVerticalSpace = ScreenUtil.dpToPixel(
                    getInt(R.styleable.TablatureView_verticalSpacing, 8),
                    context
                )
                columnHorizontalSpace = ScreenUtil.dpToPixel(
                    getInt(R.styleable.TablatureView_horizontalSpacing, 8),
                    context
                )
                numberOfColumns = calculateNumberOfColumns()

            } finally {
                recycle()
            }
        }
        initializeColumns()
    }

    //Calculates the number of columns that can fit on this screen based on screen width dp
    private fun calculateNumberOfColumns(): Int {
        val metrics = resources.displayMetrics
        val screenWidthDp = ScreenUtil.pixelToDp(metrics.widthPixels, context)
        val divisor = ScreenUtil.pixelToDp(noteBorderSize + (COLUMN_BORDER_WIDTH_EXTRA) + columnHorizontalSpace, context)
        val numberOfColumns = (screenWidthDp-(ScreenUtil.pixelToDp(PADDING, context))) / divisor
        return numberOfColumns
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        var desiredWidth = 0
        var desiredHeight = COLUMN_BORDER_HEIGHT + PADDING
        //Width will change dynamically so recalculate the width
        if (columnNotesList.size != 0) {
            val rightMostNote = columnNotesList[columnNotesList.size - 1].getNoteRightBound()
            desiredWidth = rightMostNote + PADDING
            desiredHeight = columnNotesList[0].columnBound.height() + COLUMN_BORDER_HEIGHT_EXTRA
        } else {
            desiredWidth = noteBorderSize + (2 * columnHorizontalSpace) + PADDING
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
    }

    private fun drawNotes(canvas: Canvas) {

        for (column in columnNotesList) {
            for (i in 0..5) {
                val noteVal = column.notes[i]
                val noteBound = column.noteBound[i]
                drawCenterTextRect(canvas, noteVal, noteBound)
            }
        }
    }

    //Draws text centered inside a rect
    protected fun drawCenterTextRect(canvas: Canvas, text: String, rect: Rect) {
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

    fun initializeColumns() {

        val left = 0 + PADDING
        val top = 0 + PADDING
        //Create a column of notes at starting coordinates
        val column = createColumnNotes(
            left, top
        )

        columnNotesList.add(column)
        for (i in 1 until numberOfColumns) {
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

            val startingX =
                columnNotesList[firstColumnIndex].getNoteLeftBound() - columnHorizontalSpace
            val endingX =
                columnNotesList[lastColumnIndex].getNoteRightBound() + columnHorizontalSpace
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
        val x = firstNoteBound.left - columnHorizontalSpace.toFloat()
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

        val x = firstNoteBound.right + columnHorizontalSpace.toFloat()
        val startingY = firstNoteBound.exactCenterY()
        val endingY = lastNoteBound.exactCenterY()
        canvas.drawLine(x, startingY, x, endingY, linePaint)
    }

    //Adds a column to the end of tab
    private fun addColumnToEnd() {
        //Left and top of column
        val top = 0 + PADDING //Top will be same
        val left = columnNotesList.last().getColumnRightBound() + columnHorizontalSpace
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
            noteBorder.bottom = noteBorder.top + noteBorderSize
            noteBorder.right = noteBorder.left + noteBorderSize
            currentTop = noteBorder.bottom + noteVerticalSpace

            noteBounds[i] = noteBorder
        }
        //The column bounds
        val columnBoundLeft = left - COLUMN_BORDER_WIDTH_EXTRA
        val columnBoundTop = top - COLUMN_BORDER_HEIGHT_EXTRA
        val columnBoundRight = noteBounds[5]!!.right + COLUMN_BORDER_WIDTH_EXTRA
        val columnBoundBottom = noteBounds[5]!!.bottom + COLUMN_BORDER_HEIGHT_EXTRA

        return DrawableColumn(
            noteBound = noteBounds.requireNoNulls(),
            columnBound = Rect(columnBoundLeft, columnBoundTop, columnBoundRight, columnBoundBottom)
        )

    }

    fun updateTablature(columnValues: ArrayList<Array<String>>) {

        for (i in 0 until numberOfColumns) {
            columnNotesList[i].setNoteValues(columnValues[i])
        }

        invalidate()
    }

}
