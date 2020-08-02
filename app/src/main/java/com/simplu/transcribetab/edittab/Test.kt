package com.simplu.transcribetab.edittab

import android.util.Log
import android.widget.SeekBar

class Test(val seekBar: SeekBar) {

    init {
        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                Log.v("Test", "Seekbar was clicked")
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                Log.v("Test", "Seekbar was start")

            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                Log.v("Test", "Seekbar was stop")

            }

        })
    }

}