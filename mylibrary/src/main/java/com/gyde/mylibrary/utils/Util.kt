package com.gyde.mylibrary.utils

import android.annotation.SuppressLint
import android.content.Context
import android.provider.Settings
import android.util.Log
import com.gyde.mylibrary.network.response.walkthroughlist.HelpArticle
import com.gyde.mylibrary.network.response.walkthroughlist.Walkthrough
import com.gyde.mylibrary.network.response.walkthroughsteps.Step
import java.util.*

class Util {

    companion object {

        lateinit var helpArticle: List<HelpArticle>
        lateinit var walkthroughList: List<Walkthrough>
        lateinit var languageOptions: Array<String>
        var walkthroughSteps = mutableListOf<Step>()
        var stepCounter = 0
        var isPlayVoiceOverEnabled = false
        var headerColor = ""
        var headerTextColor = ""
        var btnColor = ""
        var deepLinkData: String = ""
        var isDeepLink = false
        var appId = ""
        var selectedLanguage = "English"

        @SuppressLint("HardwareIds")
        fun getUuid(context: Context): String {
            val androidId: String =
                Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
            val uid = UUID.nameUUIDFromBytes(androidId.toByteArray())

            Log.e("android Id", ": $androidId")
            Log.e("android uuId", ": $uid")
            return uid.toString()
        }
    }
}