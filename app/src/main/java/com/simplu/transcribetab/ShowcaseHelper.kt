        package com.simplu.transcribetab

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
    private lateinit var sequence: MaterialShowcaseSequence

    fun init(context: Context, activity: FragmentActivity, sequenceId: String) {
        this.context = context
        this.activity = activity
        this.sequence = MaterialShowcaseSequence(activity, sequenceId)
        val config = ShowcaseConfig()
        config.delay = 0
        sequence.setConfig(config)
        sequence.setOnItemDismissedListener { itemView, position ->
            //position starts at 0
            if ((position+1) == count) {
                val prefs: SharedPreferences = PreferenceManager
                    .getDefaultSharedPreferences(ShowcaseHelper.context)
                prefs.edit().putBoolean("pref_cb_showcase", false).apply()
                isShowing = false
            }
        }

    }

    fun addView(
        view: View,
        contentText: String = "content text",
        dismissText: String = "dismiss text",
        title: String = "title text",
        rectangle: Boolean = false
    ) {
        ++count
        val builder = MaterialShowcaseView.Builder(activity)
        builder.setTarget(view)
            .setDismissText(dismissText)
            .setContentText(contentText)
            .setTitleText(title)
        if (rectangle) {
            builder.withRectangleShape()
        } else {
            builder.withCircleShape()
        }
        sequence.addSequenceItem(builder.build())
    }

    fun startSequence() {
        val prefs: SharedPreferences = PreferenceManager
            .getDefaultSharedPreferences(context)
        val checkboxPreference = prefs.getBoolean("pref_cb_showcase", false)
        if (checkboxPreference) {
            isShowing = true
            sequence.start()
        }
    }

    fun cleanUp() {
        count = 0
        isShowing = false
        MaterialShowcaseView.resetAll(context)
    }
}