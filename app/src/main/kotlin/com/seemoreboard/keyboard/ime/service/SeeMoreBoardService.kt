package com.seemoreboard.keyboard.ime.service

import android.inputmethodservice.InputMethodService
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.EditorInfo
import android.widget.FrameLayout
import androidx.compose.ui.platform.ComposeView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.seemoreboard.keyboard.data.settings.DataStoreSettingsRepository
import com.seemoreboard.keyboard.domain.settings.SettingsUseCases
import com.seemoreboard.keyboard.ime.integration.InputConnectionBridge
import com.seemoreboard.keyboard.ui.keyboard.KeyboardScreen
import com.seemoreboard.keyboard.ui.keyboard.KeyboardViewModel

class SeeMoreBoardService : InputMethodService() {

    private lateinit var inputBridge: InputConnectionBridge
    private lateinit var settingsUseCases: SettingsUseCases
    private lateinit var viewModel: KeyboardViewModel
    private lateinit var lifecycleOwner: LifecycleOwner

    override fun onCreate() {
        super.onCreate()
        inputBridge = InputConnectionBridge()
        val repository = DataStoreSettingsRepository(this)
        settingsUseCases = SettingsUseCases(repository)
        lifecycleOwner = object : LifecycleOwner, SavedStateRegistryOwner {
            private val registry = LifecycleRegistry(this).apply {
                currentState = Lifecycle.State.INITIALIZED
            }
            override val lifecycle: Lifecycle get() = registry
            private val controller = SavedStateRegistryController.create(this).apply {
                performRestore(null)
            }
            override val savedStateRegistry = controller.savedStateRegistry

            init {
                registry.currentState = Lifecycle.State.RESUMED
            }
        }
    }

    override fun onCreateInputView(): View {
        viewModel = KeyboardViewModel(applicationContext, inputBridge, settingsUseCases)

        val composeView = ComposeView(this).apply {
            setContent { KeyboardScreen(viewModel) }
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        }

        val wrapper = object : FrameLayout(this) {
            override fun onAttachedToWindow() {
                rootView.setViewTreeLifecycleOwner(lifecycleOwner)
                rootView.setViewTreeSavedStateRegistryOwner(
                    lifecycleOwner as SavedStateRegistryOwner
                )
                super.onAttachedToWindow()
            }
        }.apply {
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
            addView(composeView)
        }
        return wrapper
    }

    override fun onStartInputView(info: EditorInfo?, restarting: Boolean) {
        super.onStartInputView(info, restarting)
        inputBridge.setInputConnection(currentInputConnection)
        viewModel.setEditorInfo(info)
    }

    override fun onFinishInputView(finishingInput: Boolean) {
        super.onFinishInputView(finishingInput)
        inputBridge.clearInputConnection()
    }

    override fun onEvaluateInputViewShown(): Boolean {
        super.onEvaluateInputViewShown()
        return true
    }

}
