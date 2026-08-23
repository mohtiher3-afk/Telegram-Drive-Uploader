package com.telegramdrive.uploader.core.util

/**
 * Deterministic serialization for locally pinned Telegram destination IDs.
 * The representation contains IDs only; Telegram metadata remains owned by TDLib.
 */
object PinnedDestinationIds {
    fun parse(value: String?): Set<Long> = value
        .orEmpty()
        .split(',')
        .mapNotNull { it.trim().toLongOrNull() }
        .toSet()

    fun encode(ids: Set<Long>): String = ids
        .toList()
        .sorted()
        .joinToString(",")

    fun toggle(ids: Set<Long>, destinationId: Long): Set<Long> =
        if (destinationId in ids) ids - destinationId else ids + destinationId
}
