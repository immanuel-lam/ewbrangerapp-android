package org.yac.llamarangers.domain.dto

import com.google.gson.annotations.SerializedName

data class PatrolRecordDTO(
    val id: String,
    @SerializedName("created_at") val createdAt: String,
    @SerializedName("updated_at") val updatedAt: String,
    @SerializedName("patrol_date") val patrolDate: String,
    @SerializedName("start_time") val startTime: String,
    @SerializedName("end_time") val endTime: String?,
    @SerializedName("area_name") val areaName: String,
    @SerializedName("checklist_items_json") val checklistItemsJSON: String,
    val notes: String,
    @SerializedName("ranger_id") val rangerID: String
)
