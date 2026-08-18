package com.telegramdrive.uploader.domain.model

enum class TelegramConnectionState {
    DISCONNECTED,
    CONNECTING,
    WAITING_FOR_PHONE,
    WAITING_FOR_CODE,
    WAITING_FOR_PASSWORD,
    AUTHORIZED,
    CLOSING,
    ERROR
}
