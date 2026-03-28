package org.yac.llamarangers.data.local.db

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.yac.llamarangers.domain.model.PatrolChecklistItem

class Converters {

    private val gson = Gson()

    // List<String> <-> JSON String
    @TypeConverter
    fun fromStringList(value: List<String>?): String {
        return gson.toJson(value ?: emptyList<String>())
    }

    @TypeConverter
    fun toStringList(value: String?): List<String> {
        if (value.isNullOrEmpty()) return emptyList()
        val type = object : TypeToken<List<String>>() {}.type
        return gson.fromJson(value, type) ?: emptyList()
    }

    // List<List<Double>> <-> JSON String (polygon coordinates)
    @TypeConverter
    fun fromCoordinateList(value: List<List<Double>>?): String {
        return gson.toJson(value ?: emptyList<List<Double>>())
    }

    @TypeConverter
    fun toCoordinateList(value: String?): List<List<Double>> {
        if (value.isNullOrEmpty()) return emptyList()
        val type = object : TypeToken<List<List<Double>>>() {}.type
        return gson.fromJson(value, type) ?: emptyList()
    }

    // List<PatrolChecklistItem> <-> JSON String
    @TypeConverter
    fun fromChecklistItems(value: List<PatrolChecklistItem>?): String {
        return gson.toJson(value ?: emptyList<PatrolChecklistItem>())
    }

    @TypeConverter
    fun toChecklistItems(value: String?): List<PatrolChecklistItem> {
        if (value.isNullOrEmpty()) return emptyList()
        val type = object : TypeToken<List<PatrolChecklistItem>>() {}.type
        return gson.fromJson(value, type) ?: emptyList()
    }
}
