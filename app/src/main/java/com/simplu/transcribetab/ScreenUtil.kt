package com.simplu.transcribetab

import android.content.Context

object ScreenUtil {

    fun dpToPixel(dp: Int, context: Context): Int {
        val px = (context.resources.displayMetrics.density * dp).toInt()
        return px
    }

}