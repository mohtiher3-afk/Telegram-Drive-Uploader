package com.telegramdrive.uploader.core.ui.theme

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.ViewModelProvider

/**
 * Extension to decode a Uri into a Bitmap (downsampled for performance).
 */
fun Uri.toBitmap(context: Context): Bitmap {
    val contentResolver = context.contentResolver
    val inputStream = contentResolver.openInputStream(this)
        ?: return Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)
    val options = android.graphics.BitmapFactory.Options().apply {
        inJustDecodeBounds = true
    }
    android.graphics.BitmapFactory.decodeStream(inputStream, null, options)
    inputStream.close()

    // Downsample to max 512px for color extraction performance
    val maxDimension = 512
    val scale = maxOf(1, maxOf(options.outWidth, options.outHeight) / maxDimension)
    options.inJustDecodeBounds = false
    options.inSampleSize = scale

    val inputStream2 = contentResolver.openInputStream(this)
        ?: return Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)
    val bitmap = android.graphics.BitmapFactory.decodeStream(inputStream2, null, options)
    return bitmap ?: Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)
}

@Composable
fun rememberDynamicColorViewModel(): DynamicColorViewModel {
    val context = LocalContext.current
    return viewModel(
        factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return DynamicColorViewModel() as T
            }
        },
        viewModelStoreOwner = context as androidx.lifecycle.ViewModelStoreOwner
    )
}