package com.simplu.transcribetab.customviews

import android.graphics.Rect

//Simple data class a single note. Holds a string for fingering and rect for bound
data class Note(var fingeringVal: String = "", val bound: Rect)
