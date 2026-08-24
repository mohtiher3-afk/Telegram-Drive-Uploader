package com.telegramdrive.uploader.domain.model

import androidx.annotation.StringRes
import com.telegramdrive.uploader.R

sealed class TelegramError {
    object InvalidPhoneNumber : TelegramError()
    object InvalidCode : TelegramError()
    object InvalidPassword : TelegramError()
    object RateLimited : TelegramError()
    object NetworkUnavailable : TelegramError()
    object SessionExpired : TelegramError()
    object InvalidCredentials : TelegramError()
    object AppUpdateRequired : TelegramError()
    object TdLibRuntimeUnavailable : TelegramError()
    data class Unknown(val message: String) : TelegramError()

    @StringRes
    fun messageResId(): Int = when (this) {
        is InvalidPhoneNumber -> R.string.telegram_error_invalid_phone
        is InvalidCode -> R.string.telegram_error_invalid_code
        is InvalidPassword -> R.string.telegram_error_invalid_password
        is RateLimited -> R.string.telegram_error_rate_limited
        is NetworkUnavailable -> R.string.telegram_error_network_unavailable
        is SessionExpired -> R.string.telegram_error_session_expired
        is InvalidCredentials -> R.string.telegram_error_invalid_credentials
        is AppUpdateRequired -> R.string.telegram_error_app_update_required
        is TdLibRuntimeUnavailable -> R.string.telegram_error_tdlib_unavailable
        is Unknown -> R.string.telegram_error_unknown
    }
}
