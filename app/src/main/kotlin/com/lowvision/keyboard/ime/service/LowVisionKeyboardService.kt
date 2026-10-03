package com.lowvision.keyboard.ime.service

import android.inputmethodservice.InputMethodService
import android.view.View
import android.view.inputmethod.EditorInfo
import com.lowvision.keyboard.ime.integration.InputConnectionBridge
import com.lowvision.keyboard.ui.components.SingleKeyView
import com.lowvision.keyboard.ui.keyboard.KeyboardViewModel

class LowVisionKeyboardService : InputMethodService() {

    private lateinit var inputBridge: InputConnectionBridge
    private lateinit var viewModel: KeyboardViewModel

    override fun onCreate() {
        super.onCreate()
        inputBridge = InputConnectionBridge()
    }

    override fun onCreateInputView(): View {
        viewModel = KeyboardViewModel(inputBridge)
        return SingleKeyView(this, viewModel)
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