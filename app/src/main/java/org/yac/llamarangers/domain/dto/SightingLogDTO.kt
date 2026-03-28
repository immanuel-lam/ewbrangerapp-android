package org.yac.llamarangers.domain.dto

import com.google.gson.annotations.SerializedName

data class SightingLogDTO(
    val id: String,
    @SerializedName("created_at") val createdAt: String,
    @SerializedName("updated_at") val updatedAt: String,
    val latitude: Double,
    val longitude: Double,
    @SerializedName("horizontal_accuracy") val horizontalAccuracy: Double,
    val variant: String,
    @SerializedName("infestation_size") val infestationSize: String,
    val notes: String?,
    @SerializedName("photo_filenames") val photoFilenames: List<String>,
    @SerializedName("device_id") val deviceID: String,
    @SerializedName("server_id") val serverID: String?,
    @SerializedName("sync_status") val syncStatus: Int,
    @SerializedName("ranger_id") val rangerID: String,
    @SerializedName("infestation_zone_id") val infestationZoneID: String?
)
