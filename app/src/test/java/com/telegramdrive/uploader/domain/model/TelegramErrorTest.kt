package com.telegramdrive.uploader.domain.model

import com.telegramdrive.uploader.R
import org.junit.Assert.assertEquals
import org.junit.Test

class TelegramErrorTest {
    @Test
    fun knownErrorsMapToLocalizedResources() {
        val mappings = mapOf(
            TelegramError.InvalidPhoneNumber to R.string.telegram_error_invalid_phone,
            TelegramError.InvalidCode to R.string.telegram_error_invalid_code,
            TelegramError.InvalidPassword to R.string.telegram_error_invalid_password,
            TelegramError.RateLimited to R.string.telegram_error_rate_limited,
            TelegramError.NetworkUnavailable to R.string.telegram_error_network_unavailable,
            TelegramError.SessionExpired to R.string.telegram_error_session_expired,
            TelegramError.InvalidCredentials to R.string.telegram_error_invalid_credentials,
            TelegramError.AppUpdateRequired to R.string.telegram_error_app_update_required,
            TelegramError.TdLibRuntimeUnavailable to R.string.telegram_error_tdlib_unavailable
        )

        mappings.forEach { (error, resourceId) ->
            assertEquals(resourceId, error.messageResId())
        }
    }

    @Test
    fun unknownErrorsUseSafeGenericResource() {
        assertEquals(
            R.string.telegram_error_unknown,
            TelegramError.Unknown("SECRET_INTERNAL_TDLIB_MESSAGE").messageResId()
        )
    }
}
