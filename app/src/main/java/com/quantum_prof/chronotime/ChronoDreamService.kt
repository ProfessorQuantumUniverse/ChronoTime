package com.quantum_prof.chronotime

import android.service.dreams.DreamService
import androidx.compose.ui.platform.ComposeView
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.quantum_prof.chronotime.ui.theme.ChronoTimeTheme
import com.quantum_prof.chronotime.ui.MainScreen

class ChronoDreamService : DreamService() {
    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

        // Setup interactive mode
        isInteractive = true
        // Hide status bar
        isFullscreen = true

        val composeView = ComposeView(this).apply {
            setContent {
                ChronoTimeTheme(darkTheme = true) { // Always dark for screensaver mostly
                    MainScreen()
                }
            }
        }

        // DreamService doesn't automatically provide LifecycleOwner, etc. which Compose needs.
        // We need to attach lifecycle owners.

        val lifecycleOwner = object : androidx.lifecycle.LifecycleOwner {
            private val registry = androidx.lifecycle.LifecycleRegistry(this)
            init {
                // We must set the state to RESUMED so Compose can start
                registry.currentState = androidx.lifecycle.Lifecycle.State.RESUMED
            }
            override val lifecycle: androidx.lifecycle.Lifecycle get() = registry
        }

        composeView.setViewTreeLifecycleOwner(lifecycleOwner)

        // ViewModelStoreOwner is also needed for viewModel() calls, though we might not use them.
        val viewModelStore = androidx.lifecycle.ViewModelStore()
        val viewModelStoreOwner = object : androidx.lifecycle.ViewModelStoreOwner {
            override val viewModelStore: androidx.lifecycle.ViewModelStore = viewModelStore
        }
        composeView.setViewTreeViewModelStoreOwner(viewModelStoreOwner)

        val savedStateRegistryOwner = object : androidx.savedstate.SavedStateRegistryOwner {
             private val controller = androidx.savedstate.SavedStateRegistryController.create(this)
             init {
                 controller.performRestore(null)
             }
             override val lifecycle: androidx.lifecycle.Lifecycle = lifecycleOwner.lifecycle
             override val savedStateRegistry: androidx.savedstate.SavedStateRegistry = controller.savedStateRegistry
        }
        composeView.setViewTreeSavedStateRegistryOwner(savedStateRegistryOwner)

        setContentView(composeView)
    }
}

