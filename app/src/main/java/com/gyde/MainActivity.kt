package com.gyde

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.gyde.mylibrary.screens.GydeHomeActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


    }

    fun startGyde(view: View) {
        startActivity(Intent(this@MainActivity, GydeHomeActivity::class.java))
    }
}