package org.yac.llamarangers.domain.dto

import com.google.gson.annotations.SerializedName

data class ZoneSnapshotDTO(
    val id: String,
    @SerializedName("created_at") val createdAt: String,
    @SerializedName("snapshot_date") val snapshotDate: String,
    @SerializedName("polygon_coordinates") val polygonCoordinates: List<List<Double>>,
    val area: Double,
    @SerializedName("created_by_ranger_id") val createdByRangerID: String,
    @SerializedName("zone_id") val zoneID: String
)
