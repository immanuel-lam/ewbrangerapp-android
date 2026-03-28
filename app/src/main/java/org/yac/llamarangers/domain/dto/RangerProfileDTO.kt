package org.yac.llamarangers.domain.dto

import com.google.gson.annotations.SerializedName

data class RangerProfileDTO(
    val id: String,
    @SerializedName("created_at") val createdAt: String,
    @SerializedName("updated_at") val updatedAt: String,
    @SerializedName("supabase_uid") val supabaseUID: String,
    @SerializedName("display_name") val displayName: String,
    val role: String,
    @SerializedName("avatar_filename") val avatarFilename: String?
)
