package com.telegramdrive.uploader.domain.model

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

    fun getLocalizedMessage(): String {
        return when (this) {
            is InvalidPhoneNumber -> "Invalid phone number. Please enter a valid international number."
            is InvalidCode -> "The verification code you entered is invalid. Please try again."
            is InvalidPassword -> "Two-step verification password incorrect. Please try again."
            is RateLimited -> "Too many attempts. Please try again later."
            is NetworkUnavailable -> "Network is unavailable. Please check your internet connection."
            is SessionExpired -> "Your Telegram session has expired. Please authenticate again."
            is InvalidCredentials -> "Telegram API credentials not configured. Please add valid TELEGRAM_API_ID and TELEGRAM_API_HASH."
            is AppUpdateRequired -> "Telegram rejected this login code with UPDATE_APP_TO_LOGIN. This TDLib v1.8.0 build cannot complete codes delivered by another Telegram app. Use QR login, or rebuild with a newer official TDLib version."
            is TdLibRuntimeUnavailable -> "Telegram integration could not initialize TDLib on this device. Verify the APK ABI, native library load, and app configuration."
            is Unknown -> message
        }
    }
}
