package org.yac.llamarangers.domain.dto

import com.google.gson.annotations.SerializedName

data class TreatmentRecordDTO(
    val id: String,
    @SerializedName("created_at") val createdAt: String,
    @SerializedName("updated_at") val updatedAt: String,
    @SerializedName("treatment_date") val treatmentDate: String,
    val method: String,
    @SerializedName("herbicide_product") val herbicideProduct: String?,
    @SerializedName("outcome_notes") val outcomeNotes: String?,
    @SerializedName("follow_up_date") val followUpDate: String?,
    @SerializedName("photo_filenames") val photoFilenames: List<String>,
    @SerializedName("sighting_id") val sightingID: String,
    @SerializedName("ranger_id") val rangerID: String
)
