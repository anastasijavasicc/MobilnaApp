package com.example.tourapp

import android.app.Activity
import android.os.Bundle
import android.os.PersistableBundle
import android.view.View
import android.widget.Button

class About: Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.about)
        val ok:Button = findViewById<Button>(R.id.about_ok)
        ok.setOnClickListener(object: View.OnClickListener {
            override fun onClick(p0: View?) {
                finish()
            }
        })
    }
}