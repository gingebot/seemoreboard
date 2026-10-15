package com.seemoreboard.keyboard.utils

import android.util.Log
import com.seemoreboard.keyboard.ime.config.ImeConfig

object Logging {
    fun log(message: String) {
        Log.d(ImeConfig.TAG, message)
    }
}