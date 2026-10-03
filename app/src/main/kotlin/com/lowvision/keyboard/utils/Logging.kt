package com.lowvision.keyboard.utils

import android.util.Log
import com.lowvision.keyboard.ime.config.ImeConfig

object Logging {
    fun log(message: String) {
        Log.d(ImeConfig.TAG, message)
    }
}