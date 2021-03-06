package com.simplu.transcribetab

import android.content.Context

object ScreenUtil {

    fun dpToPixel(dp: Int, context: Context): Int {
        val px = (context.resources.displayMetrics.density * dp).toInt()
        return px
    }

    fun pixelToDp(px: Int, context: Context): Int {
        val dp = (px / (context.resources.displayMetrics.density))
        return dp.toInt()
    }

}