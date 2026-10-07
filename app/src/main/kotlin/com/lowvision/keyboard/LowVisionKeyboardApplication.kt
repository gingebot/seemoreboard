package com.lowvision.keyboard

import android.app.Application
import androidx.emoji2.bundled.BundledEmojiCompatConfig
import androidx.emoji2.text.EmojiCompat

class LowVisionKeyboardApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        EmojiCompat.init(
            BundledEmojiCompatConfig(this)
                .setReplaceAll(true)
        )
        EmojiCompat.get().load()
    }
}