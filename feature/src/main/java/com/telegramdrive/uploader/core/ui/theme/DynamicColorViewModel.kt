package com.telegramdrive.uploader.core.ui.theme

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel to hold extracted dynamic color state across the app.
 * Connects image picker → color extraction → theme application.
 */
@HiltViewModel
class DynamicColorViewModel @Inject constructor() : ViewModel() {

    private val _extractedScheme = MutableStateFlow<Material3ColorScheme?>(null)
    val extractedScheme: StateFlow<Material3ColorScheme?> = _extractedScheme

    private val _isExtracting = MutableStateFlow(false)
    val isExtracting: StateFlow<Boolean> = _isExtracting

    fun extractFromBitmap(bitmap: android.graphics.Bitmap, darkTheme: Boolean) {
        if (_isExtracting.value) return
        _isExtracting.value = true

        viewModelScope.launch(Dispatchers.IO) {
            val engine = DynamicColorEngine()
            val result = engine.extractDominantHueChroma(bitmap)

            val scheme = result?.let { (hue, chroma) ->
                engine.generateColorScheme(hue, chroma, darkTheme)
            }

            _extractedScheme.value = scheme
            _isExtracting.value = false
        }
    }

    fun clear() {
        _extractedScheme.value = null
    }
}