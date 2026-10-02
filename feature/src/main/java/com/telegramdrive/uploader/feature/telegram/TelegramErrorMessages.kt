package com.telegramdrive.uploader.feature.telegram

import androidx.annotation.StringRes
import com.telegramdrive.uploader.feature.R
import com.telegramdrive.uploader.domain.model.TelegramError

@StringRes
fun TelegramError.messageResId(): Int = when (this) {
    is TelegramError.InvalidPhoneNumber -> R.string.telegram_error_invalid_phone
    is TelegramError.InvalidCode -> R.string.telegram_error_invalid_code
    is TelegramError.InvalidPassword -> R.string.telegram_error_invalid_password
    is TelegramError.RateLimited -> R.string.telegram_error_rate_limited
    is TelegramError.NetworkUnavailable -> R.string.telegram_error_network_unavailable
    is TelegramError.SessionExpired -> R.string.telegram_error_session_expired
    is TelegramError.InvalidCredentials -> R.string.telegram_error_invalid_credentials
    is TelegramError.AppUpdateRequired -> R.string.telegram_error_app_update_required
    is TelegramError.TdLibRuntimeUnavailable -> R.string.telegram_error_tdlib_unavailable
    is TelegramError.Unknown -> R.string.telegram_error_unknown
}