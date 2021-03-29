package com.simplu.transcribetab.views

import android.content.Context

object ScreenUtil {

    fun dpToPixel(dp: Int, context: Context): Int {
        val px = (context.resources.displayMetrics.density * dp)
        return Math.round(px)
    }

    fun pixelToDp(px: Int, context: Context): Int {
        val dp = (px / (context.resources.displayMetrics.density))
        return Math.round(dp)
    }

}