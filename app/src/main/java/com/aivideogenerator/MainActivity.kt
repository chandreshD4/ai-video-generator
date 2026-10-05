package com.aivideogenerator

import android.app.Activity
import android.os.Bundle
import android.widget.TextView

class MainActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val textView = TextView(this)
        textView.text = "AI Video Generator"
        textView.textSize = 24f
        textView.setPadding(40, 40, 40, 40)

        setContentView(textView)
    }
}
