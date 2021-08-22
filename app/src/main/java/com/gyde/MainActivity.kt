package com.gyde

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.gyde.mylibrary.screens.GydeHomeActivity
import com.gyde.mylibrary.utils.GydeExternalMethods
import com.gyde.mylibrary.utils.GydeTooltipPosition

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        getDeepLinkingData()
    }

    private fun getDeepLinkingData() {
        try {
            val uri: Uri? = intent.data
            if (uri != null) {
                val parameters = uri.pathSegments
                val param = parameters[parameters.size - 1]
                startActivity(
                    Intent(this@MainActivity, GydeHomeActivity::class.java).putExtra(
                        "GYDE_DEEP_LINK_DATA", param
                    )
                )
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    fun startGyde(view: View) {

        GydeExternalMethods.showGydeTooltipOnView(
            context = this@MainActivity,
            viewId = R.id.btn_list_demo,
            title = "Sign Up",
            description = "Don't have an account? \nPlease click here to register!!!",
            tooltipPosition = GydeTooltipPosition.DRAW_BOTTOM_CENTER,
            buttonText = "Click Me"
        )
    }
}