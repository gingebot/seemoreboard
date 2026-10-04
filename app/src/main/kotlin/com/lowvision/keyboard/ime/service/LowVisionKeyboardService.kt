package com.lowvision.keyboard.ime.service

import android.inputmethodservice.InputMethodService
import android.view.View
import android.view.inputmethod.EditorInfo
import com.lowvision.keyboard.data.settings.DataStoreSettingsRepository
import com.lowvision.keyboard.domain.settings.SettingsUseCases
import com.lowvision.keyboard.ime.integration.InputConnectionBridge
import com.lowvision.keyboard.ui.components.QwertyKeyboardView
import com.lowvision.keyboard.ui.keyboard.KeyboardViewModel

class LowVisionKeyboardService : InputMethodService() {

    private lateinit var inputBridge: InputConnectionBridge
    private lateinit var settingsUseCases: SettingsUseCases
    private lateinit var viewModel: KeyboardViewModel

    override fun onCreate() {
        super.onCreate()
        inputBridge = InputConnectionBridge()
        val repository = DataStoreSettingsRepository(this)
        settingsUseCases = SettingsUseCases(repository)
    }

    override fun onCreateInputView(): View {
        viewModel = KeyboardViewModel(inputBridge, settingsUseCases)
        return QwertyKeyboardView(this, viewModel)
    }

    override fun onStartInputView(info: EditorInfo?, restarting: Boolean) {
        super.onStartInputView(info, restarting)
        inputBridge.setInputConnection(currentInputConnection)
    }

    override fun onFinishInputView(finishingInput: Boolean) {
        super.onFinishInputView(finishingInput)
        inputBridge.clearInputConnection()
    }

    override fun onEvaluateInputViewShown(): Boolean {
        return true
    }
}