package com.tlinq.transcribetab

import android.content.Context
import android.content.SharedPreferences
import android.view.View
import androidx.fragment.app.FragmentActivity
import androidx.preference.PreferenceManager
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseSequence
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseView
import uk.co.deanwild.materialshowcaseview.ShowcaseConfig

object ShowcaseHelper {

    var isShowing = false
    private var count = 0
    private lateinit var activity: FragmentActivity
    private lateinit var context: Context
    private var sequence: MaterialShowcaseSequence? = null

    fun init(context: Context, activity: FragmentActivity, sequenceId: String) {
        this.context = context
        this.activity = activity
        this.sequence = MaterialShowcaseSequence(activity, sequenceId)
        val config = ShowcaseConfig()
        config.delay = 0
        sequence?.setConfig(config)

        sequence?.setOnItemShownListener { itemView, position ->
            isShowing = true
        }

        sequence?.setOnItemDismissedListener { itemView, position ->
            isShowing = false

        }

    }

    fun addView(
        view: View,
        contentText: String = "",
        dismissText: String = "",
        skipText: String = "Skip",
        title: String = "",
        rectangle: Boolean = false
    ) {
        ++count
        val builder = MaterialShowcaseView.Builder(activity)
        builder.setTarget(view)
            .setDismissText(dismissText)
            .setContentText(contentText)
            .setTitleText(title)
            .setSkipText(skipText)
        if (rectangle) {
            builder.withRectangleShape()
        } else {
            builder.withCircleShape()
        }
        sequence?.addSequenceItem(builder.build())
    }

    fun startSequence() {
        val prefs: SharedPreferences = PreferenceManager
            .getDefaultSharedPreferences(context)
        val checkboxPreference = prefs.getBoolean("pref_cb_showcase", false)
        if (checkboxPreference) {
            MaterialShowcaseView.resetAll(context)
            prefs.edit().putBoolean("pref_cb_showcase", false).apply()
            sequence?.start()
        }
    }

    fun cleanUp() {
        count = 0
        isShowing = false
        sequence = null
    }
}