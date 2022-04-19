package com.gyde.mylibrary.screens

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.ViewGroup
import android.view.Window
import androidx.appcompat.widget.AppCompatTextView
import com.google.android.material.button.MaterialButton
import com.gyde.mylibrary.R
import com.gyde.mylibrary.utils.Util

internal class CommonDialog(
    context: Context,
    private val listener: CommonDialogListener,
    private val title: String,
    private val message: String
) :
    Dialog(context) {
    init {
        setCancelable(false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.common_dialog)
        window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        val tvTitle = findViewById<AppCompatTextView>(R.id.tv_dialog_title)
        val tvMessage = findViewById<AppCompatTextView>(R.id.tv_dialog_message)
        val btnOk = findViewById<MaterialButton>(R.id.btn_ok)

        tvTitle.text = title
        tvMessage.text = message
        btnOk.setBackgroundColor(Color.parseColor(Util.btnColor))
        tvTitle.setTextColor(Color.parseColor(Util.btnColor))

        btnOk.setOnClickListener {
            listener.onOkClicked(context)
            dismiss()
        }
    }

    interface CommonDialogListener {
        fun onOkClicked(context: Context)
    }
}