package com.simplu.transcribetab

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import com.simplu.transcribetab.customviews.EditTabView

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val view = findViewById<EditTabView>(R.id.tablature_view)
        val btn = findViewById<Button>(R.id.add_column)
        btn.setOnClickListener {
            view.addColumnToEnd()
        }

    }
}
