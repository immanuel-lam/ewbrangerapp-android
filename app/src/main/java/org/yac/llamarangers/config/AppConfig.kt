package org.yac.llamarangers.config

object AppConfig {
    const val BUNDLE_ID = "org.yac.llamarangers"

    // Supabase (V3 - not yet implemented)
    const val SUPABASE_URL = ""
    const val SUPABASE_ANON_KEY = ""

    // Storage
    const val PHOTOS_BUCKET = "lantana-photos"
    const val SIGNED_URL_EXPIRY = 3600 // seconds

    // Nearby Connections mesh sync
    const val MESH_SERVICE_TYPE = "yac-lantana"

    // Local photo directory
    const val PHOTOS_DIRECTORY_NAME = "Photos"
}
