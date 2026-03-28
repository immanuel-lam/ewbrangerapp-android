package org.yac.llamarangers.domain.model.enums

enum class SyncStatus(val value: Int) {
    PENDING_CREATE(0),
    PENDING_UPDATE(1),
    PENDING_DELETE(2),
    SYNCED(3);

    val iconName: String
        get() = when (this) {
            PENDING_CREATE, PENDING_UPDATE -> "cloud_upload"
            PENDING_DELETE -> "delete"
            SYNCED -> "check_circle"
        }

    companion object {
        fun fromValue(value: Int): SyncStatus =
            entries.firstOrNull { it.value == value } ?: PENDING_CREATE
    }
}
