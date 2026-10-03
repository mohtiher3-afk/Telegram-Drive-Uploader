package com.telegramdrive.uploader.core.uploads.repository

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.telegramdrive.uploader.domain.repository.UploadRepository

/**
 * Decides whether an upload may start for the current network state.
 *
 * The Wi-Fi-only toggle only restricts uploads when it is enabled; with the
 * toggle off, any network is allowed.
 */
internal fun shouldAllowUpload(useWifiOnly: Boolean, isWifiConnected: Boolean): Boolean =
    !useWifiOnly || isWifiConnected

@Composable
fun UploadViewModel(
    onUiUpdate: () -> Unit,
    repository: UploadRepository,
    useWifiOnly: Boolean = false
) {
    var _useWifiOnly by remember { mutableStateOf(useWifiOnly) }

    fun getUseWifiOnly(): Boolean = _useWifiOnly

    fun setUseWifiOnly(useWifi: Boolean) {
        _useWifiOnly = useWifi
    }

    fun isWifiConnected(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
                ?: return false
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
    }

    fun startUpload(context: Context) {
        // Restrict uploads to Wi-Fi only network when the toggle is enabled.
        if (!shouldAllowUpload(_useWifiOnly, isWifiConnected(context))) {
            Toast.makeText(context, "Upload only supported on Wi-Fi", Toast.LENGTH_SHORT).show()
        } else {
            // UploadRepository exposes no start API; notify the caller to proceed
            // with enqueueing the upload.
            onUiUpdate()
        }
    }

    fun cancelUpload() {
        onUiUpdate()
    }
}