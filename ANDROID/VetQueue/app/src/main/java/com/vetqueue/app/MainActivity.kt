package com.vetqueue.app

import android.app.Activity
import android.os.Bundle
import android.widget.TextView
import android.widget.LinearLayout
import android.view.Gravity

class MainActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(32, 32, 32, 32)
        }

        val title = TextView(this).apply {
            text = "Welcome to VetQueue"
            textSize = 26f
            gravity = Gravity.CENTER
        }

        val subtitle = TextView(this).apply {
            text = "Your veterinary care companion"
            textSize = 16f
            gravity = Gravity.CENTER
        }

        layout.addView(title)
        layout.addView(subtitle)

        setContentView(layout)
    }
}