package com.gyde.mylibrary.utils

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import android.telephony.TelephonyManager
import android.util.Log
import androidx.core.content.ContextCompat
import java.io.UnsupportedEncodingException
import java.util.*


@SuppressLint("HardwareIds")
class DeviceUuidFactory(context: Context) {
    val uuid: UUID?
        get() = Companion.uuid

    companion object {
        var uuid: UUID? = null
    }

    init {
        if (Companion.uuid == null) {
            synchronized(DeviceUuidFactory::class.java) {
                if (Companion.uuid == null) {

                    val androidId = Settings.Secure.getString(
                        context.contentResolver, Settings.Secure.ANDROID_ID
                    )

                    try {
                        if ("9774d56d682e549c" != androidId) {
                            Companion.uuid = UUID.nameUUIDFromBytes(
                                androidId
                                    .toByteArray(charset("utf8"))
                            )
                        } else {
                            val  telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
                            if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE)
                                == PackageManager.PERMISSION_GRANTED) {
                                // Permission is  granted
                                val imei : String? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                    telephonyManager.imei
                                } else { // older OS  versions
                                    telephonyManager.deviceId
                                }

                                imei?.let {
                                    Log.e("Log", "DeviceId=$imei" )
                                }

                            } else {  // Permission is not granted

                            }
                        }
                    } catch (e: UnsupportedEncodingException) {
                        throw RuntimeException(e)
                    }
                }

            }
        }
    }
}