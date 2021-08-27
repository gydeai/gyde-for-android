package com.gyde

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.gyde.mylibrary.screens.GydeHomeActivity
import com.gyde.mylibrary.utils.GydeExternalMethods
import com.gyde.mylibrary.utils.GydeTooltipPosition
import com.gyde.mylibrary.utils.Util

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
                val flowIdParam = parameters[parameters.size - 2]
                val withVoiceOverParam = parameters[parameters.size - 1]

                val bundle = Bundle()
                bundle.putString(Util.keyFlowId, flowIdParam)
                bundle.putString(Util.keyVoiceOver, withVoiceOverParam)

                startActivity(
                    Intent(this@MainActivity, GydeHomeActivity::class.java).putExtras(
                        bundle
                    )
                )
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    fun openGydeWalkthroughList(view: View) {

        startActivity(Intent(this@MainActivity, GydeHomeActivity::class.java))
    }

    fun showGydeTooltip(view: View) {
        GydeExternalMethods.showGydeTooltip(
            context = this@MainActivity,
            viewId = R.id.btn_start_walkthrough,
            title = "Start Walkthrough",
            description = "You can start walkthrough by ID",
            tooltipPosition = GydeTooltipPosition.DRAW_TOP_CENTER,
            buttonText = "Click Me"
        )
    }

    fun startGydeWalkthrough(view: View) {
        GydeExternalMethods.startGydeWalkthrough(
            this@MainActivity,
            "934e442d-b0cc-4b63-bef1-3292527824ad"
        )
    }
}
