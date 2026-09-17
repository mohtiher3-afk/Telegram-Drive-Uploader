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
}
